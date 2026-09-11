package com.example.balloon.model.dto.user.request;

import com.example.balloon.model.enums.RoleEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("role")
    private RoleEnum role;
}