package com.example.maintanence_request_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MaintenanceRequestSystemApplication {
    public static DBManager manager;

    public static void main(String[] args) {
        manager = new DBManager();
        SpringApplication.run(MaintenanceRequestSystemApplication.class, args);
    }
}
