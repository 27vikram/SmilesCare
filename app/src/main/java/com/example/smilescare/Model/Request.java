package com.example.smilescare.Model;

import java.util.List;

public class Request {
    private String phone;
    private String name;
    private String amount;
    private String message;
    private String volunteerId;
    private String use;
    private String orderId;


    public Request() {
    }

    public Request(String phone, String amount, String message, String volunteerId, String use, String orderId, String name) {
        this.phone = phone;
        this.amount = amount;
        this.message = message;
        this.volunteerId = volunteerId;
        this.use = use;
        this.orderId = orderId;
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(String volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
