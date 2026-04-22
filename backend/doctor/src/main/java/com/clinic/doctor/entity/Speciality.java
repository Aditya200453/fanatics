package com.clinic.doctor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "speciality")
public class Speciality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "speciality_id")
    private Integer specialityId;

    @Column(nullable = false, unique = true)
    private String name;

    public Speciality() {}

    public Integer getSpecialityId() {
        return specialityId;
    }

    public void setSpecialityId(Integer specialityId) {
        this.specialityId = specialityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}