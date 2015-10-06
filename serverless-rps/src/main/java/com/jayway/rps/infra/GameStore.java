package com.jayway.rps.infra;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.jayway.rps.domain.Game;
import com.jayway.rps.domain.PlayerMove;

import java.util.*;

/**
 * Utility class for storing game state in DynamoDB.
 */
public class GameStore {

    /**
     * Game store methods are called from lambda, so we need to fetch a new connection
     * every time.
     */
    public static Table createConnectionAndGetTable() {
        //Create a connection to DynamoDB Services
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(Regions.EU_WEST_1));

        //Create a DynamoDB Object from connection
        DynamoDB dynamoDB = new DynamoDB(client);

        //get table reference
        return dynamoDB.getTable("rpslab-games");
    }

    /**
     * Create game with primary key gameId.
     * @param gameId
     * @param player1
     */
    public static Game createGame(String gameId, String player1) {

        Table table = GameStore.createConnectionAndGetTable();
        String createdState = "created";

        Item item = new Item()
                .withPrimaryKey("gameId", gameId)
                .withString("state", createdState)
                .withString("player1", player1);

        // Write the item to the table
        // putItemOutcome.getItem() might be null
        table.putItem(item);

        Game game = new Game();
        game.setGameId(gameId);
        game.setState(createdState);
        game.setPlayer1(player1);
        return game;
    }

    /**
     * Player2 joins game. Idempotent for player2.
     * @param gameId
     * @param player2
     */
    public static Game joinGame(String gameId, String player2) {

        Table table = GameStore.createConnectionAndGetTable();

        Game currentGame = GameStore.getGame(gameId);
        if (!"created".equals(currentGame.getState())) {
            if (player2.equals(currentGame.getPlayer2())) {
                return currentGame;
            } else {
                throw new RuntimeException("Game already started. Could not join game.");
            }
        }

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("gameId", gameId)
                .withUpdateExpression("set #p2 = :val1, #st = :val2")
                .withConditionExpression("#st = :val3")
                .withNameMap(new NameMap().with("#p2", "player2").with("#st", "state"))
                .withValueMap(new ValueMap().withString(":val1", player2).withString(":val2", "ready").withString(":val3", "created"))
                .withReturnValues(ReturnValue.ALL_NEW);

        UpdateItemOutcome outcome =  table.updateItem(updateItemSpec);
        return itemToGame(outcome.getItem());
    }

    /**
     * Player moves.
     * @param move
     */
    public static Game makeMove(PlayerMove move) {
        Table table = GameStore.createConnectionAndGetTable();
        UpdateItemOutcome outcome = null;

        Game currentGame = GameStore.getGame(move.getGameId());
        String newState = null;
        String fieldToUpdate = null;

        if (currentGame.getState().equals("ended")) {
            System.out.println("game ended");
            return currentGame;
        }

        boolean player1Moves = move.getEmail().equals(currentGame.getPlayer1());
        boolean player2Moves = move.getEmail().equals(currentGame.getPlayer2());

        if (player1Moves) {
            if (currentGame.hasNoMoves()) {
                fieldToUpdate = "player1Move";
                newState = "waiting";
            } else if (currentGame.player2HasMoved()) {
                fieldToUpdate = "player1Move";
                newState = "ended";
            }
        } else if (player2Moves) {
            if (currentGame.hasNoMoves()) {
                fieldToUpdate = "player2Move";
                newState = "waiting";
            } else if ((currentGame.player1HasMoved())) {
                fieldToUpdate = "player2Move";
                newState = "ended";
            }
        }

        //only move if not already moved
        if (fieldToUpdate != null) {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("gameId", move.getGameId())
                    .withUpdateExpression("set #pl = :val1, #st = :val2")
                    .withNameMap(new NameMap().with("#pl", fieldToUpdate).with("#st", "state"))
                    .withValueMap(new ValueMap().withString(":val1", move.getMove().name()).withString(":val2", newState))
                    .withReturnValues(ReturnValue.ALL_NEW);

            outcome = table.updateItem(updateItemSpec);
            return itemToGame(outcome.getItem());
        } else {
            return currentGame;
        }
    }


    /**
     * Get game by id.
     * @param gameId
     * @return
     */
    public static Game getGame(String gameId) {
        Table table = GameStore.createConnectionAndGetTable();
        Item item = table.getItem("gameId", gameId);
        return itemToGame(item);
    }

    /**
     * Get games by state. Return max 20 results.
     * @param state
     * @return
     */
    public static List<Game> getGames(String state) {
        Table table = GameStore.createConnectionAndGetTable();

        // default: created games waiting for player to join
        String gameState = ((state == null) || (state.length() == 0)) ? "created" : state;

        List<Game> games = new ArrayList<Game>();

        ScanSpec spec = new ScanSpec()
                .withFilterExpression("#st = :val1")
                .withNameMap(new NameMap().with("#st", "state"))
                .withValueMap(new ValueMap().with(":val1", gameState))
                .withMaxResultSize(20);

        ItemCollection<ScanOutcome> items = table.scan(spec);

        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            games.add(GameStore.itemToGame(iterator.next()));
        }
        return games;
    }

    public static Game itemToGame(Item item) {
        Game game = new Game();
        game.setGameId((String) item.get("gameId"));
        game.setPlayer1((String) item.get("player1"));
        game.setState((String) item.get("state"));
        if (item.isPresent("player2")) {
            game.setPlayer2((String) item.get("player2"));
        }
        if (item.isPresent("player2")) {
            game.setPlayer2((String) item.get("player2"));
        }
        if (item.isPresent("player1Move")) {
            game.setPlayer1Move((String) item.get("player1Move"));
        }
        if (item.isPresent("player2Move")) {
            game.setPlayer2Move((String) item.get("player2Move"));
        }
        if (item.isPresent("player1Move") && item.isPresent("player2Move")) {
            game.updateWinner();
        }
        return game;
    }
}
