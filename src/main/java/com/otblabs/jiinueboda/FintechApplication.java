package com.otblabs.jiinueboda;

import com.otblabs.jiinueboda.Runnertests.RunnerTest;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAsync
public class FintechApplication implements ApplicationRunner {

	private final RunnerTest runnerTest;

    public FintechApplication(RunnerTest runnerTest) {
        this.runnerTest = runnerTest;
    }

    public static void main(String[] args) {
		SpringApplication.run(FintechApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		   System.out.println("Application started , start scheduled services");

    }

}



