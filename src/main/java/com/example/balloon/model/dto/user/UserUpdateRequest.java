package com.example.balloon.model.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
public class UserUpdateRequest {
    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_password")
    @ToString.Exclude
    private String userPassword;

    @JsonProperty("new_user_name")
    private String newUserName;

    @JsonProperty("new_user_password")
    @ToString.Exclude
    private String newUserPassword;
}