package com.parking.tds.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "billing_info")
public class BillingInfoEntity {

    @Id
    @GeneratedValue
    private long billId;
    private String vehicleReg;
    private double vehicleCharge;
}
