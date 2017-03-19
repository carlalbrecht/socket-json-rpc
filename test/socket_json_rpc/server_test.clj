(ns socket-json-rpc.server-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [socket-json-rpc.server :refer :all]))

; Tests all examples from http://www.jsonrpc.org/specification#examples
(deftest spec-examples
  (defprocedure subtract
    ["minuend" "subtrahend"]
    (respond (- (first args) (second args))))

  (testing "rpc call with positional parameters"
    (is (= (json/read-str (#'socket-json-rpc.server/execute
           (json/write-str
           {"jsonrpc" "2.0" "method" "subtract" "params" [42 23] "id" 1})))
           {"jsonrpc" "2.0" "result" 19 "id" 1}))

    (is (= (json/read-str (#'socket-json-rpc.server/execute
           (json/write-str
           {"jsonrpc" "2.0" "method" "subtract" "params" [23 42] "id" 2})))
           {"jsonrpc" "2.0" "result" -19 "id" 2})))


  (testing "rpc call with named parameters"
    (is (= (json/read-str (#'socket-json-rpc.server/execute
           (json/write-str
           {"jsonrpc" "2.0" "method" "subtract" "params" {"subtrahend" 23 "minuend" 42} "id" 3})))
           {"jsonrpc" "2.0" "result" 19 "id" 3}))

    (is (= (json/read-str (#'socket-json-rpc.server/execute
           (json/write-str
           {"jsonrpc" "2.0" "method" "subtract" "params" {"minuend" 42 "subtrahend" 23} "id" 4})))
           {"jsonrpc" "2.0" "result" 19 "id" 4})))


  ; Create procedures that do nothing but return spoofed values for tests
  (defprocedure update
    ["1" "2" "3" "4" "5"]
    (respond 4))

  (defprocedure foobar
    ["a" "b"]
    (respond "you shouldn't see this"))

  (testing "notifications"
    (is (= (#'socket-json-rpc.server/execute
           (json/write-str
           {"jsonrpc" "2.0" "method" "update" "params" [1 2 3 4 5]}))
           ""))

    (is (= (#'socket-json-rpc.server/execute
           (json/write-str
           {"jsonrpc" "2.0" "method" "foobar"}))
           "")))

  (testing "rpc call of non-existent method"
    (is (= (json/read-str (#'socket-json-rpc.server/execute
           (json/write-str
           {"jsonrpc" "2.0" "method" "nonexistent" "id" "1"})))
           {"jsonrpc" "2.0" "error" {"code" -32601 "message" "Method not found"} "id" "1"})))

  (testing "rpc call with invalid JSON"
    (is (= (json/read-str (#'socket-json-rpc.server/execute
           "{\"jsonrpc\": \"2.0\", \"method\": \"foobar, \"params\": \"bar\", \"baz]}"))
           {"jsonrpc" "2.0" "error" {"code" -32700 "message" "Parse error"} "id" nil})))

  (testing "rpc call with invalid Request object"
    (is (= (json/read-str (#'socket-json-rpc.server/execute
           (json/write-str
           {"jsonrpc" "2.0" "method" 1 "params" "bar"})))
           {"jsonrpc" "2.0" "error" {"code" -32600 "message" "Invalid Request"} "id" nil})))
   )
