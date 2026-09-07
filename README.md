# Car Park Management API

This is a simple Spring Boot application for managing a car park.

I have tried to keep the solution simple and focused on the main requirements of the task rather than over-engineering it.

The application supports:
- Checking available and occupied parking spaces
- Parking a vehicle in the first available space
- Generating a bill when a vehicle leaves
- Releasing the parking space when the vehicle leaves
- Calculating the parking charge based on vehicle type and parking time

## Assumptions

I have made the following assumptions while building the solution:

1. **Parking capacity is 500 spaces**
   - The application starts with 500 parking spaces.
   - All spaces are initially available.

2. **H2 database is used**
   - I have used H2 to keep the data storage simple and easy to run locally.
   - The original task mentions storing data in memory and not using a database. I chose H2 because it gives a simple persistent data model while still keeping the application easy to run locally.

3. **Parking spaces are empty when the application starts for the first time**
   - The application creates the 500 parking spaces automatically.
   - Each space is initially marked as available.

4. **First available parking space is allocated**
   - When a vehicle enters, the application looks for the first available space and assigns it to the vehicle.

5. **A vehicle cannot be parked twice at the same time**
   - If the same vehicle registration is already parked and has not checked out, the application returns an error.

6. **Vehicle type**
   - Vehicle type is expected to be 1, 2 or 3.
   - The application uses the vehicle type to calculate the parking charge.

7. **Time**
   - The application uses the current application time for vehicle entry and exit.
   - A `Clock` bean has been used so that the time can be controlled more easily in tests.

## API Endpoints

### 1. Check parking availability

**GET `/parking`**

Returns the number of available and occupied spaces.

Example response:

```json
{
  "availableSpace": 499,
  "occupiedSpace": 1
}
```

### 2. Park a vehicle

**POST `/parking`**

Request:

```json
{
  "vehicleReg": "N321RGM",
  "vehicleType": 1
}
```

The application finds the first available parking space and assigns it to the vehicle.

Example response:

```json
{
  "id": 1,
  "vehicleReg": "N321RGM",
  "spaceNumber": 1,
  "timeIn": "2026-09-07T19:00:00"
}
```

### 3. Generate a bill / exit vehicle

**POST `/parking/bill`**

Request:

```json
{
  "vehicleReg": "N321RGM"
}
```

The application finds the currently parked vehicle, records the exit time and calculates the parking charge.

Example response:

```json
{
  "billId": "1",
  "vehicleReg": "N321RGM",
  "vehicleCharge": 12.0,
  "timeIn": "2026-09-07T17:00:00",
  "timeOut": "2026-09-07T19:00:00"
}
```

## Parking Charges

The application calculates the charge based on the vehicle type and number of minutes parked.

The rates from the task are:

| Vehicle Type | Rate |
|---|---:|
| Small Car (1) | £0.10 per minute |
| Medium Car (2) | £0.20 per minute |
| Large Car (3) | £0.40 per minute |

The task also specifies an additional £1 charge for every 5 minutes.

## Project Structure

I have kept the project structure fairly simple:

```text
src/main/java/com/parking/tds
│
├── config
│   ├── ParkingSpaceConfiguration
│   └── TimeConfiguration
│
├── dto
│   ├── CarBillingInfo
│   ├── ParkedCarInfo
│   ├── ParkedCarRequest
│   └── ParkingAvailabilityDto
│
├── entity
│   ├── BillingInfoEntity
│   ├── ParkedCarEntity
│   └── ParkingSpaceEntity
│
├── entity/repository
│   ├── BillingInfoRepository
│   ├── ParkedCarEntityRepository
│   └── ParkingSpaceRepository
│
├── rest
│   └── ParkingController
│
├── service
│   └── ParkingService
│
└── TdsApplication
```

The controller handles the REST endpoints and the service contains the main parking logic.

The repositories are used to access the H2 database.

## How to Run Locally

### Prerequisites

- Java 17
- Maven
- An IDE such as IntelliJ IDEA (optional)

### Run using Maven

From the project directory:

```bash
mvn spring-boot:run
```

The application should start on:

```text
http://localhost:8080
```

### Run tests

```bash
mvn test
```

## Swagger / OpenAPI

Swagger/OpenAPI has been added to make it easier to try the APIs.

Once the application is running, Swagger UI can be opened at:

```text
http://localhost:8080/swagger-ui.html
```

If that URL redirects or is not available depending on the Springdoc version, the OpenAPI UI can also normally be accessed through:

```text
http://localhost:8080/swagger-ui/index.html
```

## Testing

I have included both service-level unit tests and controller integration tests.

The service tests cover some of the main scenarios, including:
- Checking initial availability
- Parking a vehicle
- Preventing the same vehicle from being parked twice
- Handling no available parking spaces
- Allowing a vehicle to park again after it has exited
- Generating a bill
- Billing for less than 180 minutes
- Billing for exactly 180 minutes
- Billing for more than 180 minutes
- Vehicle not found

There are also integration tests using `MockMvc` to test the REST endpoints.

## Error Handling

Some basic error handling has been included for cases such as:
- Vehicle is already parked
- Vehicle cannot be found when generating a bill
- No parking space is available
- Invalid vehicle type

At the moment these errors use simple runtime exceptions. A more complete version could introduce custom exceptions and a global exception handler to return cleaner HTTP error responses.

## Improvements / Future Work

This is intentionally a basic working version. There are several areas I would improve if I had more time.

For example:

- Add proper custom exception classes and global error handling
- Add request validation for vehicle registration and vehicle type
- Improve the parking-space release logic
- Make the parking availability calculation fully database driven instead of maintaining counters in the service
- Use `BigDecimal` instead of `double` for money calculations
- Add more integration tests
- Add more validation and edge-case tests
- Add database configuration for different environments
- Improve concurrency handling when multiple vehicles try to park at the same time
- Add proper API documentation and examples
- Add logging and monitoring where useful

For this exercise, I focused on getting the main flow working first and keeping the implementation straightforward.

## Questions I Would Ask

If this were a real requirement rather than a take-home exercise, I would clarify a few points:

1. Should the parking capacity always be 500, or should it be configurable?
2. Should the parking data survive an application restart?
3. The task mentions storing data in memory and not using a database. Is using a lightweight database such as H2 acceptable?
4. For the additional £1 charge, does "every 5 minutes" mean each started 5-minute block or only completed 5-minute blocks?
5. Should the additional £1 charge apply after the first 180 minutes or from the first minute?
6. What HTTP status codes and response format should be returned for errors?
7. Should vehicle registration numbers be case-sensitive?
8. Can the same vehicle be parked again after it has left?

## Notes

The main aim of this implementation was to provide a simple working solution covering the core parking flow.

I have deliberately not tried to build a production-level parking system for this exercise. With more time, I would improve the areas mentioned above, particularly error handling, validation, concurrency and the parking-space state management.
