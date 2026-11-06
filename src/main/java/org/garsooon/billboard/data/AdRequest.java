package org.garsooon.billboard.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdRequest {

    private UUID playerId;
    private String playerName;
    private String message;
    private int durationDays;

    public AdRequest(UUID playerId, String playerName, String message, int durationDays) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.message = message;
        this.durationDays = durationDays;
    }

    public AdRequest(Map<String, Object> map) {
        this.playerId = UUID.fromString(String.valueOf(map.get("playerId")));
        this.playerName = String.valueOf(map.get("playerName"));
        this.message = String.valueOf(map.get("message"));

        Object durationObj = map.get("durationDays");
        this.durationDays = durationObj instanceof Number ? ((Number) durationObj).intValue() : 0;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("playerId", playerId.toString());
        map.put("playerName", playerName);
        map.put("message", message);
        map.put("durationDays", durationDays);
        return map;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMessage() {
        return message;
    }

    public int getDurationDays() {
        return durationDays;
    }
}