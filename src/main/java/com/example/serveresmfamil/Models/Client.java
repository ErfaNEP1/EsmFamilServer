package com.example.serveresmfamil.Models;


import com.corundumstudio.socketio.SocketIOClient;

public class Client {
    private SocketIOClient client;
    private String name;

    public Client() {
    }

    public Client(SocketIOClient client, String name) {
        this.client = client;
        this.name = name;
    }

    public SocketIOClient getClient() {
        return client;
    }

    public void setClient(SocketIOClient client) {
        this.client = client;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
