package com.otblabs.jiinueboda.fieldapp.repository;

import com.otblabs.jiinueboda.fieldapp.models.FieldOperationsTracker;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface FieldOperationsTrackerRepository extends CrudRepository<FieldOperationsTracker, Long> {

//    @Query("SELECT * FROM field_operations_tracker WHERE user_id = :userId AND session_date = :sessionDate AND session_status = 'active'")
      @Query("SELECT * FROM field_operations_tracker WHERE user_id = :userId AND session_status = 'active'")
    Optional<FieldOperationsTracker> findActiveSessionByUser(@Param("userId") Long userId, LocalDate sessionDate);

    @Modifying
    @Query("UPDATE field_operations_tracker SET session_status = :status, updated_at = :updatedAt WHERE id = :id")
    void updateSessionStatus(@Param("id") Long id, @Param("status") String status, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query("UPDATE field_operations_tracker SET end_time = :endTime, end_lat = :endLat, end_lng = :endLng, end_location_accuracy = :endLocationAccuracy, session_status = 'completed', updated_at = :updatedAt WHERE id = :id")
    void endSession(@Param("id") Long id, @Param("endTime") LocalDateTime endTime, @Param("endLat") BigDecimal endLat, @Param("endLng") BigDecimal endLng, @Param("endLocationAccuracy") Float endLocationAccuracy, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query("UPDATE field_operations_tracker SET network_status = :networkStatus, battery_level = :batteryLevel, weather_conditions = :weatherConditions, temperature = :temperature, updated_at = :updatedAt WHERE id = :id")
    void updateSessionInfo(@Param("id") Long id, @Param("networkStatus") String networkStatus, @Param("batteryLevel") Integer batteryLevel, @Param("weatherConditions") String weatherConditions, @Param("temperature") Float temperature, @Param("updatedAt") LocalDateTime updatedAt);
}
