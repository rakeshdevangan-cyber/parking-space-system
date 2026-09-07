package com.parking.tds.service;

import com.parking.tds.dto.CarBillingInfo;
import com.parking.tds.dto.ParkedCarInfo;
import com.parking.tds.dto.ParkedCarRequest;
import com.parking.tds.dto.ParkingAvailabilityDto;
import com.parking.tds.entity.BillingInfoEntity;
import com.parking.tds.entity.ParkedCarEntity;
import com.parking.tds.entity.ParkingSpaceEntity;
import com.parking.tds.entity.repository.BillingInfoRepository;
import com.parking.tds.entity.repository.ParkedCarEntityRepository;
import com.parking.tds.entity.repository.ParkingSpaceRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import org.springframework.boot.context.properties.bind.Nested;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Builder
public class ParkingService {
    private static int INITIAL_AVAILABLE_SPACE = 500;
    private static int OCCUPIED_SPACE = 0;

    private static final int FREE_PARKING_MINUTES = 180;

    private final ParkedCarEntityRepository parkedCarEntityRepository;

    private final BillingInfoRepository billingInfoRepository;

    private final ParkingSpaceRepository parkingSpaceRepository;

    private final Clock clock;

    public ParkingAvailabilityDto getAvailability() {
        return new  ParkingAvailabilityDto(INITIAL_AVAILABLE_SPACE, OCCUPIED_SPACE);
    }


    @Transactional
    public ParkedCarInfo parkCar(final ParkedCarRequest parkedCarRequest) {

        Optional<ParkedCarEntity> carEntity  = parkedCarEntityRepository.findFirstByVehicleRegOrderByTimeInDesc(parkedCarRequest.getVehicleReg());
        if(carEntity.isPresent() && carEntity.get().getTimeOut() == null) {
            throw new RuntimeException("Car with this registration is already parked "+parkedCarRequest.getVehicleReg());
        }

        final ParkingSpaceEntity parkingSpaceEntity = parkingSpaceRepository.findFirstByAvailableTrueOrderByIdAsc().orElseThrow(() -> new RuntimeException("No Space Available"));
        ParkedCarEntity parkedCarEntity = ParkedCarEntity.builder().vehicleType(parkedCarRequest.getVehicleType()).spaceNumber(parkingSpaceEntity.getId()).vehicleReg(parkedCarRequest.getVehicleReg()).timeIn(LocalDateTime.now()).build();
        parkedCarEntity = parkedCarEntityRepository.save(parkedCarEntity);

        parkingSpaceEntity.setAvailable(false);
        parkingSpaceRepository.save(parkingSpaceEntity);
        calculateParkingSpaceAvl("entry");
        return ParkedCarInfo.builder().spaceNumber(parkedCarEntity.getSpaceNumber()).timeIn(parkedCarEntity.getTimeIn())
                .vehicleReg(parkedCarEntity.getVehicleReg())
                .id(parkedCarEntity.getId())
                .build();
    }

    private void calculateParkingSpaceAvl(String action) {
        switch(action.toLowerCase()) {
            case "entry" -> calculateAvlSpaceOnEntry();
            case "exit"  -> calculateAvlSpaceOneExit();
        }
    }

    private void calculateAvlSpaceOnEntry() {
        INITIAL_AVAILABLE_SPACE--;
        OCCUPIED_SPACE++;
    }

    private void calculateAvlSpaceOneExit() {
        INITIAL_AVAILABLE_SPACE++;
        OCCUPIED_SPACE--;
    }

    @Transactional
    public CarBillingInfo generateBilling(final String vehicleReg) {
        ParkedCarEntity  parkedCarEntity =  parkedCarEntityRepository.findFirstByVehicleRegOrderByTimeInDesc(vehicleReg)
                .orElseThrow(() -> new RuntimeException("Vehicle Not Found"));
        LocalDateTime timeOut =  LocalDateTime.now(clock);
        parkedCarEntity.setTimeOut(timeOut);
        parkedCarEntityRepository.save(parkedCarEntity);

        ParkingSpaceEntity parkingSpaceEntity = new ParkingSpaceEntity();
        parkingSpaceEntity.setAvailable(true);
        parkingSpaceEntity.setId(parkedCarEntity.getId());
        parkingSpaceRepository.save(parkingSpaceEntity);


        final double vehicleCharge = calculateCharge(parkedCarEntity);

        BillingInfoEntity billingInfoEntity = BillingInfoEntity.builder().vehicleReg(vehicleReg).vehicleCharge(vehicleCharge).build();
        billingInfoEntity = billingInfoRepository.save(billingInfoEntity);
        calculateParkingSpaceAvl("exit");
        return  CarBillingInfo.builder().billId(String.valueOf(billingInfoEntity.getBillId()))
                .timeIn(parkedCarEntity.getTimeIn())
                .timeOut(parkedCarEntity.getTimeOut())
                .vehicleCharge(vehicleCharge)
                .vehicleReg(vehicleReg)
                .build();
    }

    private double calculateCharge(final ParkedCarEntity  parkedCarEntity) {
        long calculateParkedMinutes = Duration.between(parkedCarEntity.getTimeIn(), parkedCarEntity.getTimeOut()).toMinutes();
        if(calculateParkedMinutes < 0) {
            calculateParkedMinutes = 0;
        }

        //calculate base charge for no of minutes

        double baseRatePerMinute = 0.0;

        switch(parkedCarEntity.getVehicleType()) {
            case 1 -> baseRatePerMinute = 0.10;
            case 2 -> baseRatePerMinute = 0.20;
            case 3 -> baseRatePerMinute = 0.40;
            default -> throw new IllegalArgumentException("No vehicle type found for "+parkedCarEntity.getVehicleReg());
        }
        double totalBaseRate =  calculateParkedMinutes * baseRatePerMinute;

        // calculate additional minutes
        double additionalCharge = 0.0;
        if(calculateParkedMinutes > 180) {
            long extraMinutes =  calculateParkedMinutes - 180;

             long blockOf5ExtraMinute = (long)Math.ceil(extraMinutes/5);
            additionalCharge  = (blockOf5ExtraMinute + 1);
        }
        double totalCharge =
                totalBaseRate + additionalCharge;

        return Math.round(totalCharge * 100.0) / 100.0;

    }


}
