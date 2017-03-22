(ns socket-json-rpc.client
  (:require [clojure.data.json :as json]
            [clojure.core.async :as async]
            [com.gearswithingears.async-sockets :refer :all]))

(defn- parse-body
  "Converts function call(s) from clojure s-expression form to JSON RPC form"
  [body]
  (loop [body body out nil id 1]
    (if-let [func (if (list? (second (first body)))
                    (second (first body))
                    (first body))]
      (let [method (str (first func))
            args (rest func)]
        (let [args (if (keyword? (first args))
                     (if (odd? (count args))
                       (throw (new IllegalArgumentException "Keyword arguments must contain an even number of forms"))
                       (zipmap (take-nth 2 args) (take-nth 2 (rest args))))
                     args)]
          (let [request {"jsonrpc" "2.0"
                         "method" method
                         "params" args
                         "id" id}]
            (recur (rest body)
                   (conj out (if (list? (second (first body)))
                               (@(resolve (first (first body))) request)
                               request))
                   (inc id)))))
      (if (= (count out) 1)
        (first out)
        out))))

(defn notify
  "Filters out the 'id' key from a single RPC request."
  [request]
  (dissoc request "id"))

; (Should probably create a common file for all of these functions ;)
; From http://stackoverflow.com/a/12503724
(defn- parse-int
  "Parses the first continuous number only"
  [s]
  (new Integer (re-find #"\d+" s)))

(defn- read-all
  "Reads the character count on the first line of the response and then reads
  specified number of characters from the response, then returns the response
  body."
  [socket]
  (loop [length -1 response ""]
    (when-let [line (async/<!! (:in socket))]
      (if (or (nil? length) (zero? length))
        nil
        (if (neg? length)
          (recur (try (parse-int line)
                      (catch Exception _ nil))
                 response)
          (let [response (str response line)]
            (if (< (count response) length)
              (recur length (str response "\n"))
              response)))))))

(defn- socket-io
  "Sends the collated JSON-RPC request to the RPC server, and then returns a
  properly formatted response."
  [address port out]
  (let [socket (socket-client port address)]
    (let [jsonout (json/write-str out)]
      (async/>!! (:out socket) (str (count jsonout) "\n" jsonout))
      (if-let [ret (read-all socket)]
        (do
          (close-socket-client socket)
          (let [return (json/read-str ret)]
            (if (map? return)
              (if (contains? return "error")
                (vector (list true (get-in return ["error" "code"]) (get-in return ["error" "message"])))
                (vector (list false (get return "result"))))
              (loop [return return result []]
                (if-let [response (first return)]
                  (if (contains? response "error")
                    (recur (rest return)
                           (conj result
                                 (list (get response "id")
                                       true
                                       (get-in response ["error" "code"])
                                       (get-in response ["error" "message"]))))
                    (recur (rest return)
                           (conj result
                                 (list (get response "id")
                                       false
                                       (get response "result")))))
                  (vec (map rest (sort-by first result))))))))
        (do
          (close-socket-client socket)
          nil)))))

(defmacro with-server
  "Runs the body on a specified server. Ordered parameters are supplied just
  like supplying args to any other Clojure function. Named parameters are
  supplied by using keyword arguments."
  [address port & body]
  `(when-let [out# (#'parse-body '~body)]
     (#'socket-io ~address ~port out#)))
