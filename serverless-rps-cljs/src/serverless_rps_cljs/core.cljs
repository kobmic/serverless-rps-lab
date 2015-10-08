(ns serverless-rps-cljs.core
  (:require [serverless-rps-cljs.game-store :as game-store]
            [cljs-lambda.util :refer [async-lambda-fn]]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:export create-game
  (async-lambda-fn
    (fn [{:keys [email] :as event} context]
      (go
        (println "input event:" (pr-str event))
        (<! (game-store/create-game (str (random-uuid)) email))))))

(def ^:export get-game
  (async-lambda-fn
    (fn [{:keys [gameId] :as event} context]
      (go
        (println "input event:" (pr-str event))
        (<! (game-store/get-game gameId))))))

(def ^:export get-games
  (async-lambda-fn
    (fn [{:keys [state] :as event} context]
      (go
        (println "input event:" (pr-str event))
        (<! (game-store/get-games state))))))

(def ^:export join-game
  (async-lambda-fn
    (fn [{:keys [gameId email] :as event} context]
      (go
        (println "input event:" (pr-str event))
        (<! (game-store/join-game gameId email))))))

(def ^:export make-move
  (async-lambda-fn
    (fn [{:keys [gameId email move] :as event} context]
      (go
        (println "input event:" (pr-str event))
        (<! (game-store/make-move gameId email move))))))
