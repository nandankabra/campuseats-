package com.example.campuseatshttp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchOrderRequest {

    private String status;
    private String address;

    public PatchOrderRequest() {
    }

    public PatchOrderRequest(String status, String address) {
        this.status = status;
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
