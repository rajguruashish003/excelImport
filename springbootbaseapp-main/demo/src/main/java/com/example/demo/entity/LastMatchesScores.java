package com.example.demo.entity;

import com.example.demo.dto.LastMatchScoreDTO;

import javax.naming.Name;
import javax.persistence.*;

@Entity
@Table(name = "last_matches_scores")
public class LastMatchesScores {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private  long id;

    @Column(name = "match_numer")
    private long matchNumer;

    @Column(name = "points")
    private long points;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private PlayersList player;

    @Column(name = "dream_team")
    boolean isDreamTeam;

    public long getMatchNumer() {
        return matchNumer;
    }

    public void setMatchNumer(long matchNumer) {
        this.matchNumer = matchNumer;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public PlayersList getPlayer() {
        return player;
    }

    public void setPlayer(PlayersList player) {
        this.player = player;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isDreamTeam() {
        return isDreamTeam;
    }

    public void setDreamTeam(boolean dreamTeam) {
        isDreamTeam = dreamTeam;
    }

    public LastMatchScoreDTO toDto(){

        LastMatchScoreDTO lastMatchScoreDTO=new LastMatchScoreDTO();
        lastMatchScoreDTO.setMatchNumber(this.matchNumer);
        lastMatchScoreDTO.setPlayerId(player.getId());
        lastMatchScoreDTO.setPoints(this.points);
        lastMatchScoreDTO.setDreamTeam(this.isDreamTeam);
        return lastMatchScoreDTO;
    }
}
