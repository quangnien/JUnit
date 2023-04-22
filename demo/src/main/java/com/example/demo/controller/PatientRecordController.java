package com.example.demo.controller;

import com.example.demo.entity.PatientRecord;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.PatientRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/patient")
public class PatientRecordController {

    @Autowired
    PatientRecordRepository patientRecordRepository;
    // CRUD methods to be added

    @GetMapping
    public List<PatientRecord> getAllRecords() {
        return patientRecordRepository.findAll();
    }

    @GetMapping(value = "{patientId}")
    public PatientRecord getPatientById(@PathVariable(value="patientId") Long patientId) {
        return patientRecordRepository.findById(patientId).get();
    }

    @PostMapping
    public PatientRecord createRecord(@RequestBody @Valid PatientRecord patientRecord) {
        return patientRecordRepository.save(patientRecord);
    }

    @PutMapping
    public PatientRecord updatePatientRecord(@RequestBody PatientRecord patientRecord) throws NotFoundException {
        if (patientRecord == null || patientRecord.getPatientId() == null) {
            throw new InvalidRequestException("PatientRecord or ID must not be null!");
        }
        Optional<PatientRecord> optionalRecord = patientRecordRepository.findById(patientRecord.getPatientId());
        if (optionalRecord.isPresent() == false) {
            throw new NotFoundException("Patient with ID " + patientRecord.getPatientId() + " does not exist.");
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient with ID " + patientRecord.getPatientId() + " does not exist.");
        }
        PatientRecord existingPatientRecord = optionalRecord.get();

        existingPatientRecord.setName(patientRecord.getName());
        existingPatientRecord.setAge(patientRecord.getAge());
        existingPatientRecord.setAddress(patientRecord.getAddress());

        return patientRecordRepository.save(existingPatientRecord);
    }

    @DeleteMapping(value = "{patientId}")
    public void deletePatientById(@PathVariable(value = "patientId") Long patientId) throws NotFoundException {
        if (patientRecordRepository.findById(patientId).isPresent() == false) {
            throw new NotFoundException("Patient with ID " + patientId + " does not exist.");
        }
        patientRecordRepository.deleteById(patientId);
    }

}