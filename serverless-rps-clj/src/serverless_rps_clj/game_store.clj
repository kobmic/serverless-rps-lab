(ns serverless-rps-clj.game-store
  (:require [taoensso.faraday :as far]))

(def ^:dynamic *client-opts* {:endpoint "https://dynamodb.eu-west-1.amazonaws.com"})

(def beats {"rock" "paper" "paper" "scissors" "scissors" "rock"})

(defn update-winner [{:keys [player1 player1Move player2 player2Move] :as game}]
  (if (and player1Move player2Move)
    (assoc game :winner (condp = player2Move
                          (beats player1Move) player2
                          player1Move "tie"
                          player1))
    game))

(defn create-game [game-id player1]
  (far/put-item *client-opts*
    :rpslab-games
    {:gameId  game-id
     :state   :created
     :player1 player1})
  {:gameId game-id
   :email player1
   :state :created})

(defn get-game [game-id]
  (update-winner
    (far/get-item *client-opts*
      :rpslab-games
      {:gameId game-id})))

(defn get-games [state]
  (->> (far/scan *client-opts*
         :rpslab-games
         {:attr-conds {:state [:eq state]}})
    (take 20)
    (map update-winner)))

(defn join-game [game-id player2]
  (let [current-game (get-game game-id)]
    (if (not= "created" (:state current-game))
      (if (= player2 (:player2 current-game))
        current-game
        (throw (Exception. "Game already started. Could not join game.")))
      (update-winner (far/update-item *client-opts*
                       :rpslab-games
                       {:gameId game-id}
                       {:player2 [:put player2]
                        :state   [:put :ready]}
                       {:return   :all-new
                        :expected {:state [:eq :created]}})))))

(defn- decide-state-field [player current-game]
  (let [{:keys [player1 player2 player1Move player2Move]} current-game]
    (condp = player
          player1 (cond (and (nil? player1Move) (nil? player2Move)) [:waiting :player1Move]
                        player1Move [:waiting :player1Move]
                        player2Move [:ended :player1Move])
          player2 (cond (and (nil? player1Move) (nil? player2Move)) [:waiting :player2Move]
                        player2Move [:waiting :player2Move]
                        player1Move [:ended :player2Move]))))

(defn make-move [game-id player move]
  (let [current-game (get-game game-id)]
    (if (= "ended" (:state current-game))
      current-game
      (let [[new-state field-to-update] (decide-state-field player current-game)]
        (if field-to-update
          (update-winner
            (far/update-item *client-opts*
              :rpslab-games
              {:gameId game-id}
              {field-to-update [:put move]
               :state          [:put new-state]}
              {:return :all-new}))
          current-game)))))