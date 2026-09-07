package com.parking.tds.config;

import com.parking.tds.entity.ParkingSpaceEntity;
import com.parking.tds.entity.repository.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Initialises parking spaces with all true in at the server start
 */
@Configuration
@RequiredArgsConstructor
public class ParkingSpaceConfiguration {

    private final ParkingSpaceRepository parkingSpaceRepository;

    @Bean
    CommandLineRunner initialiseParkingSpaces() {
        return  args -> {

            int totalSpace = 500;

            if(parkingSpaceRepository.count() == 0) {

                List<ParkingSpaceEntity> parkingSpaceEntities = IntStream.rangeClosed(1, totalSpace).mapToObj(i -> ParkingSpaceEntity
                        .builder().available(true).build()).toList();

                parkingSpaceRepository.saveAll(parkingSpaceEntities);
            }
        };
    }


}
