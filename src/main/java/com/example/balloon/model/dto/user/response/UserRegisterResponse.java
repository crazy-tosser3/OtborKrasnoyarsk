package com.example.balloon.model.dto.user.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterResponse {
    private String id;
    private String userName;
    private String userEmail;
}
