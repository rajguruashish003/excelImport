package com.example.demo.dto;

public class LastMatchScoreDTO {
    long matchNumber;
    long points;
    long playerId;
    boolean isDreamTeam;

    public long getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(long matchNumber) {
        this.matchNumber = matchNumber;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public boolean isDreamTeam() {
        return isDreamTeam;
    }

    public void setDreamTeam(boolean dreamTeam) {
        isDreamTeam = dreamTeam;
    }
}
