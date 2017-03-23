# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/), and uses [Semantic Versioning](http://semver.org).

## [Unreleased]
### Changed

## [1.0.0] - 2017-03-23
### Added
- `start` and `start-async` functions to serve incoming socket RPC requests
- `defprocedure` to create new procedures that can then be called remotely
- `respond` and `error` for server-side procedures to generate responses
- `socket-io` private function to handle reading and writing to server sockets
- `execute` private function to parse the request and decide whether the request
  is a single procedure call or a batch
- `do-single` and `do-batch` to separate individual calls, and execute, before
  arranging return JSON
- `execute-single` to execute the map representation of a single procedure call,
  if it is valid
- `when-valid` executes its body if the input to `execute-single` is in a valid
  JSON-RPC request format
- `named-params` and `positional-params` organise input parameters into a
  consistent format, before calling the procedure defined by `defprocedure`
- `with-server` macro for clients to call procedures on a server
- `notify` can be wrapped around a procedure call if a client does not want a
  response
- `socket-io` to send a request to the specified server, then read the response,
  organising responses by `id` in the case of a batch.
- `read-all` handles reading the character count at the top of the response, then
  reading an appropriate number of characters based on that count
- `parse-body` converts the body of a `with-server` block from clojure function
  form to JSON-RPC form, checking for validity
- README with basic API documentation

[Unreleased]: https://github.com/invlpg/socket-json-rpc/compare/1.0.0...HEAD
