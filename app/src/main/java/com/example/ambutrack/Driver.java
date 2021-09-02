package com.example.ambutrack;

public class Driver {
    public String name,contactNumber,gender,rcNumber,licenseNumber,mailId,password,repassword;

    public Driver(){

    }
    public Driver(String name, String contactNumber, String gender, String rcNumber, String licenseNumber, String mailId, String password, String repassword) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.gender = gender;
        this.rcNumber = rcNumber;
        this.licenseNumber = licenseNumber;
        this.mailId = mailId;
        this.password = password;
        this.repassword = repassword;
    }
}
