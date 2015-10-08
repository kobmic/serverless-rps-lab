(defproject serverless-rps-cljs "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "http://please.FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [io.nervous/cljs-lambda "0.1.2"]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-npm "0.5.0"]
            [io.nervous/lein-cljs-lambda "0.2.4"]]
  :node-dependencies [[source-map-support "0.2.8"]
                      [aws-sdk "2.2.8"]]
  :source-paths ["src"]
  :cljs-lambda
  {:defaults {:role "arn:aws:iam::554360467205:role/lambda_basic_execution"}
   :functions
   [{:name   "ulsa-cljs-create-game"
     :invoke serverless-rps-cljs.core/create-game}
    {:name   "ulsa-cljs-get-game"
     :invoke serverless-rps-cljs.core/get-game}
    {:name   "ulsa-cljs-join-game"
     :invoke serverless-rps-cljs.core/join-game}
    {:name   "ulsa-cljs-make-move"
     :invoke serverless-rps-cljs.core/make-move}
    {:name   "ulsa-cljs-get-games"
     :invoke serverless-rps-cljs.core/get-games}]
   :aws-profile "jayway-devops-ulrik"}
  :cljsbuild
  {:builds [{:id "serverless-rps-cljs"
             :source-paths ["src"]
             :compiler {:output-to "out/serverless_rps_cljs.js"
                        :output-dir "out"
                        :target :nodejs
                        :optimizations :none
                        :source-map true}}]})
