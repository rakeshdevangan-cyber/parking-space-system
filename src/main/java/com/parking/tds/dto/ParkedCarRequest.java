package com.parking.tds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ParkedCarRequest {

    private String vehicleReg;

    private int  vehicleType;
}
