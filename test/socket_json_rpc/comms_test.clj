(ns socket-json-rpc.comms-test
  (:require  [clojure.test :refer :all]
             [clojure.data.json :as json]
             [socket-json-rpc.server :as server]
             [socket-json-rpc.client :as client :refer (notify)]))

; TODO figure out why notify only works when using the full path
; i.e. socket-json-rpc.client/notify instead of just notify or client/notify
; n.b. This issue only seems to happen inside tests :\
(deftest communication
  ; All of these servers should work the same...
  (server/start-async 23450)

  (server/start-async 23451 50)

  (future (server/start 23452))

  (future (server/start 23453 50))

  (server/defprocedure subtract
    ["minuend" "subtrahend"]
    (server/respond (- (first args) (second args))))

  (server/defprocedure add
    ["..."]
    (server/respond (reduce + args)))


  (testing "single call unnamed args"
    (is (= (client/with-server "localhost" 23450
             (subtract 42 23))
           '[(false 19)])))


  (testing "single call named args"
    (is (= (client/with-server "localhost" 23451
             (subtract :subtrahend 42 :minuend 23))
           '[(false -19)])))


  (testing "single call notification"
    (is (= (client/with-server "localhost" 23452
             (socket-json-rpc.client/notify (subtract 42 23)))
           nil)))


  (testing "single call error"
    (is (= (client/with-server "localhost" 23453
             (non-existant true))
           '[(true -32601 "Method not found")])))


  (testing "single call notify error"
    (is (= (client/with-server "localhost" 23450
             (socket-json-rpc.client/notify (non-existant true)))
           nil)))


  (testing "batch call"
    (is (= (client/with-server "localhost" 23451
             (subtract :minuend 512 :subtrahend 256)
             (socket-json-rpc.client/notify (subtract 5 2))
             (add 1 2 3 4 5)
             (non-existant true)
             (subtract 5)
             (socket-json-rpc.client/notify (non-existant true))
             (add 3))
           '[(false 256)
             (false 15)
             (true -32601 "Method not found")
             (true -32602 "Invalid params")
             (false 3)]))))


