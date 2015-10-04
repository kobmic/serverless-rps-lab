(ns serverless-rps-clj.core
  (:require [clojure.data.json :as json]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [serverless-rps-clj.game-store :as game-store]))

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

(defmacro deflambda [name args & body]
  (let [class-name (->> (clojure.string/split (str name) #"-")
                     (mapcat clojure.string/capitalize)
                     (apply str))]
    `(do (gen-class
           :name ~(symbol class-name)
           :prefix ~(symbol (str class-name "-"))
           :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])

         (defn ~(symbol (str "handle-" name "-event")) ~args
           ~@body)
         
         (defn ~(symbol (str class-name "-handleRequest")) ~'[this is os context]
           (handleRequest ~'this ~'is ~'os ~'context ~(symbol (str "handle-" name "-event")))))))

;; Create Game

(deflambda create-game [event]
  (game-store/create-game (str (java.util.UUID/randomUUID)) (:email event)))

;; Get Game

(deflambda get-game [event]
  (game-store/get-game (:game-id event)))

;; Get Games

(deflambda get-games [event]
  (game-store/get-games (:state event)))

;; Join Game

(deflambda join-game [{:keys [game-id email] :as event}]
  (game-store/join-game game-id email))

;; Make Move

(deflambda make-move [{:keys [game-id email move] :as event}]
  (game-store/make-move game-id email move))