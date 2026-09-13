package com.example.balloon.model.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameHistoryResponse {
    private String id;
    @JsonProperty("user_name")
    private String userName;
    private Integer score;
    @JsonProperty("played_at")
    private String playedAt;
    @JsonProperty("is_success")
    private Boolean isSuccess;
}
