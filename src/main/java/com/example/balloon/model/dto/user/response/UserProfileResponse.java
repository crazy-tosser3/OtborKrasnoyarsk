package com.example.balloon.model.dto.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_email")
    private String userEmail;
}