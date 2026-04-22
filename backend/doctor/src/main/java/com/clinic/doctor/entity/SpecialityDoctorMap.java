package com.clinic.doctor.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "speciality_doctor_map")
@IdClass(SpecialityDoctorMapId.class)
public class SpecialityDoctorMap {

    @Id
    @Column(name = "speciality_id")
    private Integer specialityId;

    @Id
    @Column(name = "doctor_id")
    private Integer doctorId;

    public SpecialityDoctorMap() {}

    public Integer getSpecialityId() {
        return specialityId;
    }

    public void setSpecialityId(Integer specialityId) {
        this.specialityId = specialityId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }
}
