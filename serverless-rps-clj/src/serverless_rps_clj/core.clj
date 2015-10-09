(ns serverless-rps-clj.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [serverless-rps-clj.game-store :as game-store]))

;; convenience macro for generating gen-class and handleRequest
(defmacro deflambda [name args & body]
  (let [class-name (->> (clojure.string/split (str name) #"-")
                     (mapcat clojure.string/capitalize)
                     (apply str))
        fn-name (symbol (str "handle-" name "-event"))]
    `(do (gen-class
           :name ~(symbol class-name)
           :prefix ~(symbol (str class-name "-"))
           :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])

         (defn ~(symbol (str class-name "-handleRequest")) [this# is# os# context#]
           (let [~fn-name (fn ~args ~@body)
                 w# (io/writer os#)]
             (-> (json/read (io/reader is#) :key-fn keyword)
               (~fn-name)
               (json/write w#))
             (.flush w#))))))

;; Create Game

(deflambda create-game [event]
  (game-store/create-game (str (java.util.UUID/randomUUID)) (:email event)))

;; Get Game

(deflambda get-game [event]
  (game-store/get-game (:gameId event)))

;; Get Games

(deflambda get-games [event]
  (game-store/get-games (:state event)))

;; Join Game

(deflambda join-game [{:keys [gameId email] :as event}]
  (game-store/join-game gameId email))

;; Make Move

(deflambda make-move [{:keys [gameId email move] :as event}]
  (game-store/make-move gameId email move))
