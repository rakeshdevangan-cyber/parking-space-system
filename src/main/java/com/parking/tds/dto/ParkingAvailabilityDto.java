package com.parking.tds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingAvailabilityDto {

    private int availableSpace;

    private int occupiedSpace;

}
