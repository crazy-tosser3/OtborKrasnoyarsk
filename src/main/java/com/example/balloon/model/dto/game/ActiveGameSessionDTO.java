package com.example.balloon.model.dto.game;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ActiveGameSessionDTO {
    private String id;
    private String userName;
    private long startedAt;
    private String secret;
}
