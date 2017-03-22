(ns socket-json-rpc.client-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [socket-json-rpc.client :as client]
            [socket-json-rpc.server :as server]))

(deftest client-errors
  (testing "keyword arguments uneven number of forms"
    (is (thrown? IllegalArgumentException
                 (client/with-server "localhost" 12345
                   (test :hi))))))


; Force server to generate multi-line responses
(ns socket-json-rpc.server)
(defn set-use-newlines [f]
  (def execute-orig execute)

  (defn- execute
    [request]
    (let [result (execute-orig request)]
      (str/replace result #"," ",\n")))

  (f))
(ns socket-json-rpc.client-test)
(use-fixtures :once server/set-use-newlines)


(deftest client-misc
  (server/defprocedure subtract
    ["minuend" "subtrahend"]
    (server/respond (- (first args) (second args))))

  (println "wasd")

  (server/start-async 34577)

  (testing "json spread over multiple lines"
    (is (= (client/with-server "localhost" 34577
             (subtract 1337 420))
           '[(false 917)]))))

