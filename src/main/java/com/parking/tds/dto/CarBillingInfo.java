package com.parking.tds.dto;

import lombok.*;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record CarBillingInfo(
        String billId,
        String vehicleReg,
        double vehicleCharge,
        LocalDateTime timeIn,
        LocalDateTime timeOut
) {}
