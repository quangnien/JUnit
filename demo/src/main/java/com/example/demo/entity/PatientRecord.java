package com.example.demo.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long patientId;

    @NotNull
    @Column
    private String name;

    @NotNull
    @Column
    private Integer age;

    @NotNull
    @Column
    private String address;
}