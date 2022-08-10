package com.example.serveresmfamil;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.serveresmfamil.Models.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

public class GameHandler implements Runnable {
    private Game game;
    private SocketIOServer server;
    private String roomName;

    private final ArrayList<String> NotChosenCharacters = new ArrayList<>();

    private Player currentPlayer;

    private int countRound = 1;

    private final ArrayList<Player> notChosenPlayers = new ArrayList<>();

    public GameHandler(Game game, SocketIOServer server, String roomName) {
        this.game = game;
        this.server = server;
        this.roomName = roomName;
    }

    public void init() {

        notChosenPlayers.addAll(game.getPlayers());
        NotChosenCharacters.add("الف");
        NotChosenCharacters.add("ب");
        NotChosenCharacters.add("پ");
        NotChosenCharacters.add("ت");
        NotChosenCharacters.add("س");
        NotChosenCharacters.add("ج");
        NotChosenCharacters.add("چ");
        NotChosenCharacters.add("ح");
        NotChosenCharacters.add("خ");
        NotChosenCharacters.add("د");
        NotChosenCharacters.add("ذ");
        NotChosenCharacters.add("ر");
        NotChosenCharacters.add("ز");
        NotChosenCharacters.add("ژ");
        NotChosenCharacters.add("س");
        NotChosenCharacters.add("ش");
        NotChosenCharacters.add("ص");
        NotChosenCharacters.add("ض");
        NotChosenCharacters.add("ط");
        NotChosenCharacters.add("ظ");
        NotChosenCharacters.add("ع");
        NotChosenCharacters.add("غ");
        NotChosenCharacters.add("ف");
        NotChosenCharacters.add("ق");
        NotChosenCharacters.add("ک");
        NotChosenCharacters.add("گ");
        NotChosenCharacters.add("ل");
        NotChosenCharacters.add("م");
        NotChosenCharacters.add("ن");
        NotChosenCharacters.add("و");
        NotChosenCharacters.add("ه");
        NotChosenCharacters.add("ی");
    }

    private Player getRandomPlayer() {
        if (notChosenPlayers.size() < 1) {
            notChosenPlayers.addAll(game.getPlayers());
        }
        if (isCreatorSelected()) {
            int index = (int) (Math.random() * notChosenPlayers.size());
            return notChosenPlayers.get(index);
        } else {
            return getCreator();
        }
    }

    private boolean isCreatorSelected() {
        for (Player player : notChosenPlayers) {
            if (player.getRole().equals("CREATOR"))
                return false;
        }
        return true;
    }

    private Player getCreator() {
        for (Player player : game.getPlayers()) {
            if (player.getRole().equals("CREATOR"))
                return player;
        }
        return null;
    }

