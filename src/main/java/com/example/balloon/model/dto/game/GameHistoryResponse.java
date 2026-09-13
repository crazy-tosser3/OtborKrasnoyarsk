package com.example.balloon.model.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    /** имя поля намеренно совпадает с JSON-тегом Go-версии (`is_succes`) */
    @JsonProperty("is_succes")
    private Boolean isSuccess;
}
