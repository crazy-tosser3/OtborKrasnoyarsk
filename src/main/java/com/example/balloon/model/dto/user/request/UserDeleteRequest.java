package com.example.balloon.model.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserDeleteRequest {
    @JsonProperty("username")
    private String username;

    @JsonProperty("user_password")
    private String userPassword;
}
