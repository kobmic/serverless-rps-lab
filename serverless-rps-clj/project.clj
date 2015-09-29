(defproject serverless-rps-clj "0.1.0-SNAPSHOT"
  :description "Clojure implementation of the serverless-rps module in serverless-rps-lab"
  :url "https://github.com/kobmic/serverless-rps-lab"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.amazonaws/aws-lambda-java-core "1.0.0"]
                 [amazonica "0.3.33"
                  :exclusions [com.amazonaws/aws-java-sdk
                               com.amazonaws/amazon-kinesis-client]]
                 [com.amazonaws/aws-java-sdk-core "1.10.12"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.10.12"]
                 [com.amazonaws/aws-java-sdk-lambda "1.10.12"]
                 ;; stupid, but it's needed in load-time
                 [com.amazonaws/aws-java-sdk-cloudsearch "1.10.12"]]
  :java-source-paths ["src-java"]
  :aot :all)
