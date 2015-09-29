(ns hello-lambda-clj.core
  (:gen-class
    :methods [^:static [handler [String] String]])
  (:require [clojure.data.json :as json])
  (:import (java.util UUID)))

(defn -handler [s]
  (str "Hello " s "!"))

; Add POJO handling

(defn -handlepojo [this event]
  (json/write-str {:gameid (str (UUID/randomUUID))
                   :name   (.getName event)
                   :email  (.getEmail event)}))

(gen-class
  :name PojoHandler
  :methods [[handlepojo [hello_lambda_clj.Player] String]])