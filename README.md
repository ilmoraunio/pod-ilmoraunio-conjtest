# pod-ilmoraunio-conjtest

A [babashka pod](https://github.com/babashka/pods) for interacting with
Clojure/babashka parsers and parsers from
[conftest](https://github.com/open-policy-agent/conftest) using [pod-ilmoraunio-conftest](https://github.com/ilmoraunio/pod-ilmoraunio-conftest).

## Usage

```clojure
(ns my-ns
  (:require [babashka.pods :as pods]))

(pods/load-pod 'ilmoraunio/conjtest "0.0.2")
(require '[pod-ilmoraunio-conjtest.api :as api])
```

The namespace exposes the following functions: 

- `parse`
- `parse-as`
- `parse-go`
- `parse-go-as`

Generally, all functions accept either file or glob patterns as argument(s).
Passing a directory is also supported, the files directly under the directory
will be passed to the parser.

Each function will return a map of filenames to parsed configurations, eg:

```clojure
{"test-resources/test.yaml"
 {:apiVersion "v1",
  :kind "Service",
  :metadata {:name "hello-kubernetes"},
  :spec
  {:type "LoadBalancer",
   :ports ({:port 80, :targetPort 8080}),
   :selector {:app "hello-kubernetes"}}},

 "test-resources/test.json" {:hello [1 2 4], "@foo" "bar"},

 "test-resources/test.edn" {:foo :bar, :duration "#duration 20m"}}
```

Generally, Clojure parsers aim to return keyworded maps whereas Go parsers do
not.

Currently supported Clojure parsers: 
- edn
- json
- yaml

Currently supported Go parsers:
- cue
- dockerfile
- edn
- hcl1
- hcl2
- hocon
- ignore
- ini
- json
- jsonnet
- properties
- spdx
- toml
- vcl
- xml
- yaml
- dotenv

### `parse`

Attempts to parse `filenames` using either Clojure or Go parsers (in this
order, whichever is supported). Will automatically try to determine parser
based on filename extension.

```clojure
(api/parse "test-resources/test.edn")
;; => {"test-resources/test.edn" {:foo :bar, :duration "#duration 20m"}}

(api/parse "test-resources/*.{edn,json}")
;; => {"test-resources/test.json" {:hello [1 2 4], "@foo" "bar"}, "test-resources/test.edn" {:foo :bar, :duration "#duration 20m"}}

(api/parse "test-resources/")
;; => {"test-resources/test.yaml" #ordered/map ([:apiVersion "v1"] [:kind "Service"] [:metadata #ordered/map ([:name "hello-kubernetes"])] [:spec #ordered/map ([:type "LoadBalancer"] [:ports (#ordered/map ([:port 80] [:targetPort 8080]))] [:selector #ordered/map ([:app "hello-kubernetes"])])]), "test-resources/test.yml" #ordered/map ([:apiVersion "v1"] [:kind "Service"] [:metadata #ordered/map ([:name "hello-kubernetes-2"])] [:spec #ordered/map ([:type "LoadBalancer"] [:ports (#ordered/map ([:port 80] [:targetPort 8080]))] [:selector #ordered/map ([:app "hello-kubernetes"])])]), "test-resources/test.json" {:hello [1 2 4], "@foo" "bar"}, "test-resources/test.edn" {:foo :bar, :duration "#duration 20m"}, "test-resources/.dockerignore" [[{"Value" ".idea", "Original" ".idea", "Kind" "Path"} {"Kind" "Empty", "Value" "", "Original" ""}]]}
```

### `parse-as`

Attempts to parse `filenames` using `parser`, either using Clojure or Go parser
(in this order, whichever is supported).

```clojure
(api/parse-as "edn" "test-resources/test.edn")
;; => {"test-resources/test.edn" {:foo :bar, :duration "#duration 20m"}}

(api/parse-as "hocon" "test-resources/hocon/hocon.conf")
;; => {"test-resources/hocon/hocon.conf" {"play" {"editor" "<<unknown value>>", "server" {"debug" {"addDebugInfoToRequests" false}, "dir" "<<unknown value>>", "http" {"address" "0.0.0.0", "idleTimeout" "\"75 seconds\"", "port" 9001.0}, "https" {"address" "0.0.0.0", "engineProvider" "play.core.server.ssl.DefaultSSLEngineProvider", "idleTimeout" "\"75 seconds\"", "keyStore" {"algorithm" "<<unknown value>>", "password" "\"\"", "path" "<<unknown value>>", "type" "JKS"}, "needClientAuth" false, "port" "<<unknown value>>", "trustStore" {"noCaVerification" false}, "wantClientAuth" false}, "pidfile" {"path" "<<unknown value>>"}, "websocket" {"frame" {"maxLength" "64k"}}}}}}
```

### `parse-go`

Attempts to parse `filenames` using only Go parsers. Will automatically try to
determine parser based on filename extension.

```clojure
(api/parse-go "test-resources/*{edn,json,yaml,yml,.dockerignore}")
;; => {"test-resources/test.yaml" {"kind" "Service", "metadata" {"name" "hello-kubernetes"}, "spec" {"type" "LoadBalancer", "ports" [{"port" 80.0, "targetPort" 8080.0}], "selector" {"app" "hello-kubernetes"}}, "apiVersion" "v1"}, "test-resources/test.yml" {"apiVersion" "v1", "kind" "Service", "metadata" {"name" "hello-kubernetes-2"}, "spec" {"ports" [{"port" 80.0, "targetPort" 8080.0}], "selector" {"app" "hello-kubernetes"}, "type" "LoadBalancer"}}, "test-resources/test.json" {"hello" [1.0 2.0 4.0], "@foo" "bar"}, "test-resources/.dockerignore" [[{"Kind" "Path", "Value" ".idea", "Original" ".idea"} {"Kind" "Empty", "Value" "", "Original" ""}]], "test-resources/test.edn" {":foo" ":bar", ":duration" "#duration 20m"}}
```

### `parse-go-as`

Attempts to parse `filenames` using `parser`. Uses only Go parsers.

```clojure
(api/parse-go-as "edn" "test-resources/test.edn")
;; => {"test-resources/test.edn" {":foo" ":bar", ":duration" "#duration 20m"}}
```

## Development

### Build instructions

For building locally on macOS (AArch64)

- Run `git submodule update --init --recursive`
- Run `make build-macos-aarch64`

### Test instructions

Run `./scripts/test`.
