package com.example.balloon.model.dto.user.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String id;
    private String username;
    private String userEmail;
}
