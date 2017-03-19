# socket-json-rpc

socket-json-rpc is a Clojure library that simplifies creating async JSON-RPC
servers and clients that communicate using TCP sockets.

**Note:** This is in-development software and _definitely_ shouldn't be used in
production code under any circumstances. I will laugh at you if you get fired
because you used under-developed software and it failed.

## Server Usage

First, require the server:

```clojure
(require '[socket-json-rpc.server :as server])
```

Procedures are defined with `defprocedure`, and is used in the form

```clojure
(server/defprocedure [name] [[named-args]] [body])
```

The named-args argument tells the server how to arrange named arguments into a
vector so that the body can use the arguments in a consistent manner. For
example, the following procedure allows both unnamed and named argument usage.

```clojure
(server/defprocedure subtract
  ["minuend" "subtrahend"]
  (respond (- (first args) (second args))))
```

*Note:* The body automatically receives the variables `args` and `notification`.
`args` is a vector of supplied arguments, matching the order specified in the
`named-args` vector supplied when the procedure was defined. `notification` is a
boolean value that is true when the client does not want to receive a reply with
the result of the procedure call. If the procedure does not respect the
`notification` variable, the server will still not send the result anyway.

This method can be called with either of the following JSON:

```javascript
// Unnamed arguments
{
  "jsonrpc": "2.0",
  "method": "subtract",
  "params": [42, 23],
  "id": 1
}
```

```javascript
// Named arguments
{
  "jsonrpc": "2.0",
  "method": "subtract",
  "params": {
    "subtrahend": 23,
    "minuend": 42
  },
  "id": 1
}
```

And both would return the same result:

```javascript
{
  "jsonrpc": "2.0",
  "result": "19",
  "id": 1
}
```

The server can then be started in one of two ways:

```clojure
; This version blocks the calling thread, so it won't close.
(defn -main
  [& args]
  ...
  (server/start [port] [backlog] [bind-addr]))
```

```clojure
; This version allows the calling thread to continue immediately, meaning that
; if the main thread closes, the server will also be shut down.
(defn -main
  [& args]
  ...
  (server/start-async [port] [backlog] [bind-addr])
  ...)
```

The only required argument to either form is `port`. `backlog` specifies how many
pending connections should be buffered, and is 50 by default. `bind-addr`
specifies the host address that the server should listen on.

## License

Copyright © 2017 Carl Albrecht

Distributed under the Eclipse Public License version 1.0
