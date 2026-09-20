package com.materia.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class Backend2Application {

    public static void main(String[] args) {
        SpringApplication.run(Backend2Application.class, args);
    }

}   