(ns serverless-rps-cljs.game-store
  (:require [cljs.nodejs :as node]
            [cljs.core.async :refer [chan <! >! put!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def aws (node/require "aws-sdk"))
(def beats {"rock" "paper" "paper" "scissors" "scissors" "rock"})

(defn- update-winner [{:keys [player1 player1Move player2 player2Move] :as game}]
  (println "update-winner:" (pr-str game))
  (if (and player1Move player2Move)
    (assoc game :winner (condp = player2Move
                          (beats player1Move) player2
                          player1Move "tie"
                          player1))
    game))

(defn- item-to-game [item]
  (println "item-to-game:" (pr-str item))
  (let [{:keys [gameId player1 player2 player1Move player2Move state]} item]
    (println "item:" (pr-str item))
    (update-winner (merge {}
                     (when gameId {:gameId (:S gameId)})
                     (when player1 {:player1 (:S player1)})
                     (when player2 {:player2 (:S player2)})
                     (when player1Move {:player1Move (:S player1Move)})
                     (when player2Move {:player2Move (:S player2Move)})
                     (when state {:state (:S state)})))))

(defn get-game [gameId]
  (println "get-game:" (pr-str gameId))
  (go
    (let [c (chan)
          db (aws.DynamoDB.)]
      (.getItem db (clj->js {:Key       {:gameId {:S gameId}}
                             :TableName :rpslab-games})
        (fn [err data]
          (when err (throw err))
          (put! c (item-to-game (:Item (js->clj data :keywordize-keys true))))))
      (<! c))))

(defn get-games [state]
  (println "get-games:" (pr-str state))
  (go
    (let [c (chan)
          db (aws.DynamoDB.)]
      (.scan db (clj->js {:ScanFilter {:state {:ComparisonOperator :EQ
                                               :AttributeValueList [{:S state}]}}
                          :TableName  :rpslab-games})
        (fn [err data]
          (when err (throw err))
          (put! c (take 20 (mapv item-to-game (:Items (js->clj data :keywordize-keys true)))))))
      (<! c))))

(defn create-game [gameId email]
  (println "create-game:" (pr-str gameId email))
  (go
    (let [c (chan)
          db (aws.DynamoDB.)]
      (.putItem db (clj->js {:Item      {:gameId  {:S gameId}
                                         :state   {:S :created}
                                         :player1 {:S email}}
                             :TableName :rpslab-games})
        (fn [err data]
          (when err (throw err))
          (put! c {:gameId gameId})))
      (-> (<! c) :gameId get-game <!))))

(defn join-game [gameId email]
  (println "join-game:" (pr-str gameId email))
  (go
    (let [c (chan)
          db (aws.DynamoDB.)]
      (.updateItem db (clj->js {:Key              {:gameId {:S gameId}}
                                :AttributeUpdates {:player2 {:Action :PUT
                                                             :Value  {:S email}}
                                                   :state   {:Action :PUT
                                                             :Value  {:S :ready}}}
                                :Expected         {:state {:AttributeValueList [{:S :created}]
                                                           :ComparisonOperator :EQ}}
                                :TableName        :rpslab-games
                                :ReturnValues     :ALL_NEW})
        (fn [err data]
          (when err (throw err))
          (put! c (item-to-game (:Attributes (js->clj data :keywordize-keys true))))))
      (<! c))))

(defn- decide-state-field [player current-game]
  (println "decide-state-field:" (pr-str player current-game))
  (let [{:keys [player1 player2 player1Move player2Move]} current-game]
    (condp = player
      player1 (cond (and (nil? player1Move) (nil? player2Move)) [:waiting :player1Move]
                    player1Move [:waiting :player1Move]
                    player2Move [:ended :player1Move])
      player2 (cond (and (nil? player1Move) (nil? player2Move)) [:waiting :player2Move]
                    player2Move [:waiting :player2Move]
                    player1Move [:ended :player2Move]))))

(defn make-move [gameId email move]
  (println "make-move:" (pr-str gameId email move))
  (go
    (let [current-game (<! (get-game gameId))
          c (chan)]
      (if (= "ended" (:state current-game))
        (>! c current-game)
        (let [[new-state field-to-update] (decide-state-field email current-game)
              _ (println "[new-state field-to-update]" (pr-str [new-state field-to-update]))
              db (aws.DynamoDB.)]
          (if field-to-update
            (.updateItem db (clj->js {:Key              {:gameId {:S gameId}}
                                      :AttributeUpdates {field-to-update {:Action :PUT
                                                                          :Value  {:S move}}
                                                         :state          {:Action :PUT
                                                                          :Value  {:S new-state}}}
                                      :TableName        :rpslab-games
                                      :ReturnValues     :ALL_NEW})
              (fn [err data]
                (when err (throw err))
                (put! c (item-to-game (:Attributes (js->clj data :keywordize-keys true))))))
            (>! c current-game))))
      (<! c))))
