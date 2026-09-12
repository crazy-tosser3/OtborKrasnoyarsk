package com.example.balloon.model.dto.user;

import com.example.balloon.model.enums.RoleEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponse {
    @JsonProperty("token")
    private String token;

    @JsonProperty("role")
    private RoleEnum role;
}