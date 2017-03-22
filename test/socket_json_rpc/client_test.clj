(ns socket-json-rpc.client-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [socket-json-rpc.client :as client]))

(deftest client-errors
  (testing "keyword arguments uneven number of forms"
    (is (thrown? IllegalArgumentException
                 (client/with-server "localhost" 12345
                   (test :hi))))))

