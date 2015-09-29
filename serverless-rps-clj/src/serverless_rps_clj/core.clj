(ns serverless-rps-clj.core
  (:require [clojure.data.json :as json]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [serverless-rps-clj.game-store :as game-store])
  (:import (java.util UUID)))

(defn key->keyword [key-string]
  (-> key-string
    (s/replace #"([a-z])([A-Z])" "$1-$2")
    (s/replace #"([A-Z]+)([A-Z])" "$1-$2")
    (s/lower-case)
    (keyword)))

;; reads JSON, executes f, writes JSON
(defn handleRequest [this is os context f]
  (let [w (io/writer os)]
    (-> (json/read (io/reader is) :key-fn key->keyword)
      (f)
      (json/write w))
    (.flush w)))

;; convenience macro for generating gen-class and handleRequest

(defmacro deflambda
  "Event handler that you write:
  
    (defn handle-create-game-event [event]
      (prn event)
      (game-store/create-game (str (UUID/randomUUID)) (:email event)))
  
  Then add a call to deflambda:
  
    (deflambda create-game)
  
  The deflambda macro generates this code:
  
    (gen-class
      :name CreateGame
      :prefix CreateGame-
      :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
    
    (defn CreateGame-handleRequest [this is os context]
      (handleRequest this is os context handle-create-game-event))"
  [name]
  (let [class-name (->> (clojure.string/split (str name) #"-")
                     (mapcat clojure.string/capitalize)
                     (apply str))]
    `(do (gen-class
           :name ~(symbol class-name)
           :prefix ~(symbol (str class-name "-"))
           :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])

         (defn ~(symbol (str class-name "-handleRequest")) [this# is# os# context#]
           (handleRequest this# is# os# context# ~(symbol (str "handle-" name "-event")))))))

;; Create Game

(defn handle-create-game-event [event]
  (prn event)
  (game-store/create-game (str (UUID/randomUUID)) (:email event)))

(deflambda create-game)

;; Get Game

(defn handle-get-game-event [event]
  (prn event)
  (game-store/get-game (:game-id event)))

(deflambda get-game)

;; Get Games

(defn handle-get-games-event [event]
  (prn event)
  (game-store/get-games (:state event)))

(deflambda get-games)

;; Join Game

(defn handle-join-game-event [{:keys [game-id email] :as event}]
  (prn event)
  (game-store/join-game game-id email))

(deflambda join-game)

;; Make Move

(defn handle-make-move-event [{:keys [game-id email move] :as event}]
  (prn event)
  (game-store/make-move game-id email move))

(deflambda make-move)
