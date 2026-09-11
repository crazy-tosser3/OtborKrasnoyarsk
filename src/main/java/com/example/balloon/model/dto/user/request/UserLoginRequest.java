package com.example.balloon.model.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
public class UserLoginRequest {
    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_password")
    @ToString.Exclude
    private String userPassword;
}