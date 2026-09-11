package com.example.balloon.model.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @JsonProperty("username")
    private String username;

    @JsonProperty("user_password")
    private String userPassword;

    @JsonProperty("new_username")
    private String newUsername;

    @JsonProperty("new_user_password")
    private String newUserPassword;
}
