package com.parking.tds.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ParkingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAvailableSpaces_shouldReturn200() throws Exception {

        mockMvc.perform(get("/parking"))
                .andExpect(status().isOk());
    }

    @Test
    void parkCar_shouldReturn200() throws Exception {

        String request = """
            {
                "vehicleReg": "N321RGM",
                "vehicleType": 1
            }
            """;

        mockMvc.perform(
                        post("/parking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk());
    }

    @Test
    void generateBill_shouldReturn200() throws Exception {

        String parkRequest = """
            {
                "vehicleReg": "N321RGM",
                "vehicleType": 1
            }
            """;

        // First park the car
        mockMvc.perform(
                        post("/parking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(parkRequest)
                )
                .andExpect(status().isOk());

        // Then generate the bill
        mockMvc.perform(
                        post("/parking/bill")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(parkRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleReg").value("N321RGM"))
                .andExpect(jsonPath("$.billId").exists())
                .andExpect(jsonPath("$.timeIn").exists())
                .andExpect(jsonPath("$.timeOut").exists())
                .andExpect(jsonPath("$.vehicleCharge").exists());
    }
}
