package com.skillswap.dto;

public class MemberResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Integer timeCreditBalance;

    public MemberResponse() {
    }

    public MemberResponse(
            Long id,
            String name,
            String email,
            String phone,
            Integer timeCreditBalance) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.timeCreditBalance = timeCreditBalance;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getTimeCreditBalance() {
        return timeCreditBalance;
    }
}