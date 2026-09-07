package com.parking.tds.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkedCarInfo {

    private long id;

    private String vehicleReg;

    private Long spaceNumber;

    private LocalDateTime timeIn;


}