    private int getPlayerBySessionId(UUID session_id) {
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);
            if (player.getSession_id().equals(session_id))
                return i;
        }
        return -1;
    }

    private boolean isInArray(String[] stringArray, String searchedValue) {
        for (String x : stringArray) {
            if (x.equals(searchedValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {

        System.out.println("Game " + game.getId() + " started!");
        server.addEventListener("choosePlayer_" + game.getId(), String.class, (socketIOClient, s, ackRequest) -> {
            if (currentPlayer == null) {
                currentPlayer = getRandomPlayer();
                notChosenPlayers.remove(currentPlayer);
            }
            JSONObject response = new JSONObject();
            response.put("chosenPlayerUserName", currentPlayer.getUser_name());
            response.put("areYouChosen", currentPlayer.getSession_id().equals(socketIOClient.getSessionId()));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            response.put("notChosenCharacters", gson.toJson(NotChosenCharacters));
            ackRequest.sendAckData(response.toString());
        });

        server.addEventListener("letterSubmit_" + game.getId(), String.class, (socketIOClient, s, ackRequest) -> {
            NotChosenCharacters.remove(s);
            server.getRoomOperations(roomName).sendEvent("goToGameView", s);
        });

        server.addEventListener("gameStop_" + game.getId(), String.class, (socketIOClient, s, ackRequest) -> {

            for (SocketIOClient soc : server.getRoomOperations(roomName).getClients()) {
                JSONObject data = new JSONObject();
                data.put("stopper",game.getPlayers().get(getPlayerBySessionId(socketIOClient.getSessionId())).getUser_name());
                data.put("gameRound",countRound);
                soc.sendEvent("giveRoundData", new AckCallback<String>(String.class) {
                    @Override
                    public void onSuccess(String s) {

                        Type RoundDataType = new TypeToken<ArrayList<RoundData>>() {
                        }.getType();
                        JSONObject response = new JSONObject(s);
                        ArrayList<RoundData> roundData = new Gson().fromJson(response.getString("roundData"), RoundDataType);
                        game.getPlayers().get(getPlayerBySessionId(UUID.fromString(response.getString("session_id")))).setRoundData(roundData);
                    }
                }, data.toString());
            }

        });

        server.addEventListener("startOtherRound_" + game.getId(), String.class, (socketIOClient, s, ackRequest) -> {
            for (Player player : game.getPlayers()) {
                for (int i = 0; i < player.getRoundData().size(); i++) {
                    RoundData roundData = player.getRoundData().get(i);
                    if (roundData.getGameField() == GameFields.NAME && !isInArray(Dictionary.names, roundData.getData())) {
                        continue;
                    } else if (roundData.getGameField().getName().equals(GameFields.FAMILY_NAME.getName()) && !isInArray(Dictionary.famil, roundData.getData())) {
                        continue;
                    } else if (roundData.getGameField().getName().equals(GameFields.ANIMAL.getName()) && !isInArray(Dictionary.animals, roundData.getData())) {
                        continue;
                    } else if (roundData.getGameField().getName().equals(GameFields.CAR.getName()) && !isInArray(Dictionary.cars, roundData.getData())) {
                        continue;
                    } else if (roundData.getGameField().getName().equals(GameFields.CITY.getName()) && !isInArray(Dictionary.city, roundData.getData())) {
                        continue;
                    } else if (roundData.getGameField().getName().equals(GameFields.CLOTH.getName()) && !isInArray(Dictionary.clothes, roundData.getData())) {
                        continue;
                    } else if (roundData.getGameField().getName().equals(GameFields.COUNTRY.getName()) && !isInArray(Dictionary.country, roundData.getData())) {
                        continue;
                    } else if (roundData.getGameField().getName().equals(GameFields.FLOWER.getName()) && !isInArray(Dictionary.flower, roundData.getData())) {
                        continue;
                    } else if (roundData.getGameField().getName().equals(GameFields.FOOD.getName()) && !isInArray(Dictionary.food, roundData.getData())) {
                        continue;
                    } else if (roundData.getGameField().getName().equals(GameFields.FRUIT.getName()) && !isInArray(Dictionary.fruit, roundData.getData())) {
                        continue;
                    } else if (roundData.getGameField().getName().equals(GameFields.THING.getName()) && !isInArray(Dictionary.things, roundData.getData())) {
                        continue;
                    }

                    boolean isItDuplicate = false;
                    for (Player innerPlayer : game.getPlayers()) {
                        if (innerPlayer != player) {
                            if (roundData.getData().equals(innerPlayer.getRoundData().get(i).getData())) {
                                isItDuplicate = true;
                                break;
                            }
                        }
                    }
                    if (countRound <= game.getGameRoundCount()) {
                        if (isItDuplicate)
                            player.addScore(5);
                        else
                            player.addScore(10);
                    }

                }
            }

            countRound++;
            if (countRound > game.getGameRoundCount()) {
                System.out.println("endGame");
                server.getRoomOperations(roomName).sendEvent("endGame");
            } else {
                System.out.println("startROUND");
                server.getRoomOperations(roomName).sendEvent("startAnotherRound");

                currentPlayer = null;
                for (Player player : game.getPlayers()) {
                    player.setRoundData(null);
                }

            }
        });

        server.addEventListener("getScores_" + game.getId(), String.class, (socketIOClient, s, ackRequest) -> {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            ackRequest.sendAckData(gson.toJson(game.getPlayers()));
        });


    }
}
