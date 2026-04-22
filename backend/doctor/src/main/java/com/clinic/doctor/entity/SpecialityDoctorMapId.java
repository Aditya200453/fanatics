package com.clinic.doctor.entity;

import java.io.Serializable;
import java.util.Objects;

public class SpecialityDoctorMapId implements Serializable {

    private Integer specialityId;
    private Integer doctorId;

    public SpecialityDoctorMapId() {}

    public SpecialityDoctorMapId(Integer specialityId, Integer doctorId) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpecialityDoctorMapId)) return false;
        SpecialityDoctorMapId that = (SpecialityDoctorMapId) o;
        return Objects.equals(specialityId, that.specialityId) &&
                Objects.equals(doctorId, that.doctorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specialityId, doctorId);
    }
}
