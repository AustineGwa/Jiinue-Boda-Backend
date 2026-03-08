package com.otblabs.jiinueboda.integrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class BulkNumberInserter {
    private static final Logger log = LoggerFactory.getLogger(BulkNumberInserter.class);

    private final DataSource dataSource;
    private final int threadCount;
    private final int batchSize;
    private final int maxRetries;
    private final long retryBackoffBaseMillis; // base for exponential backoff
    private final Function<Long, String> hasher; // injection point for hashString()

    public BulkNumberInserter(DataSource dataSource,
                              int threadCount,
                              int batchSize,
                              int maxRetries,
                              long retryBackoffBaseMillis,
                              Function<Long, String> hasher) {
        this.dataSource = dataSource;
        this.threadCount = threadCount;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
        this.retryBackoffBaseMillis = retryBackoffBaseMillis;
        this.hasher = hasher;
    }

    /**
     * Partition range [start, end] across threads and run insertion.
     */
    public void run(long start, long end) throws InterruptedException, ExecutionException {
        if (start > end) throw new IllegalArgumentException("start > end");
        long total = end - start + 1;
        log.info("Starting bulk insert: start={}, end={}, total={}, threads={}, batchSize={}",
                start, end, total, threadCount, batchSize);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount, r -> {
            Thread t = new Thread(r);
            t.setDaemon(false);
            return t;
        });

        AtomicLong progress = new AtomicLong(0);
        Instant begin = Instant.now();

        List<Future<Long>> futures = new ArrayList<>(threadCount);
        long perThread = total / threadCount;
        long remainder = total % threadCount;
        long partStart = start;

        for (int i = 0; i < threadCount; i++) {
            long partSize = perThread + (i < remainder ? 1 : 0);
            long partEnd = partStart + partSize - 1;
            if (partSize <= 0) {
                break; // fewer tasks than threads
            }
            final long s = partStart;
            final long e = partEnd;

            Callable<Long> worker = () -> insertRangeWorker(s, e, progress);
            futures.add(executor.submit(worker));

            partStart = partEnd + 1;
        }

        // wait for completion
        long totalInserted = 0;
        for (Future<Long> f : futures) {
            totalInserted += f.get(); // propagate exceptions
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        Duration elapsed = Duration.between(begin, Instant.now());
        log.info("All done. inserted={}, elapsed={}, rows/sec={}",
                totalInserted, formatDuration(elapsed),
                (totalInserted / Math.max(1.0, elapsed.toMillis() / 1000.0)));
    }

    private long insertRangeWorker(long workerStart, long workerEnd, AtomicLong progress) throws Exception {
        log.info("Worker start={} end={}", workerStart, workerEnd);
        long insertedByThisWorker = 0L;

        String sql = "INSERT INTO mpesa_hash_table (number, hash) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            int batchCount = 0;
            List<Long> batchNumbers = new ArrayList<>(batchSize);

            for (long num = workerStart; num <= workerEnd; num++) {
                String hash = hasher.apply(num);

                ps.setLong(1, num);
                ps.setString(2, hash);
                ps.addBatch();

                batchNumbers.add(num);
                batchCount++;

                if (batchCount >= batchSize) {
                    executeBatchWithRetries(conn, ps, batchNumbers);
                    insertedByThisWorker += batchCount;
                    progress.addAndGet(batchCount);
                    batchNumbers.clear();
                    batchCount = 0;

                    if (progress.get() % Math.max(1, batchSize * 10) == 0) {
                        log.info("Progress: inserted {} rows (worker range {}-{})", progress.get(), workerStart, workerEnd);
                    }
                }
            }

            // final partial batch
            if (batchCount > 0) {
                executeBatchWithRetries(conn, ps, batchNumbers);
                insertedByThisWorker += batchCount;
                progress.addAndGet(batchCount);
            }

        } catch (SQLException ex) {
            log.error("Worker failed for range {}-{}", workerStart, workerEnd, ex);
            throw ex;
        }

        log.info("Worker finished range {}-{} inserted={}", workerStart, workerEnd, insertedByThisWorker);
        return insertedByThisWorker;
    }

    private void executeBatchWithRetries(Connection conn, PreparedStatement ps, List<Long> batchNumbers) throws Exception {
        int attempt = 0;
        while (true) {
            try {
                ps.executeBatch();
                conn.commit();
                ps.clearBatch();
                return;
            } catch (SQLException ex) {
                attempt++;
                log.warn("Batch failed (attempt {} / {}). batchSize={}, error={}", attempt, maxRetries, batchNumbers.size(), ex.getMessage());
                try {
                    conn.rollback();
                } catch (SQLException rbe) {
                    log.warn("Rollback failed: {}", rbe.getMessage());
                }

                if (attempt > maxRetries) {
                    // attempt a single-row fallback to find bad row (optional) or rethrow
                    log.error("Max retries reached for a batch. Rethrowing exception.");
                    throw ex;
                }

                long backoff = retryBackoffBaseMillis * (1L << (attempt - 1));
                log.info("Retrying after {} ms", backoff);
                Thread.sleep(backoff);
                // Note: prepared statement still has batched items so just loop and retry
            }
        }
    }

    private static String formatDuration(Duration d) {
        long s = d.getSeconds();
        return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

}

