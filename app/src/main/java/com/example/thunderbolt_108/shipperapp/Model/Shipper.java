package com.example.thunderbolt_108.shipperapp.Model;

public class Shipper {

    private String name,phone,password;

    public Shipper() {

    }

    public Shipper(String name,String phone,String password) {

        this.name=name;
        this.phone=phone;
        this.password=password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }
}

