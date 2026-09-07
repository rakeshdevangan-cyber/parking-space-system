package com.parking.tds.entity.repository;

import com.parking.tds.entity.ParkedCarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface ParkedCarEntityRepository extends JpaRepository<ParkedCarEntity, Long> {

    Optional<ParkedCarEntity> findFirstByVehicleRegOrderByTimeInDesc(String vehicleReg);

}
