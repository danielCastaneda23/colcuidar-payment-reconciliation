package com.reconcilitation.reconcilitation.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.reconcilitation.reconcilitation.controllers.mappers.ScheduleMapper;
import com.reconcilitation.reconcilitation.model.interfaces.IScheduleReader;

/**
 * Tests ScheduleController in isolation: the reader and mapper are mocks,
 * so only the HTTP behaviour of the controller is checked.
 */
@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc; //allow us to run client request to our mvc server

    @MockitoBean
    private IScheduleReader reader; //creates isolated mock instance for testing purposes

    @MockitoBean 
    private ScheduleMapper mapper; //creates isolated mock instance for testing purposes

    private static MockMultipartFile csvFile() {
        String csv = "FECHA,B,C,D,E,TURNO,,AUX,I,J,CUMPLIDO,L,M,N,O,VALOR\n"
                + "1/10/26,P,C,D,E,DIA,,PEREZ ANA,,,SI,,,,,95000\n";
        return new MockMultipartFile("file", "october.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void uploadReturns201AndCallsReaderAndMapper() throws Exception {
        mockMvc.perform(multipart("/api/schedule/upload").file(csvFile()))
                .andExpect(status().isCreated());

        verify(reader).result(any(InputStream.class));
        verify(mapper).uploadResponse(any());
    }

    @Test
    void uploadReturns500WhenTheReaderFails() throws Exception {
        when(reader.result(any(InputStream.class)))
                .thenThrow(new RuntimeException("exception throw"));

        mockMvc.perform(multipart("/api/schedule/upload").file(csvFile()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void uploadReturns400WhenThereIsNoFile() throws Exception {
        mockMvc.perform(multipart("/api/schedule/upload"))
                .andExpect(status().isBadRequest());
    }
}
