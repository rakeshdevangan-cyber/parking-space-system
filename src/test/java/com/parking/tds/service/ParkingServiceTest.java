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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {



    @Mock
    private ParkedCarEntityRepository parkedCarEntityRepository;

    @Mock
    private BillingInfoRepository billingInfoRepository;

    @Mock
    private  ParkingSpaceRepository parkingSpaceRepository;


    private ParkingService parkingService;

    private final static String REG_NUMBER = "N321RGM";

    private final static int TYPE = 1;

    private Clock clock;

    @BeforeEach
    void setUp() {

        clock = Clock.fixed(
                Instant.parse("2026-09-05T12:00:00Z"),
                ZoneId.of("UTC")
        );

        parkingService = new ParkingService(
                parkedCarEntityRepository,
                billingInfoRepository,
                parkingSpaceRepository,
                clock
        );
    }

    @Test
    void testGetAvailability() {
        ParkingAvailabilityDto availabilityDto = parkingService.getAvailability();
        assertEquals(500, availabilityDto.getAvailableSpace());
        assertEquals(0, availabilityDto.getOccupiedSpace());
    }





    @Nested
    class BillingGeneration {
        //When car with  same registration has not time out ; It means it is already in the parking
        @Test
        void testParkCar_NoTimeOut() {
            final ParkedCarRequest parkedCarRequest = ParkedCarRequest.builder().vehicleReg(REG_NUMBER).vehicleType(TYPE).build();
            Optional<ParkedCarEntity> carEntity = Optional.ofNullable(ParkedCarEntity.builder()
                    .timeIn(LocalDateTime.now())
                    .vehicleType(TYPE)
                    .vehicleReg(REG_NUMBER).build());
            when(parkedCarEntityRepository.findFirstByVehicleRegOrderByTimeInDesc(REG_NUMBER)).thenReturn(carEntity);

            assertThrows(RuntimeException.class, () -> {
                parkingService.parkCar(parkedCarRequest);
            });
        }

        @Test
        void testParkCar_GenerateBillsSuccessfully_LessThan180Mins() {
            LocalDateTime timeIn = LocalDateTime.of(2026, 9, 5, 10, 0);
            Optional<ParkedCarEntity> carEntity = Optional.ofNullable(ParkedCarEntity.builder()
                    .timeIn(timeIn)
                    .vehicleType(TYPE)
                    .vehicleReg(REG_NUMBER).build());
            when(parkedCarEntityRepository.findFirstByVehicleRegOrderByTimeInDesc(REG_NUMBER)).thenReturn(carEntity);
            when(parkedCarEntityRepository.save(carEntity.get())).thenReturn(carEntity.get());
            double vehicleCharge = 12.0;
            BillingInfoEntity billingInfoEntity = BillingInfoEntity.builder().vehicleReg(REG_NUMBER).vehicleCharge(vehicleCharge).build();
            when(billingInfoRepository.save(billingInfoEntity)).thenReturn(billingInfoEntity);

            CarBillingInfo actualBillingInfo = parkingService.generateBilling(REG_NUMBER);

            assertEquals(
                    LocalDateTime.of(2026, 9, 5, 12, 0),
                    actualBillingInfo.timeOut()
            );

            assertEquals(12.0, actualBillingInfo.vehicleCharge());
        }

        @Test
        void testParkCar_GenerateBillsSuccessfully_MoreThan180Mins() {
            LocalDateTime timeIn = LocalDateTime.of(2026, 9, 5, 8, 56);
            Optional<ParkedCarEntity> carEntity = Optional.ofNullable(ParkedCarEntity.builder()
                    .timeIn(timeIn)
                    .vehicleType(TYPE)
                    .vehicleReg(REG_NUMBER).build());
            when(parkedCarEntityRepository.findFirstByVehicleRegOrderByTimeInDesc(REG_NUMBER)).thenReturn(carEntity);
            when(parkedCarEntityRepository.save(carEntity.get())).thenReturn(carEntity.get());
            double vehicleCharge = 19.4;
            BillingInfoEntity billingInfoEntity = BillingInfoEntity.builder().vehicleReg(REG_NUMBER).vehicleCharge(vehicleCharge).build();
            when(billingInfoRepository.save(billingInfoEntity)).thenReturn(billingInfoEntity);

            CarBillingInfo actualBillingInfo = parkingService.generateBilling(REG_NUMBER);

            assertEquals(
                    LocalDateTime.of(2026, 9, 5, 12, 0),
                    actualBillingInfo.timeOut()
            );

            assertEquals(19.4, actualBillingInfo.vehicleCharge(),  0.01);
        }

        @Test
        void testParkCar_GenerateBillsSuccessfully_EqualsTo180Mins() {
            LocalDateTime timeIn = LocalDateTime.of(2026, 9, 5, 9, 0);
            Optional<ParkedCarEntity> carEntity = Optional.ofNullable(ParkedCarEntity.builder()
                    .timeIn(timeIn)
                    .vehicleType(TYPE)
                    .vehicleReg(REG_NUMBER).build());
            when(parkedCarEntityRepository.findFirstByVehicleRegOrderByTimeInDesc(REG_NUMBER)).thenReturn(carEntity);
            when(parkedCarEntityRepository.save(carEntity.get())).thenReturn(carEntity.get());
            double vehicleCharge = 18.0;
            BillingInfoEntity billingInfoEntity = BillingInfoEntity.builder().vehicleReg(REG_NUMBER).vehicleCharge(vehicleCharge).build();
            when(billingInfoRepository.save(billingInfoEntity)).thenReturn(billingInfoEntity);

            CarBillingInfo actualBillingInfo = parkingService.generateBilling(REG_NUMBER);

            assertEquals(
                    LocalDateTime.of(2026, 9, 5, 12, 0),
                    actualBillingInfo.timeOut()
            );

            assertEquals(18.0, actualBillingInfo.vehicleCharge(),  0.01);
        }

        @Test
        void testGenerateBilling_VehicleNotFound() {

            when(parkedCarEntityRepository
                    .findFirstByVehicleRegOrderByTimeInDesc(REG_NUMBER))
                    .thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> parkingService.generateBilling(REG_NUMBER)
            );

            assertEquals("Vehicle Not Found", exception.getMessage());

            verify(parkedCarEntityRepository, never())
                    .save(any());

            verify(billingInfoRepository, never())
                    .save(any());
        }
    }

    @Nested
    class ParkCarTests {
        @Test
        void parkCar_shouldParkCarSuccessfully() {

            ParkedCarRequest request = ParkedCarRequest.builder()
                    .vehicleReg("N321RGM")
                    .vehicleType(1)
                    .build();

            ParkingSpaceEntity space = ParkingSpaceEntity.builder()
                    .id(1L)
                    .available(true)
                    .build();

            ParkedCarEntity savedCar = ParkedCarEntity.builder()
                    .id(10L)
                    .vehicleReg("N321RGM")
                    .vehicleType(1)
                    .spaceNumber(1L)
                    .timeIn(LocalDateTime.of(2026, 9, 5, 10, 0))
                    .build();

            // No existing parked car
            when(parkedCarEntityRepository
                    .findFirstByVehicleRegOrderByTimeInDesc("N321RGM"))
                    .thenReturn(Optional.empty());

            // Available space
            when(parkingSpaceRepository
                    .findFirstByAvailableTrueOrderByIdAsc())
                    .thenReturn(Optional.of(space));

            // Save car
            when(parkedCarEntityRepository.save(any(ParkedCarEntity.class)))
                    .thenReturn(savedCar);

            // Save parking space
            when(parkingSpaceRepository.save(any(ParkingSpaceEntity.class)))
                    .thenReturn(space);

            ParkedCarInfo result = parkingService.parkCar(request);

            assertEquals("N321RGM", result.getVehicleReg());
            assertEquals(1L, result.getSpaceNumber());
            assertEquals(10L, result.getId());

            verify(parkedCarEntityRepository).save(any(ParkedCarEntity.class));
            verify(parkingSpaceRepository).save(space);

            assertFalse(space.getAvailable());
        }

        @Test
        void parkCar_shouldThrowException_whenCarAlreadyParked() {

            ParkedCarRequest request = ParkedCarRequest.builder()
                    .vehicleReg("N321RGM")
                    .vehicleType(1)
                    .build();

            ParkedCarEntity existingCar = ParkedCarEntity.builder()
                    .id(10L)
                    .vehicleReg("N321RGM")
                    .vehicleType(1)
                    .timeIn(LocalDateTime.of(2026, 9, 5, 10, 0))
                    .timeOut(null)
                    .build();

            when(parkedCarEntityRepository
                    .findFirstByVehicleRegOrderByTimeInDesc("N321RGM"))
                    .thenReturn(Optional.of(existingCar));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> parkingService.parkCar(request)
            );

            assertEquals(
                    "Car with this registration is already parked N321RGM",
                    exception.getMessage()
            );

            verify(parkingSpaceRepository, never())
                    .findFirstByAvailableTrueOrderByIdAsc();

            verify(parkedCarEntityRepository, never())
                    .save(any(ParkedCarEntity.class));
        }

        @Test
        void parkCar_shouldThrowException_whenNoSpaceAvailable() {

            ParkedCarRequest request = ParkedCarRequest.builder()
                    .vehicleReg("N321RGM")
                    .vehicleType(1)
                    .build();

            when(parkedCarEntityRepository
                    .findFirstByVehicleRegOrderByTimeInDesc("N321RGM"))
                    .thenReturn(Optional.empty());

            when(parkingSpaceRepository
                    .findFirstByAvailableTrueOrderByIdAsc())
                    .thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> parkingService.parkCar(request)
            );

            assertEquals("No Space Available", exception.getMessage());

            verify(parkedCarEntityRepository, never())
                    .save(any(ParkedCarEntity.class));

            verify(parkingSpaceRepository, never())
                    .save(any(ParkingSpaceEntity.class));
        }

        @Test
        void parkCar_shouldAllowCarToParkAgain_whenPreviouslyExited() {

            ParkedCarRequest request = ParkedCarRequest.builder()
                    .vehicleReg("N321RGM")
                    .vehicleType(1)
                    .build();

            ParkedCarEntity previousCar = ParkedCarEntity.builder()
                    .id(10L)
                    .vehicleReg("N321RGM")
                    .timeIn(LocalDateTime.of(2026, 9, 5, 8, 0))
                    .timeOut(LocalDateTime.of(2026, 9, 5, 9, 0))
                    .build();

            ParkingSpaceEntity space = ParkingSpaceEntity.builder()
                    .id(2L)
                    .available(true)
                    .build();

            ParkedCarEntity newCar = ParkedCarEntity.builder()
                    .id(11L)
                    .vehicleReg("N321RGM")
                    .vehicleType(1)
                    .spaceNumber(2L)
                    .timeIn(LocalDateTime.of(2026, 9, 5, 10, 0))
                    .build();

            when(parkedCarEntityRepository
                    .findFirstByVehicleRegOrderByTimeInDesc("N321RGM"))
                    .thenReturn(Optional.of(previousCar));

            when(parkingSpaceRepository
                    .findFirstByAvailableTrueOrderByIdAsc())
                    .thenReturn(Optional.of(space));

            when(parkedCarEntityRepository.save(any(ParkedCarEntity.class)))
                    .thenReturn(newCar);

            when(parkingSpaceRepository.save(any(ParkingSpaceEntity.class)))
                    .thenReturn(space);

            ParkedCarInfo result = parkingService.parkCar(request);

            assertEquals("N321RGM", result.getVehicleReg());
            assertEquals(2L, result.getSpaceNumber());

            verify(parkedCarEntityRepository).save(any(ParkedCarEntity.class));
            verify(parkingSpaceRepository).save(space);
        }
    }


}
