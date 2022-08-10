package com.example.serveresmfamil;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.BroadcastAckCallback;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

import com.example.serveresmfamil.Models.Client;
import com.example.serveresmfamil.Models.Game;
import com.example.serveresmfamil.Models.GameInfo;
import com.example.serveresmfamil.Models.Player;
import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import javafx.application.Application;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Server extends Application {

    private static final int DEFAULT_PORT = 9999;

    public static final List<Game> games = new ArrayList<>();


    @Override
    public void start(Stage stage) throws IOException {

    }

    private static int getGameIndexById(int id) {
        for (int i = 0; i < games.size(); i++)
            if (games.get(i).getId() == id)
                return i;

        return -1;
    }

    private static String getCreatorUserName(int gameId) {
        for (Game game : games)
            if (game.getId() == gameId)
                return game.getName();
        return null;
    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setPort(DEFAULT_PORT);
        SocketIOServer server = new SocketIOServer(configuration);

        server.addConnectListener(socketIOClient -> {
            Client client = new Client(socketIOClient, "");
            System.out.println("New Client Has Been Connected !");
            System.out.println(client.getClient().getSessionId());
        });


        server.addEventListener("createGame", String.class, (socketIOClient, game, ackRequest) -> {


            System.out.println(socketIOClient.getSessionId());

            Gson gson = new Gson();
            Game newGame = gson.fromJson(game, Game.class);
            newGame.setId(games.size());

            Player creator = new Player();
            creator.setSession_id(socketIOClient.getSessionId());
            creator.setUser_name(newGame.getName());
            creator.setRole("CREATOR");
            creator.setSocketIOClient(socketIOClient);

            newGame.getPlayers().add(creator);

            creator.getSocketIOClient().joinRoom(creator.getUser_name() + "_" + newGame.getId());

            games.add(newGame);

            System.out.println("New Game Created : " + newGame);
            System.out.println("------------------------");
            JSONObject response = new JSONObject();
            Gson gson1 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            response.put("status", "ok");
            response.put("game_id", newGame.getId());
            response.put("game_model", gson1.toJson(newGame));
            response.put("role", "CREATOR");
            response.put("session_id", socketIOClient.getSessionId().toString());

            ackRequest.sendAckData(response.toString());


        });

        server.addEventListener("getLiveGames", String.class, (socketIOClient, s, ackRequest) -> {
            System.out.println("Getting Live Games");
            ArrayList<GameInfo> gameInfos = new ArrayList<>();
            for (Game game : games) {
                gameInfos.add(new GameInfo(game.getName(), game.getId()));
            }
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            ackRequest.sendAckData(gson.toJson(games));

        });


        server.addEventListener("joinGame", String.class, (socketIOClient, s, ackRequest) -> {

            JSONObject playerInfo = new JSONObject(s);


            Player player = new Player();
            player.setSession_id(socketIOClient.getSessionId());
            player.setUser_name(playerInfo.getString("user_name"));
            player.setRole("PLAYER");
            player.setSocketIOClient(socketIOClient);

            games.get(getGameIndexById(playerInfo.getInt("game_id"))).getPlayers().add(player);
            String gameRoom = getCreatorUserName(playerInfo.getInt("game_id")) + "_" + playerInfo.getInt("game_id");
            Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
            server.getRoomOperations(gameRoom).sendEvent("updatePlayerList", gson.toJson(games.get(getGameIndexById(playerInfo.getInt("game_id"))), Game.class));
            player.getSocketIOClient().joinRoom(gameRoom);

            System.out.println("Player " + playerInfo.getString("user_name") + " Joined the game " + playerInfo.getString("game_name"));
            System.out.println("------------------------");
            Gson gson1 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            response.put("game_id", games.get(getGameIndexById(playerInfo.getInt("game_id"))).getId());
            response.put("game_model", gson1.toJson(games.get(getGameIndexById(playerInfo.getInt("game_id")))));
            response.put("session_id", socketIOClient.getSessionId().toString());
            ackRequest.sendAckData(response.toString());

        });

        server.addEventListener("gameInfo", String.class, (socketIOClient, s, ackRequest) -> {
            Game wantedGame = games.get(getGameIndexById(Integer.parseInt(s)));
            System.out.println("gameInfo Fired: " + wantedGame.getName());

            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
                ackRequest.sendAckData(gson.toJson(wantedGame), socketIOClient.getSessionId().equals(wantedGame.getCreator().getSession_id()) ? "CREATOR" : "PLAYER");
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            System.out.println("------------------------");

        });

        server.addEventListener("startGame", String.class, (socketIOClient, s, ackRequest) -> {
            Game game = games.get(getGameIndexById(Integer.parseInt(s)));
            String gameRoom = getCreatorUserName(Integer.parseInt(s)) + "_" + s;
            GameHandler gameHandler = new GameHandler(game, server, gameRoom);
            gameHandler.init();
            Thread gameThread = new Thread(gameHandler, "gameId_" + s);
            gameThread.start();

            server.getRoomOperations(gameRoom).sendEvent("goToChooseCharacterView");
        });

        server.addDisconnectListener(socketIOClient -> {
            System.out.println("Client Has Been Disconnected !");
            System.out.println("------------------------");
            boolean playerIsCreator = false;
            Game created = null;
            for (Game game: games) {
                for (Player player: game.getPlayers()) {
                    if(player.getSession_id().equals(socketIOClient.getSessionId()) && player.getRole().equals("CREATOR")){
                        playerIsCreator = true;
                        created = game;
                        break;
                    }
                }
                if(playerIsCreator)
                    break;
            }
            String gameRoom = getCreatorUserName(created.getId()) + "_" + created.getId();
            if (playerIsCreator) {
                games.remove(created);
                server.getRoomOperations(gameRoom).sendEvent("removeGame");
                server.getRoomOperations(gameRoom).getClients().clear();
                server.getRoomOperations(gameRoom).disconnect();
            }
        });


        server.start();
    }
}