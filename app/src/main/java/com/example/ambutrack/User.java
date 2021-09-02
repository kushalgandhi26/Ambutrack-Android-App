package com.example.ambutrack;

public class User {
    public String username,number,dob,bloodtype,gender,address,mailid,password,repassword,avatar;

    public User(){

    }

    public User(String username, String number, String dob, String bloodtype, String gender, String address, String mailid, String password, String repassword) {
        this.username = username;
        this.number = number;
        this.dob = dob;
        this.bloodtype = bloodtype;
        this.gender = gender;
        this.address = address;
        this.mailid = mailid;
        this.password = password;
        this.repassword = repassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}