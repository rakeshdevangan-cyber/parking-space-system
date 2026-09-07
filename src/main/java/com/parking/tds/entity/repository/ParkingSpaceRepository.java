package com.parking.tds.entity.repository;

import com.parking.tds.entity.ParkingSpaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface ParkingSpaceRepository
        extends JpaRepository<ParkingSpaceEntity, Long> {

    Optional<ParkingSpaceEntity> findFirstByAvailableTrueOrderByIdAsc();
}