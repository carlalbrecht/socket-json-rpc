(defproject socket-json-rpc "1.0.0"
  :description "An asynchronous JSON-RPC client and server for Clojure that uses sockets for communication"
  :url "https://github.com/invlpg/socket-json-rpc"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.async "0.3.441"]
                 [com.hellonico.gearswithingears/async-sockets "0.1.0"]])
