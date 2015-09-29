(defproject hello-lambda-clj "0.1.0-SNAPSHOT"
  :description "Clojure implementation of the hello-lambda module in serverless-rps-lab"
  :url "https://github.com/kobmic/serverless-rps-lab"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.amazonaws/aws-lambda-java-core "1.0.0"]]
  :java-source-paths ["src-java"]
  :aot :all)
