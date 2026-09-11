package com.example.balloon.model.dto.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterResponse {
    @JsonProperty("message")
    private String message;
}