package com.example.serveresmfamil.Models;

import com.corundumstudio.socketio.SocketIOClient;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.UUID;

public class Player {
    @Expose
    private String user_name;
    @Expose(serialize = false)
    private UUID session_id;
    @Expose
    private Game current_game;
    @Expose
    private String role = "player";
    @Expose(serialize = false)
    private SocketIOClient socketIOClient;
    @Expose
    private ArrayList<RoundData> roundData;
    @Expose
    private int roundScore = 0;

    public Player() {

    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public UUID getSession_id() {
        return session_id;
    }

    public void setSession_id(UUID session_id) {
        this.session_id = session_id;
    }

    public Game getCurrent_game() {
        return current_game;
    }

    public void setCurrent_game(Game current_game) {
        this.current_game = current_game;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public SocketIOClient getSocketIOClient() {
        return socketIOClient;
    }

    public void setSocketIOClient(SocketIOClient socketIOClient) {
        this.socketIOClient = socketIOClient;
    }

    public ArrayList<RoundData> getRoundData() {
        return roundData;
    }

    public void setRoundData(ArrayList<RoundData> roundData) {
        this.roundData = roundData;
    }

    public int getRoundScore() {
        return roundScore;
    }

    public void setRoundScore(int roundScore) {
        this.roundScore = roundScore;
    }

    public void addScore(int score){
        this.roundScore += score;
    }
}
