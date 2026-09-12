package com.example.balloon.model.dto.reward;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RewardResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("claimed")
    private boolean claimed;

    @JsonProperty("user_name")
    private String userName;
}