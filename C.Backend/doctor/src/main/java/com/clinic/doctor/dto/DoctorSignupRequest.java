package com.clinic.doctor.dto;

public class DoctorSignupRequest {

    private String name;
    private String email;
    private String phone;
    private Integer experience;
    private String qualification;

    //  NEW FIELD
    private String password;

    // getters & setters

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
