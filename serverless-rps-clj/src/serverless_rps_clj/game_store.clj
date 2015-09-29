(ns serverless-rps-clj.game-store
  (:require [amazonica.aws.dynamodbv2 :as dyn])
  (:import (com.amazonaws.services.dynamodbv2.document.spec UpdateItemSpec)
           (com.amazonaws.services.dynamodbv2.model ReturnValue)
           (com.amazonaws.services.dynamodbv2 AmazonDynamoDBClient)
           (com.amazonaws.services.dynamodbv2.document DynamoDB)
           (com.amazonaws.regions Region Regions)))

(def beats {"rock" "paper" "paper" "scissors" "scissors" "rock"})

(defn update-winner [{:keys [player1 player1Move player2 player2Move] :as game}]
  (if (and player1Move player2Move)
    (assoc game :winner (condp = player2Move
                          (beats player1Move) player2
                          player1Move "the game was a tie"
                          player1))
    game))

(defn create-game [game-id player1]
  (dyn/put-item
    :table-name "rpslab-games"
    :item {:gameId game-id
           :state "created"
           :player1 player1})
  {:gameId game-id})

(defn get-game [game-id]
  (let [{{:keys [player1 state player2 player1Move player2Move] :as game} :item}
        (dyn/get-item
          :table-name "rpslab-games"
          :key {:gameId {:s game-id}})]
    (prn game)
    (update-winner (merge {:gameId  game-id
                           :player1 player1
                           :state   state}
                     (when player2 {:player2 player2})
                     (when player1Move {:player1Move player1Move})
                     (when player2Move {:player2Move player2Move})))))

(defn get-games [state]
  (->> (dyn/scan
         :table-name "rpslab-games"
         :scan-filter {:state {:attribute-value-list [(or state "created")]
                               :comparison-operator  :EQ}})
    :items
    (take 20)
    (map update-winner)))

(defn- item-to-game [item]
  (update-winner (merge {:gameId (.getString item "gameId")
                         :player1 (.getString item "player1")
                         :state   (.getString item "state")}
                   (when-let [player2 (.getString item "player2")]
                     {:player2 player2})
                   (when-let [player1-move (.getString item "player1Move")]
                     {:player1Move player1-move})
                   (when-let [player2-move (.getString item "player2Move")]
                     {:player2Move player2-move}))))

(defn join-game [game-id player2]
  #_(update-item
    :table-name "game-table"
    :key {"gameId" gameId}
    :update-expression "set #p2 = :val1, #st = :val2"
    :condition-expression "#st = :val3"
    :return-values "ALL_NEW")
  (let [client (doto (AmazonDynamoDBClient.)
                 (.setRegion (Region/getRegion Regions/EU_WEST_1)))
        dynamo (DynamoDB. client)
        table (.getTable dynamo "rpslab-games")
        spec (doto (UpdateItemSpec.)
               (.withPrimaryKey "gameId" game-id)
               (.withUpdateExpression "set #p2 = :val1, #st = :val2")
               (.withConditionExpression "#st = :val3")
               (.withNameMap {"#p2" "player2" "#st" "state"})
               (.withValueMap {":val1" player2 ":val2" "ready" ":val3" "created"})
               (.withReturnValues ReturnValue/ALL_NEW))
        item (.getItem (.updateItem table spec))]
    (prn item)
    (item-to-game item)))

(defn- decide-state-field [player current-game]
  (let [{:keys [player1 player2 player1Move player2Move]} current-game]
    (condp = player
          player1 (cond
                    (and
                      (nil? player1Move)
                      (nil? player2Move)) ["waiting" "player1Move"]
                    player2Move ["ended" "player1Move"]) 
          
          player2 (cond
                    (and
                      (nil? player1Move)
                      (nil? player2Move)) ["waiting" "player2Move"]
                    player1Move ["ended" "player2Move"]))))

(defn make-move [game-id player move]
  (let [current-game (get-game game-id)
        _ (prn current-game)]
    (if (= "ended" (:state current-game))
      (update-winner current-game)
      (let [[new-state field-to-update] (decide-state-field player current-game)
            _ (println new-state field-to-update)]
        (if field-to-update
          (let [client (doto (AmazonDynamoDBClient.)
                         (.setRegion (Region/getRegion Regions/EU_WEST_1)))
                dynamo (DynamoDB. client)
                table (.getTable dynamo "rpslab-games")
                spec (doto (UpdateItemSpec.)
                       (.withPrimaryKey "gameId" game-id)
                       (.withUpdateExpression "set #pl = :val1, #st = :val2")
                       (.withNameMap {"#pl" field-to-update "#st" "state"})
                       (.withValueMap {":val1" move ":val2" new-state})
                       (.withReturnValues ReturnValue/ALL_NEW))
                item (.getItem (.updateItem table spec))]
            (prn item)
            (item-to-game item))
          current-game)))))