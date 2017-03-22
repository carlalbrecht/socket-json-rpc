# socket-json-rpc

[![Build Status](https://travis-ci.org/invlpg/socket-json-rpc.svg?branch=master)](https://travis-ci.org/invlpg/socket-json-rpc)
[![codecov](https://codecov.io/gh/invlpg/socket-json-rpc/branch/master/graph/badge.svg)](https://codecov.io/gh/invlpg/socket-json-rpc)
[![Dependencies Status](https://jarkeeper.com/invlpg/socket-json-rpc/status.svg)](https://jarkeeper.com/invlpg/socket-json-rpc)

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
  (server/respond (- (first args) (second args))))
```

*Note:* The body automatically receives the variables `args` and `notification`.
`args` is a vector of supplied arguments, matching the order specified in the
`named-args` vector supplied when the procedure was defined. `notification` is a
boolean value that is true when the client does not want to receive a reply with
the result of the procedure call. If the procedure does not respect the
`notification` variable, the server will still not send the result anyway.

*Note:* Normally, the number of unnamed arguments supplied must match the number
of named arguments supplied in the procedure definition, but to allow for
variable arguments, the special named argument `"..."` can be used. For example:

```clojure
(server/defprocedure add
  ["a" "..."]
  (server/respond (reduce + args)))
```

The client must still provide at least the number of named arguments other than
`"..."`. In the previous example, at least one number would have to be specified.

The `subtract` method can be called with either of the following JSON:

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

## Client Usage

First, require the client:

```clojure
(require '[socket-json-rpc.client :as client])
```

The client is very simple, only exposing one macro - `with-server`, and is used
in the form

```clojure
(client/with-server [host] [port] [body])
```

This form allows the client to call functions on the server *almost* as if they
were local functions. For example, our `subtract` procedure defined above could
be called on a local server running on port `9001` by either:

Using the ordered argument form:

```clojure
(client/with-server "localhost" 9001
  (subtract 42 23))
```

Or, using the named argument form:

```clojure
(client/with-server "localhost" 9001
  (subtract :subtrahend 23 :minuend 42))
```

Both forms would return the same result:

```clojure
[(false 19)]
```

`with-server` returns a vector of function results. Each item in the vector is a
list containing

```clojure
'([error] 
   [result] || [code] [message])
  
; i.e. successful function call
'(false 19)

; i.e. error condition
'(true -32601 "Method not found")
```

A remote procedure call can be wrapped in `notify` if you aren't interested in
receiving a reply with the result of the function call.

Multiple functions can be grouped together into a single "batch" by calling them
successively inside the `with-server` block. Note that each function will be
called on the server in-order.

```clojure
(client/with-server "localhost" 9001
  (subtract 42 23)
  (add 1 2 3 4 5)
  (subtract :subtrahend 42 :minuend 23)
  (notify (add 15 15))
  (doesntexist true))
  
;=> [(false 19)
     (false 15)
     (false -19)
     (true -32601 "Method not found")]
```

Notice that the return values arrive in the same order as the calls were
delivered. 

## Common Problems

```clojure
ClassCastException java.lang.String cannot be cast to clojure.lang.Associative
```

This usually means that you forgot to add `(respond [val])` around your procedure's
return value.

## License

Copyright © 2017 Carl Albrecht

Distributed under the Eclipse Public License version 1.0
