package com.parking.tds.rest;

import com.parking.tds.dto.CarBillingInfo;
import com.parking.tds.dto.ParkedCarInfo;
import com.parking.tds.dto.ParkedCarRequest;
import com.parking.tds.dto.ParkingAvailabilityDto;
import com.parking.tds.service.ParkingService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Builder
@RequestMapping("parking")
@Slf4j
public class ParkingController {

    private final ParkingService parkingService;

    /**
     * Gets available and occupied number of spaces
     * @return available and occupied parking spaces
     */
    @GetMapping
    public ParkingAvailabilityDto getAvailableSpaces() {
        return parkingService.getAvailability();
    }

    /**
     *
     * @param parkedCarRequest
     * @return
     */
    @PostMapping
    public ParkedCarInfo parkCar(@RequestBody  ParkedCarRequest parkedCarRequest) {
        log.debug("parkedCarRequest-->{}", parkedCarRequest);
        return parkingService.parkCar(parkedCarRequest);
    }

    /**
     *
     * @param parkedCarRequest
     * @return
     */
    @PostMapping("/bill")
    public CarBillingInfo generateBill(@RequestBody  ParkedCarRequest parkedCarRequest) {
        log.debug("parkedCarRequest-->{}", parkedCarRequest);
        return parkingService.generateBilling(parkedCarRequest.getVehicleReg());

    }
}
