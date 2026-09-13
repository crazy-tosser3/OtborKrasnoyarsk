package com.example.balloon.model.dto.reward;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RewardRequest {
    private String name;
    private Boolean claimed;
    private String userName;
}
