package com.example.demo;

/*
Cả hai annotation @SpringBootTest và @WebMvcTest đều là các annotation
được sử dụng trong unit testing của ứng dụng Spring Boot.

Tuy nhiên, @SpringBootTest được sử dụng để tạo ra một integration test với toàn
bộ ứng dụng Spring Boot, bao gồm cả các bean, component và configuration của ứng dụng.
Điều này đảm bảo rằng tất cả các phần của ứng dụng đang hoạt động chính xác như
mong đợi và tích hợp tốt với nhau.

Ngược lại, @WebMvcTest chỉ tạo ra một test cho một phần của ứng dụng, đó là các controller,
mà không cần phải cấu hình hoặc khởi động toàn bộ ứng dụng. Khi sử dụng @WebMvcTest,
Spring Boot sẽ tạo ra một môi trường test riêng biệt chỉ với một phần của ứng dụng,
giả lập một request tới các controller và kiểm tra kết quả trả về.

Vì vậy, @SpringBootTest thường được sử dụng để 'kiểm tra tích hợp và chức năng hoạt động của
toàn bộ ứng dụng, trong khi @WebMvcTest được sử dụng để kiểm tra phần controller của ứng dụng.
*/

import com.example.demo.controller.PatientRecordController;
import com.example.demo.entity.PatientRecord;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.PatientRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;

import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
Sự lựa chọn giữa @SpringBootTest và @WebMvcTest phụ thuộc vào mục đích và phạm vi của bài kiểm tra.
- Nếu bạn muốn kiểm tra toàn bộ ứng dụng Spring Boot, bao gồm cả cấu hình, tầng Service và
tầng Persistence thì @SpringBootTest là sự lựa chọn tốt nhất.
- Nếu bạn muốn kiểm tra tầng Web và các controller liên quan, thì @WebMvcTest là lựa chọn tốt hơn.
Việc sử dụng @WebMvcTest cũng giúp cho bài kiểm tra nhanh hơn và dễ dàng để chạy.
Tuy nhiên, bạn cũng có thể sử dụng cả hai annotations để kiểm tra toàn bộ ứng dụng Spring Boot
cũng như các controller liên quan đến tầng Web.
* */

/* SOURCE : https://stackabuse.com/guide-to-unit-testing-spring-boot-rest-apis/ */
@WebMvcTest(PatientRecordController.class)
public class PatientRecordControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    PatientRecordRepository patientRecordRepository;

    PatientRecord RECORD_1 = new PatientRecord(1l, "Rayven Yor", 23, "Cebu Philippines");
    PatientRecord RECORD_2 = new PatientRecord(2l, "David Landup", 27, "New York USA");
    PatientRecord RECORD_3 = new PatientRecord(3l, "Jane Doe", 31, "New York USA");

    /* ______________________________________*/
    /* Unit Testing the GET Request Handlers */
    /* ______________________________________*/
    @Test
    public void getAllRecords_success() throws Exception {

        List<PatientRecord> records = new ArrayList<>(Arrays.asList(RECORD_1, RECORD_2, RECORD_3));

        /* The Mockito when().thenReturn() chain method mocks the getAllRecords() method call in the JPA repository,
        so every time the method is called within the controller, it will return the specified value in
        the parameter of the thenReturn() method. In this case, it returns a list of three preset patient records,
        instead of actually making a database call. */
        Mockito.when(patientRecordRepository.findAll()).thenReturn(records);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/patient")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].name").value("Jane Doe"));

    }

    @Test
    public void getPatientById_success() throws Exception {
        Mockito.when(patientRecordRepository.findById(RECORD_1.getPatientId())).thenReturn(java.util.Optional.of(RECORD_1));

        mockMvc.perform(MockMvcRequestBuilders
                .get("/patient/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Rayven Yor"));
//                .andExpect(jsonPath("$", notNullValue()))
//                .andExpect(jsonPath("$.name", is("Rayven Yor")));
    }

    /* ______________________________________ */
    /* Unit Testing the POST Request Handlers */
    /* ______________________________________ */

    @Test
    public void createRecord_success() throws Exception {
        PatientRecord record = PatientRecord.builder()
                .name("John Doe")
                .age(47)
                .address("New York USA")
                .build();

        Mockito.when(patientRecordRepository.save(record)).thenReturn(record);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(record));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("John Doe"));
    }

    /* _____________________________________ */
    /* Unit Testing the PUT Request Handlers */
    /* _____________________________________ */

    @Test
    public void updatePatientRecord_success() throws Exception {
        PatientRecord updatedRecord = PatientRecord.builder()
                .patientId(1l)
                .name("Rayven Zambo")
                .age(23)
                .address("Cebu Philippines")
                .build();

        Mockito.when(patientRecordRepository.findById(RECORD_1.getPatientId())).thenReturn(Optional.of(RECORD_1));
        Mockito.when(patientRecordRepository.save(updatedRecord)).thenReturn(updatedRecord);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Rayven Zambo"));
    }

    @Test
    public void updatePatientRecord_nullId() throws Exception {
        PatientRecord updatedRecord = PatientRecord.builder()
                .name("Sherlock Holmes")
                .age(40)
                .address("221B Baker Street")
                .build();

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof InvalidRequestException))
                .andExpect(result ->
                        assertEquals("PatientRecord or ID must not be null!", result.getResolvedException().getMessage()));
    }

    @Test
    public void updatePatientRecord_recordNotFound() throws Exception {
        PatientRecord updatedRecord = PatientRecord.builder()
                .patientId(5l)
                .name("Sherlock Holmes")
                .age(40)
                .address("221B Baker Street")
                .build();

        Mockito.when(patientRecordRepository.findById(updatedRecord.getPatientId())).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(result ->
                        assertEquals("Patient with ID 5 does not exist.", result.getResolvedException().getMessage()));
    }

    /* _____________________________________ */
    /* Unit Testing the PUT Request Handlers */
    /* _____________________________________ */

    @Test
    public void deletePatientById_success() throws Exception {
        Mockito.when(patientRecordRepository.findById(RECORD_2.getPatientId())).thenReturn(Optional.of(RECORD_2));

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/patient/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void deletePatientById_notFound() throws Exception {

        Mockito.when(patientRecordRepository.findById(5l)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/patient/5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(result ->
                        assertEquals("Patient with ID 5 does not exist.", result.getResolvedException().getMessage()));
    }

}