package com.example.smilescare.Model;

public class User {
    private String Name;
    private String Password;
    private String Phone;
    private String IsVolunteer;
    private String secureCode;
    private String Email;

    public User() {
    }

    public User(String name, String password, String secureCode, String email) {
        Name = name;
        Password = password;
        IsVolunteer = "false";
        this.secureCode = secureCode;
        Email = email;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getIsVolunteer() {
        return IsVolunteer;
    }

    public void setIsVolunteer(String isVolunteer) {
        IsVolunteer = isVolunteer;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }
}
