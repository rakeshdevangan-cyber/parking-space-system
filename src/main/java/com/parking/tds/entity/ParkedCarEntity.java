package com.parking.tds.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parked_car_info")
public class ParkedCarEntity {

    @Id
    @GeneratedValue
    private long id;

    private String vehicleReg;

    private Long spaceNumber;

    private LocalDateTime timeIn;

    private LocalDateTime timeOut;

    private int vehicleType;
}
