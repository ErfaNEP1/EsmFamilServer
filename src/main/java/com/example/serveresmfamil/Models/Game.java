package com.example.serveresmfamil.Models;

import com.example.serveresmfamil.Models.GameFields;
import com.example.serveresmfamil.Models.Player;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Game {
    @Expose
    private String name;
    @Expose
    private int id;
    @Expose
    private GameMode gameMode;
    @Expose
    private int gameTimeSeconds;
    @Expose
    private int gameRoundCount;
    @Expose
    private GameFields[] gameFields;
    @Expose
    private final ArrayList<Player> players = new ArrayList<>();

    @Expose
    private final List<String> characters = new ArrayList<>();

    public Game() {

    }

    public Game(String name, int id, GameFields[] gameFields) {
        this.name = name;
        this.id = id;
        this.gameFields = gameFields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public GameFields[] getGameFields() {
        return gameFields;
    }

    public void setGameFields(GameFields[] gameFields) {
        this.gameFields = gameFields;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Player getCreator(){
        for (Player player: players) {
            if (player.getRole().equals("CREATOR"))
                return player;
        }
        return null;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public int getGameRoundCount() {
        return gameRoundCount;
    }

    public void setGameRoundCount(int gameRoundCount) {
        this.gameRoundCount = gameRoundCount;
    }

    public int getGameTimeSeconds() {
        return gameTimeSeconds;
    }

    public void setGameTimeSeconds(int gameTimeSeconds) {
        this.gameTimeSeconds = gameTimeSeconds;
    }

    public List<String> getCharacters() {
        return characters;
    }
}
