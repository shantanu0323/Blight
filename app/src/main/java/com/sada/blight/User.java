package com.sada.blight;

/**
 * Created by Shantanu on 3/19/2018.
 */

public class User {
    private String name;
    private String email;
    private String bloodgroup;
    private String contact;;
    private String emergency_contact;
    private String device_token;
    private String profile_pic;
    private String password;

    public User() {
    }

    public User(String name, String email, String bloodgroup, String contact, String emergency_contact, String device_token, String profile_pic, String password) {
        this.name = name;
        this.email = email;
        this.bloodgroup = bloodgroup;
        this.contact = contact;
        this.emergency_contact = emergency_contact;
        this.device_token = device_token;
        this.profile_pic = profile_pic;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBloodgroup() {
        return bloodgroup;
    }

    public void setBloodgroup(String bloodgroup) {
        this.bloodgroup = bloodgroup;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmergency_contact() {
        return emergency_contact;
    }

    public void setEmergency_contact(String emergency_contact) {
        this.emergency_contact = emergency_contact;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
