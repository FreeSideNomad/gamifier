package com.starfleet.gamifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starfleet Gamifier Application
 *
 * Unified application containing both Configuration Service (Knowledge Layer)
 * and Gamification Service for employee behavior incentivization.
 */
@SpringBootApplication
public class GamifierApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamifierApplication.class, args);
    }
}