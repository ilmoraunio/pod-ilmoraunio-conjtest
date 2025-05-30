(ns cli-test
  (:require [babashka.pods :as pods]
            [clojure.test :refer [deftest is testing]]))

(pods/load-pod "./pod-ilmoraunio-conjtest")
(require '[pod-ilmoraunio-conjtest.api :as api])

(set! *data-readers* {'ordered/map #'flatland.ordered.map/ordered-map})

(deftest parse-test
  (testing "parse"
    (is (= {"test-resources/test.json" {:hello [1 2 4], "@foo" "bar"},
            "test-resources/test.edn" {:foo :bar
                                       :duration "#duration 20m"},
            "test-resources/test.yaml" #ordered/map([:apiVersion "v1"]
                                                    [:kind "Service"]
                                                    [:metadata #ordered/map([:name "hello-kubernetes"])]
                                                    [:spec
                                                     #ordered/map([:type "LoadBalancer"]
                                                                  [:ports '(#ordered/map([:port 80] [:targetPort 8080]))]
                                                                  [:selector #ordered/map([:app "hello-kubernetes"])])])
            "test-resources/test.yml" #ordered/map([:apiVersion "v1"]
                                                   [:kind "Service"]
                                                   [:metadata #ordered/map([:name "hello-kubernetes-2"])]
                                                   [:spec
                                                    #ordered/map([:type "LoadBalancer"]
                                                                 [:ports '(#ordered/map([:port 80] [:targetPort 8080]))]
                                                                 [:selector #ordered/map([:app "hello-kubernetes"])])])
            "test-resources/test.dockerignore" [[{"Original" ".idea", "Kind" "Path", "Value" ".idea"}
                                                 {"Value" "", "Original" "", "Kind" "Empty"}]]}
           (api/parse "test-resources/*{.edn,.json,.yaml,.yml,.dockerignore}"))))
  (testing "parse-as"
    (is (= {"test-resources/hocon/hocon.conf" {"play" {"editor" "<<unknown value>>",
                                                       "server" {"debug" {"addDebugInfoToRequests" false},
                                                                 "dir" "<<unknown value>>",
                                                                 "http" {"address" "0.0.0.0",
                                                                         "idleTimeout" "\"75 seconds\"",
                                                                         "port" 9001.0},
                                                                 "https" {"wantClientAuth" false,
                                                                          "address" "0.0.0.0",
                                                                          "engineProvider" "play.core.server.ssl.DefaultSSLEngineProvider",
                                                                          "idleTimeout" "\"75 seconds\"",
                                                                          "keyStore" {"algorithm" "<<unknown value>>",
                                                                                      "password" "\"\"",
                                                                                      "path" "<<unknown value>>",
                                                                                      "type" "JKS"},
                                                                          "needClientAuth" false,
                                                                          "port" "<<unknown value>>",
                                                                          "trustStore" {"noCaVerification" false}},
                                                                 "pidfile" {"path" "<<unknown value>>"},
                                                                 "websocket" {"frame" {"maxLength" "64k"}}}}}}
           (api/parse-as "hocon" "test-resources/hocon/hocon.conf")))
    (is (= [{"test-resources/test.edn" {:foo :bar, :duration "#duration 20m"}}
            {"test-resources/test.edn" {":duration" "#duration 20m", ":foo" ":bar"}}]
           [(api/parse-as "edn" "test-resources/test.edn")
            (api/parse-go-as "edn" "test-resources/test.edn")]))
    (is (= {"test-resources/test.json" #ordered/map([:hello [1 2 4]] ["@foo" "bar"]),
            "test-resources/test.yaml" #ordered/map([:apiVersion "v1"]
                                                    [:kind "Service"]
                                                    [:metadata #ordered/map([:name "hello-kubernetes"])]
                                                    [:spec
                                                     #ordered/map([:type "LoadBalancer"]
                                                                  [:ports '(#ordered/map([:port 80] [:targetPort 8080]))]
                                                                  [:selector #ordered/map([:app "hello-kubernetes"])])])
            "test-resources/test.yml" #ordered/map([:apiVersion "v1"]
                                                   [:kind "Service"]
                                                   [:metadata #ordered/map([:name "hello-kubernetes-2"])]
                                                   [:spec
                                                    #ordered/map([:type "LoadBalancer"]
                                                                 [:ports '(#ordered/map([:port 80] [:targetPort 8080]))]
                                                                 [:selector #ordered/map([:app "hello-kubernetes"])])])}
           (api/parse-as "yaml" "test-resources/test.json" "test-resources/test.yaml" "test-resources/test.yml")))
    (is (thrown-with-msg? Exception
                          #"unsupported SPDX version"
                          (api/parse-as "spdx" "test-resources/test.edn"))))
  (testing "parse-go"
    (is (= {"test-resources/test.yaml" {"apiVersion" "v1",
                                        "kind" "Service",
                                        "metadata" {"name" "hello-kubernetes"},
                                        "spec" {"ports" [{"port" 80.0, "targetPort" 8080.0}],
                                                "selector" {"app" "hello-kubernetes"},
                                                "type" "LoadBalancer"}},
            "test-resources/test.yml" {"apiVersion" "v1",
                                       "kind" "Service",
                                       "metadata" {"name" "hello-kubernetes-2"},
                                       "spec" {"ports" [{"port" 80.0, "targetPort" 8080.0}],
                                               "selector" {"app" "hello-kubernetes"},
                                               "type" "LoadBalancer"}}
            "test-resources/test.json" {"hello" [1.0 2.0 4.0], "@foo" "bar"},
            "test-resources/test.dockerignore" [[{"Kind" "Path", "Value" ".idea", "Original" ".idea"}
                                                 {"Original" "", "Kind" "Empty", "Value" ""}]],
            "test-resources/test.edn" {":foo" ":bar", ":duration" "#duration 20m"}}
           (api/parse-go "test-resources/*{.edn,.json,.yaml,.yml,.dockerignore}"))))
  (testing "parse-go-as"
    (is (= {"test-resources/test.edn" {":foo" ":bar", ":duration" "#duration 20m"}}
           (api/parse-go-as "edn" "test-resources/test.edn"))))
  (testing "support directories"
    (is (= {"test-resources/test.yml" #ordered/map([:apiVersion "v1"]
                                                   [:kind "Service"]
                                                   [:metadata #ordered/map([:name "hello-kubernetes-2"])]
                                                   [:spec
                                                    #ordered/map([:type "LoadBalancer"]
                                                                 [:ports '(#ordered/map([:port 80] [:targetPort 8080]))]
                                                                 [:selector #ordered/map([:app "hello-kubernetes"])])]),
            "test-resources/test.dockerignore" [[{"Kind" "Path", "Value" ".idea", "Original" ".idea"}
                                                 {"Kind" "Empty", "Value" "", "Original" ""}]],
            "test-resources/test.env" {"APP_NAME" "test", "MYSQL_USER" "user2"},
            "test-resources/test.toml" {"entryPoints" {"http" {"auth" {"headerField" "X-WebAuth-User",
                                                                       "basic" {"removeHeader" true,
                                                                                "users" ["test:$apr1$H6uskkkW$IgXLP6ewTrSuBkTrqE8wj/"
                                                                                         "test2:$apr1$d9hr9HBB$4HxwgUir3HP4EsggP/QNo0"],
                                                                                "usersFile" "/path/to/.htpasswd"},
                                                                       "digest" {"removeHeader" true,
                                                                                 "users" ["test:traefik:a2688e031edb4be6a3797f3882655c05"
                                                                                          "test2:traefik:518845800f9e2bfb1f1f740ec24f074e"],
                                                                                 "usersFile" "/path/to/.htdigest"},
                                                                       "forward" {"tls" {"ca" "path/to/local.crt",
                                                                                         "caOptional" true,
                                                                                         "cert" "path/to/foo.cert",
                                                                                         "key" "path/to/foo.key",
                                                                                         "insecureSkipVerify" true},
                                                                                  "address" "https://authserver.com/auth",
                                                                                  "trustForwardHeader" true,
                                                                                  "authResponseHeaders" ["X-Auth-User"]}},
                                                               "proxyProtocol" {"trustedIPs" ["10.10.10.1" "10.10.10.2"],
                                                                                "insecure" true},
                                                               "forwardedHeaders" {"trustedIPs" ["10.10.10.1" "10.10.10.2"]},
                                                               "address" ":80",
                                                               "compress" true,
                                                               "whitelist" {"sourceRange" ["10.42.0.0/16"
                                                                                           "152.89.1.33/32"
                                                                                           "afed:be44::/16"],
                                                                            "useXForwardedFor" true},
                                                               "tls" {"minVersion" "VersionTLS12",
                                                                      "cipherSuites" ["TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
                                                                                      "TLS_RSA_WITH_AES_256_GCM_SHA384"],
                                                                      "certificates" [{"certFile" "path/to/my.cert",
                                                                                       "keyFile" "path/to/my.key"}
                                                                                      {"certFile" "path/to/other.cert",
                                                                                       "keyFile" "path/to/other.key"}],
                                                                      "clientCA" {"files" ["path/to/ca1.crt" "path/to/ca2.crt"],
                                                                                  "optional" false}},
                                                               "redirect" {"entryPoint" "https",
                                                                           "regex" "^http://localhost/(.*)",
                                                                           "replacement" "http://mydomain/$1",
                                                                           "permanent" true}}},
                                        "defaultEntryPoints" ["http" "https"]},
            "test-resources/test.gitignore" [[{"Original" "foo", "Kind" "Path", "Value" "foo"}
                                              {"Kind" "NegatedPath", "Value" "bar", "Original" "!bar"}
                                              {"Kind" "Empty", "Value" "", "Original" ""}
                                              {"Kind" "Comment", "Value" "Baz", "Original" "# Baz"}
                                              {"Kind" "Path", "Value" "qux/", "Original" "qux/"}
                                              {"Value" "", "Original" "", "Kind" "Empty"}]],
            "test-resources/test.vcl" {"acl" {"purge" ["127.0.0.1"]},
                                       "backend" {"app" {"max_connections" 800.0,
                                                         "port" "8081",
                                                         "between_bytes_timeout" 60.0,
                                                         "connect_timeout" 60.0,
                                                         "first_byte_timeout" 60.0,
                                                         "host" "127.0.0.1"}}},
            "test-resources/test.Dockerfile" [[{"JSON" false,
                                                "Flags" [],
                                                "Value" ["openjdk:8-jdk-alpine"],
                                                "Stage" 0.0,
                                                "Cmd" "from",
                                                "SubCmd" ""}
                                               {"Cmd" "volume",
                                                "SubCmd" "",
                                                "JSON" false,
                                                "Flags" [],
                                                "Value" ["/tmp"],
                                                "Stage" 0.0}
                                               {"Cmd" "arg",
                                                "SubCmd" "",
                                                "JSON" false,
                                                "Flags" [],
                                                "Value" ["DEPENDENCY=target/dependency"],
                                                "Stage" 0.0}
                                               {"Value" ["${DEPENDENCY}/BOOT-INF/lib" "/app/lib"],
                                                "Stage" 0.0,
                                                "Cmd" "copy",
                                                "SubCmd" "",
                                                "JSON" false,
                                                "Flags" []}
                                               {"Cmd" "copy",
                                                "SubCmd" "",
                                                "JSON" false,
                                                "Flags" [],
                                                "Value" ["${DEPENDENCY}/META-INF" "/app/META-INF"],
                                                "Stage" 0.0}
                                               {"SubCmd" "",
                                                "JSON" false,
                                                "Flags" [],
                                                "Value" ["${DEPENDENCY}/BOOT-INF/classes" "/app"],
                                                "Stage" 0.0,
                                                "Cmd" "copy"}
                                               {"Flags" [],
                                                "Value" ["apk add --no-cache python3 python3-dev build-base && pip3 install awscli==1.18.1"],
                                                "Stage" 0.0,
                                                "Cmd" "run",
                                                "SubCmd" "",
                                                "JSON" false}
                                               {"JSON" true,
                                                "Flags" [],
                                                "Value" ["java" "-cp" "app:app/lib/*" "hello.Application"],
                                                "Stage" 0.0,
                                                "Cmd" "entrypoint",
                                                "SubCmd" ""}]],
            "test-resources/test.cue" {"kind" "Deployment",
                                       "metadata" {"name" "hello-kubernetes"},
                                       "spec" {"replicas" 3.0,
                                               "selector" {"matchLabels" {"app" "hello-kubernetes"}},
                                               "template" {"metadata" {"labels" {"app" "hello-kubernetes"}},
                                                           "spec" {"containers" [{"name" "hello-kubernetes",
                                                                                  "image" "paulbouwer/hello-kubernetes:1.5",
                                                                                  "ports" [{"containerPort" 8081.0}]}]}}},
                                       "apiVersion" "apps/v1"},
            "test-resources/test.jsonnet" {"str1" "The value of self.ex2 is 3.",
                                           "concat_array" [1.0 2.0 3.0 4.0],
                                           "obj" {"a" 1.0, "b" 3.0, "c" 4.0},
                                           "str5" "ex1=1.67\nex2=3.00\n",
                                           "str2" "The value of self.ex2 is 3.",
                                           "obj_member" true,
                                           "concat_string" "1234",
                                           "str4" "ex1=1.67, ex2=3.00",
                                           "equality1" false,
                                           "ex4" true,
                                           "ex1" 1.6666666666666665,
                                           "ex2" 3.0,
                                           "equality2" true,
                                           "str3" "ex1=1.67, ex2=3.00",
                                           "ex3" 1.6666666666666665},
            "test-resources/test.yaml" #ordered/map([:apiVersion "v1"]
                                                    [:kind "Service"]
                                                    [:metadata #ordered/map([:name "hello-kubernetes"])]
                                                    [:spec
                                                     #ordered/map([:type "LoadBalancer"]
                                                                  [:ports '(#ordered/map([:port 80] [:targetPort 8080]))]
                                                                  [:selector #ordered/map([:app "hello-kubernetes"])])]),
            "test-resources/test.edn" {:foo :bar, :duration "#duration 20m"},
            "test-resources/test.spdx" {"name" "hello",
                                        "documentNamespace" "https://swinslow.net/spdx-examples/example1/hello-v3",
                                        "creationInfo" {"created" "2021-08-26T01:46:00Z",
                                                        "creators" ["Person: Steve Winslow (steve@swinslow.net)"
                                                                    "Tool: github.com/spdx/tools-golang/builder"
                                                                    "Tool: github.com/spdx/tools-golang/idsearcher"]},
                                        "spdxVersion" "SPDX-2.3",
                                        "dataLicense" "conftest-demo",
                                        "SPDXID" "SPDXRef-DOCUMENT"},
            "test-resources/test.ini" {"server" {"http_port" 3000.0,
                                                 "protocol" "http",
                                                 "serve_from_sub_path" false,
                                                 "http_addr" "",
                                                 "enforce_domain" false,
                                                 "static_root_path" "public",
                                                 "root_url" "%(protocol)s://%(domain)s:%(http_port)s/",
                                                 "domain" "localhost",
                                                 "enable_gzip" false,
                                                 "router_logging" false},
                                       "users" {"viewers_can_edit" false,
                                                "allow_sign_up" false,
                                                "password_hint" "password",
                                                "auto_assign_org" true,
                                                "verify_email_enabled" false,
                                                "auto_assign_org_id" 1.0,
                                                "login_hint" "email or username",
                                                "auto_assign_org_role" "Viewer",
                                                "allow_org_create" false,
                                                "editors_can_admin" false,
                                                "default_theme" "dark"},
                                       "alerting" {"execute_alerts" true,
                                                   "max_attempts" 3.0,
                                                   "nodata_or_nullvalues" "no_data",
                                                   "notification_timeout_seconds" 30.0,
                                                   "concurrent_render_limit" 5.0,
                                                   "enabled" true,
                                                   "error_or_timeout" "alerting",
                                                   "evaluation_timeout_seconds" 30.0},
                                       "auth" {"login_maximum_lifetime_days" 30.0,
                                               "token_rotation_interval_minutes" 10.0,
                                               "disable_login_form" false,
                                               "login_cookie_name" "grafana_session",
                                               "login_maximum_inactive_lifetime_days" 7.0},
                                       "auth.basic" {"enabled" true}},
            "test-resources/test.hcl1.tf" {"output" {"client_certificate" {"value" "${google_container_cluster.primary.master_auth.0.client_certificate}"},
                                                     "client_key" {"value" "${google_container_cluster.primary.master_auth.0.client_key}"},
                                                     "cluster_ca_certificate" {"value" "${google_container_cluster.primary.master_auth.0.cluster_ca_certificate}"}},
                                           "provider" {"google" {"project" "instrumenta",
                                                                 "region" "europe-west2",
                                                                 "version" "2.5.0"}},
                                           "resource" {"google_container_node_pool" {"primary_preemptible_nodes" {"cluster" "${google_container_cluster.primary.name}",
                                                                                                                  "location" "us-central1",
                                                                                                                  "name" "my-node-pool",
                                                                                                                  "node_config" {"machine_type" "n1-standard-1",
                                                                                                                                 "metadata" {"disable-legacy-endpoints" "true"},
                                                                                                                                 "oauth_scopes" ["https://www.googleapis.com/auth/logging.write"
                                                                                                                                                 "https://www.googleapis.com/auth/monitoring"],
                                                                                                                                 "preemptible" true},
                                                                                                                  "node_count" 1.0}},
                                                       "google_container_cluster" {"primary" {"initial_node_count" 1.0,
                                                                                              "location" "us-central1",
                                                                                              "master_auth" {"password" "",
                                                                                                             "username" ""},
                                                                                              "name" "my-gke-cluster",
                                                                                              "remove_default_node_pool" true}}}},
            "test-resources/test.hcl2.tf" {"resource" {"aws_alb_listener" {"my-alb-listener" {"port" "80", "protocol" "HTTP"}},
                                                       "aws_db_security_group" {"my-group" {}},
                                                       "aws_s3_bucket" {"valid" {"acl" "private",
                                                                                 "bucket" "validBucket",
                                                                                 "tags" {"environment" "prod", "owner" "devops"}}},
                                                       "aws_security_group_rule" {"my-rule" {"cidr_blocks" ["0.0.0.0/0"],
                                                                                             "type" "ingress"}},
                                                       "azurerm_managed_disk" {"source" {"encryption_settings" {"enabled" false}}}}},
            "test-resources/test.properties" {"SAMPLE_VALUE" "something-here",
                                              "other.value.url" "https://example.com/",
                                              "secret.value.exception" "f9761ebe-d4dc-11eb-8046-1e00e20cdb95"},
            "test-resources/test.xml" {"project" {"properties" {"activejdbc.version" "2.3",
                                                                "environments" "development.test,development"},
                                                  "groupId" "org.javalite",
                                                  "dependencies" {"dependency" [{"artifactId" "junit",
                                                                                 "version" "4.13.1",
                                                                                 "scope" "test",
                                                                                 "groupId" "junit"}
                                                                                {"artifactId" "activejdbc",
                                                                                 "version" "${activejdbc.version}",
                                                                                 "exclusions" {"exclusion" {"groupId" "opensymphony",
                                                                                                            "artifactId" "oscache"}},
                                                                                 "groupId" "org.javalite"}
                                                                                {"version" "5.1.34",
                                                                                 "groupId" "mysql",
                                                                                 "artifactId" "mysql-connector-java"}
                                                                                {"version" "1.7.9",
                                                                                 "groupId" "org.slf4j",
                                                                                 "artifactId" "slf4j-simple"}]},
                                                  "packaging" "jar",
                                                  "name" "ActiveJDBC - Simple Maven Example",
                                                  "-schemaLocation" "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd",
                                                  "modelVersion" "4.0.0",
                                                  "build" {"plugins" {"plugin" [{"groupId" "org.apache.maven.plugins",
                                                                                 "artifactId" "maven-compiler-plugin",
                                                                                 "version" "3.6.0",
                                                                                 "configuration" {"source" "1.8",
                                                                                                  "target" "1.8",
                                                                                                  "encoding" "UTF-8"}}
                                                                                {"artifactId" "activejdbc-instrumentation",
                                                                                 "version" "${activejdbc.version}",
                                                                                 "executions" {"execution" {"phase" "process-classes",
                                                                                                            "goals" {"goal" "instrument"}}},
                                                                                 "groupId" "org.javalite"}
                                                                                {"executions" {"execution" {"id" "dev_migrations",
                                                                                                            "phase" "validate",
                                                                                                            "goals" {"goal" "migrate"}}},
                                                                                 "dependencies" {"dependency" {"artifactId" "mysql-connector-java",
                                                                                                               "version" "5.1.34",
                                                                                                               "groupId" "mysql"}},
                                                                                 "groupId" "org.javalite",
                                                                                 "artifactId" "db-migrator-maven-plugin",
                                                                                 "version" "${activejdbc.version}",
                                                                                 "configuration" {"configFile" "${project.basedir}/src/main/resources/database.properties",
                                                                                                  "environments" "${environments}"}}
                                                                                {"configuration" {"excludes" {"exclude" ["**/helpers/*"
                                                                                                                         "**/*$*"]},
                                                                                                  "reportFormat" "brief",
                                                                                                  "trimStackTrace" "true",
                                                                                                  "useFile" "false",
                                                                                                  "includes" {"include" ["**/*Spec*.java"
                                                                                                                         "**/*Test*.java"]}},
                                                                                 "groupId" "org.apache.maven.plugins",
                                                                                 "artifactId" "maven-surefire-plugin",
                                                                                 "version" "2.18.1"}]}},
                                                  "-xmlns" "http://maven.apache.org/POM/4.0.0",
                                                  "version" "1.0-SNAPSHOT",
                                                  "-xsi" "http://www.w3.org/2001/XMLSchema-instance",
                                                  "artifactId" "simple-example"}},
            "test-resources/test.json" {:hello [1 2 4], "@foo" "bar"}}
           (api/parse "test-resources")
           (api/parse "test-resources/")
           (api/parse "test-resources" "test-resources/" "test-resources/*{.yml,.dockerignore,.env,.toml,.gitignore,.vcl,.Dockerfile,.cue,.jsonnet,.yaml,.edn,.spdx,.ini,.hcl1.tf,.hcl2.tf,.properties,.xml,.json}"))))
  (testing "yaml"
    (testing "support multi-documents"
      (is (= {"test-resources/yaml/combine.yaml" [#ordered/map([:apiVersion "apps/v1"]
                                                               [:kind "Deployment"]
                                                               [:metadata #ordered/map([:name "hello-kubernetes"])]
                                                               [:spec
                                                                #ordered/map([:replicas 3]
                                                                             [:selector
                                                                              #ordered/map([:matchLabels
                                                                                            #ordered/map([:app "hello-kubernetes"])])]
                                                                             [:template
                                                                              #ordered/map([:metadata
                                                                                            #ordered/map([:labels
                                                                                                          #ordered/map([:app
                                                                                                                        "hello-kubernetes"])])]
                                                                                           [:spec
                                                                                            #ordered/map([:containers
                                                                                                          [#ordered/map([:name
                                                                                                                         "hello-kubernetes"]
                                                                                                                        [:image
                                                                                                                         "paulbouwer/hello-kubernetes:1.5"]
                                                                                                                        [:ports
                                                                                                                         [#ordered/map([:containerPort
                                                                                                                                        8080])]])]])])])])
                                                  #ordered/map([:apiVersion "apps/v1"]
                                                               [:kind "Deployment"]
                                                               [:metadata #ordered/map([:name "goodbye-kubernetes"])]
                                                               [:spec
                                                                #ordered/map([:replicas 3]
                                                                             [:selector
                                                                              #ordered/map([:matchLabels
                                                                                            #ordered/map([:app "goodbye-kubernetes"])])]
                                                                             [:template
                                                                              #ordered/map([:metadata
                                                                                            #ordered/map([:labels
                                                                                                          #ordered/map([:app
                                                                                                                        "goodbye-kubernetes"])])]
                                                                                           [:spec
                                                                                            #ordered/map([:containers
                                                                                                          [#ordered/map([:name
                                                                                                                         "goodbye-kubernetes"]
                                                                                                                        [:image
                                                                                                                         "paulbouwer/hello-kubernetes:1.5"]
                                                                                                                        [:ports
                                                                                                                         [#ordered/map([:containerPort
                                                                                                                                        8080])]])]])])])])
                                                  #ordered/map([:apiVersion "v1"]
                                                               [:kind "Service"]
                                                               [:metadata #ordered/map([:name "hello-kubernetes"])]
                                                               [:spec
                                                                #ordered/map([:type "LoadBalancer"]
                                                                             [:ports [#ordered/map([:port 80] [:targetPort 8080])]]
                                                                             [:selector #ordered/map([:app "hello-kubernetes"])])])]}
             (api/parse "test-resources/yaml/combine.yaml"))))
    (testing "tolerate unknown tags"
      (is (= {"test-resources/yaml/lambda.yaml" #ordered/map([:AWSTemplateFormatVersion "2010-09-09"]
                                                             [:Transform "AWS::Serverless-2016-10-31"]
                                                             [:Description
                                                              "proccess loadnewpackages events send to logLocationFinder."]
                                                             [:Parameters
                                                              #ordered/map([:DatadogLambda
                                                                            #ordered/map([:Type "String"]
                                                                                         [:Default
                                                                                          "arn:aws:lambda:us-east-1:12312312312312:function:datadog-log-forwarder"])])]
                                                             [:Resources
                                                              #ordered/map([:LambdaFunction
                                                                            #ordered/map([:Type "AWS::Serverless::Function"]
                                                                                         [:Properties
                                                                                          #ordered/map([:Handler "main.handler"]
                                                                                                       [:Runtime "python2.7"]
                                                                                                       [:Environment
                                                                                                        #ordered/map([:Variables
                                                                                                                      ["dbhost:mydb"
                                                                                                                       "dbuser:root"
                                                                                                                       "dbpassword:mypassword"]])]
                                                                                                       [:CodeUri "dist"]
                                                                                                       [:MemorySize 128]
                                                                                                       [:Timeout 300]
                                                                                                       [:Policies
                                                                                                        [#ordered/map([:Statement
                                                                                                                       [#ordered/map([:Action
                                                                                                                                      ["sqs:*"
                                                                                                                                       "logs:CreateLogGroup"
                                                                                                                                       "logs:CreateLogStream"
                                                                                                                                       "lambda:put"]]
                                                                                                                                     [:Effect
                                                                                                                                      "Allow"]
                                                                                                                                     [:Resource
                                                                                                                                      ["arn:aws:sqs:us-east-1:12321312312:vuln_search_eng_package_names{{ENV}}"
                                                                                                                                       "arn:aws:sqs:us-east-1:12312312312:vuln_search_eng_package_repos{{ENV}}"]])]])
                                                                                                         #ordered/map([:Statement
                                                                                                                       [#ordered/map([:Action
                                                                                                                                      "*"]
                                                                                                                                     [:Effect
                                                                                                                                      "Allow"]
                                                                                                                                     [:Resource
                                                                                                                                      ["arn:aws:sqs:us-east-1:12321312312:vuln_search_eng_package_names{{ENV}}"
                                                                                                                                       "arn:aws:sqs:us-east-1:12312312312:vuln_search_eng_package_repos{{ENV}}"]])]])
                                                                                                         #ordered/map([:Statement
                                                                                                                       [#ordered/map([:Action
                                                                                                                                      ["sqs:Read"]]
                                                                                                                                     [:Effect
                                                                                                                                      "Allow"]
                                                                                                                                     [:Resource
                                                                                                                                      "*"])]])]]
                                                                                                       [:Events
                                                                                                        #ordered/map([:Stream
                                                                                                                      #ordered/map([:Type
                                                                                                                                    "SQS"]
                                                                                                                                   [:Properties
                                                                                                                                    #ordered/map([:Queue
                                                                                                                                                  "arn:aws:sqs:us-east-1:321321312:vuln_search_eng_package_names{{ENV}}"]
                                                                                                                                                 [:BatchSize
                                                                                                                                                  1])])])])])]
                                                                           [:DatadogLambdaLogGroupPermission
                                                                            #ordered/map([:Type "AWS::Lambda::Permission"]
                                                                                         [:Properties
                                                                                          #ordered/map([:Action
                                                                                                        "lambda:InvokeFunction"]
                                                                                                       [:FunctionName
                                                                                                        "DatadogLambda"]
                                                                                                       [:Principal
                                                                                                        "logs.us-east-1.amazonaws.com"]
                                                                                                       [:SourceArn
                                                                                                        "LambdaFunctionLogGroup.Arn"])])]
                                                                           [:LambdaFunctionLogGroup
                                                                            #ordered/map([:DependsOn "LambdaFunction"]
                                                                                         [:Properties
                                                                                          #ordered/map([:LogGroupName
                                                                                                        #ordered/map(["Fn::Join"
                                                                                                                      [""
                                                                                                                       ["/aws/lambda/"
                                                                                                                        #ordered/map([:Ref
                                                                                                                                      "LambdaFunction"])]]])]
                                                                                                       [:RetentionInDays 14])]
                                                                                         [:Type "AWS::Logs::LogGroup"])]
                                                                           [:LambdaSubscriptionFilter
                                                                            #ordered/map([:Type "AWS::Logs::SubscriptionFilter"]
                                                                                         [:Properties
                                                                                          #ordered/map([:LogGroupName
                                                                                                        "LambdaFunctionLogGroup"]
                                                                                                       [:DestinationArn
                                                                                                        "DatadogLambda"]
                                                                                                       [:FilterPattern ""])])])])}
             (api/parse "test-resources/yaml/lambda.yaml")))))
  (testing "keywordize"
    (testing "clojure parsers use keyworded results by default"
      (is (= {"test-resources/test.json" {:hello [1 2 4], "@foo" "bar"}}
             (api/parse "test-resources/test.json")))
      (testing "you can \"escape\" keywordization for clojure parsers via false `keywordize?`"
        (is (= {"test-resources/test.json" {"hello" [1 2 4], "@foo" "bar"}}
               (api/parse* {:keywordize? false} "test-resources/test.json")))))
    (testing "go parsers use non-keyworded results by default"
      (is (= #{"apiVersion" "kind" "metadata" "spec"}
             (into (hash-set)
                   (-> (api/parse "test-resources/test.cue")
                       (get "test-resources/test.cue")
                       (keys))))))
    (testing "keyworded go parser results"
      (testing "cue"
        (is (= {"test-resources/test.cue" {:apiVersion "apps/v1",
                                           :kind "Deployment",
                                           :metadata {:name "hello-kubernetes"},
                                           :spec {:replicas 3.0,
                                                  :selector {:matchLabels {:app "hello-kubernetes"}},
                                                  :template {:metadata {:labels {:app "hello-kubernetes"}},
                                                             :spec {:containers [{:name "hello-kubernetes",
                                                                                  :image "paulbouwer/hello-kubernetes:1.5",
                                                                                  :ports [{:containerPort 8081.0}]}]}}}}}
               (api/parse-go* {:keywordize? true} "test-resources/test.cue")
               (api/parse-as* {:keywordize? true} "cue" "test-resources/test.cue")
               (api/parse-go-as* {:keywordize? true} "cue" "test-resources/test.cue"))))
      (testing "edn"
        (is (= {"test-resources/test.edn" {:foo ":bar", :duration "#duration 20m"}}
               (api/parse-go* {:keywordize? true} "test-resources/test.edn")
               (api/parse-go-as* {:keywordize? true} "edn" "test-resources/test.edn"))))
      (testing "hocon"
        (= {"test-resources/hocon/hocon.conf" {:play {:editor "<<unknown value>>",
                                                      :server {:https {:keyStore {:algorithm "<<unknown value>>",
                                                                                  :password "\"\"",
                                                                                  :path "<<unknown value>>",
                                                                                  :type "JKS"},
                                                                       :needClientAuth false,
                                                                       :port "<<unknown value>>",
                                                                       :trustStore {:noCaVerification false},
                                                                       :wantClientAuth false,
                                                                       :address "0.0.0.0",
                                                                       :engineProvider "play.core.server.ssl.DefaultSSLEngineProvider",
                                                                       :idleTimeout "\"75 seconds\""},
                                                               :pidfile {:path "<<unknown value>>"},
                                                               :websocket {:frame {:maxLength "64k"}},
                                                               :debug {:addDebugInfoToRequests false},
                                                               :dir "<<unknown value>>",
                                                               :http {:address "0.0.0.0",
                                                                      :idleTimeout "\"75 seconds\"",
                                                                      :port 9001.0}}}}}
           #_(api/parse-go* {:keywordize? true} "test-resources/hocon/hocon.conf") ; hocon needs explicit parser
           (api/parse-as* {:keywordize? true} "hocon" "test-resources/hocon/hocon.conf")
           (api/parse-go-as* {:keywordize? true} "hocon" "test-resources/hocon/hocon.conf")))
      (testing "dotenv"
        (is (= {"test-resources/test.env" {:APP_NAME "test", :MYSQL_USER "user2"}}
               (api/parse-go* {:keywordize? true} "test-resources/test.env")
               (api/parse-as* {:keywordize? true} "dotenv" "test-resources/test.env")
               (api/parse-go-as* {:keywordize? true} "dotenv" "test-resources/test.env"))))
      (testing "jsonnet"
        (is (= {"test-resources/test.jsonnet" {:ex1 1.6666666666666665,
                                               :str4 "ex1=1.67, ex2=3.00",
                                               :obj_member true,
                                               :str1 "The value of self.ex2 is 3.",
                                               :ex4 true,
                                               :concat_string "1234",
                                               :ex2 3.0,
                                               :str2 "The value of self.ex2 is 3.",
                                               :str3 "ex1=1.67, ex2=3.00",
                                               :concat_array [1.0 2.0 3.0 4.0],
                                               :obj {:c 4.0, :a 1.0, :b 3.0},
                                               :ex3 1.6666666666666665,
                                               :equality1 false,
                                               :equality2 true,
                                               :str5 "ex1=1.67\nex2=3.00\n"}}
               (api/parse-go* {:keywordize? true} "test-resources/test.jsonnet")
               (api/parse-as* {:keywordize? true} "jsonnet" "test-resources/test.jsonnet")
               (api/parse-go-as* {:keywordize? true} "jsonnet" "test-resources/test.jsonnet"))))
      (testing "spdx"
        (is (= {"test-resources/test.spdx" {:spdxVersion "SPDX-2.3",
                                            :dataLicense "conftest-demo",
                                            :SPDXID "SPDXRef-DOCUMENT",
                                            :name "hello",
                                            :documentNamespace "https://swinslow.net/spdx-examples/example1/hello-v3",
                                            :creationInfo {:creators ["Person: Steve Winslow (steve@swinslow.net)"
                                                                      "Tool: github.com/spdx/tools-golang/builder"
                                                                      "Tool: github.com/spdx/tools-golang/idsearcher"],
                                                           :created "2021-08-26T01:46:00Z"}}}
               (api/parse-go* {:keywordize? true} "test-resources/test.spdx")
               (api/parse-as* {:keywordize? true} "spdx" "test-resources/test.spdx")
               (api/parse-go-as* {:keywordize? true} "spdx" "test-resources/test.spdx"))))
      (testing "vcl"
        (is (= {"test-resources/test.vcl" {:backend {:app {:port "8081",
                                                           :between_bytes_timeout 60.0,
                                                           :connect_timeout 60.0,
                                                           :first_byte_timeout 60.0,
                                                           :host "127.0.0.1",
                                                           :max_connections 800.0}},
                                           :acl {:purge ["127.0.0.1"]}}}
               (api/parse-go* {:keywordize? true} "test-resources/test.vcl")
               (api/parse-as* {:keywordize? true} "vcl" "test-resources/test.vcl")
               (api/parse-go-as* {:keywordize? true} "vcl" "test-resources/test.vcl"))))
      (testing "dockerfile"
        (is (= {"test-resources/test.Dockerfile" [[{:SubCmd "",
                                                    :JSON false,
                                                    :Flags [],
                                                    :Value ["openjdk:8-jdk-alpine"],
                                                    :Stage 0.0,
                                                    :Cmd "from"}
                                                   {:Flags [], :Value ["/tmp"], :Stage 0.0, :Cmd "volume", :SubCmd "", :JSON false}
                                                   {:SubCmd "",
                                                    :JSON false,
                                                    :Flags [],
                                                    :Value ["DEPENDENCY=target/dependency"],
                                                    :Stage 0.0,
                                                    :Cmd "arg"}
                                                   {:Cmd "copy",
                                                    :SubCmd "",
                                                    :JSON false,
                                                    :Flags [],
                                                    :Value ["${DEPENDENCY}/BOOT-INF/lib" "/app/lib"],
                                                    :Stage 0.0}
                                                   {:Cmd "copy",
                                                    :SubCmd "",
                                                    :JSON false,
                                                    :Flags [],
                                                    :Value ["${DEPENDENCY}/META-INF" "/app/META-INF"],
                                                    :Stage 0.0}
                                                   {:Cmd "copy",
                                                    :SubCmd "",
                                                    :JSON false,
                                                    :Flags [],
                                                    :Value ["${DEPENDENCY}/BOOT-INF/classes" "/app"],
                                                    :Stage 0.0}
                                                   {:Flags [],
                                                    :Value ["apk add --no-cache python3 python3-dev build-base && pip3 install awscli==1.18.1"],
                                                    :Stage 0.0,
                                                    :Cmd "run",
                                                    :SubCmd "",
                                                    :JSON false}
                                                   {:Flags [],
                                                    :Value ["java" "-cp" "app:app/lib/*" "hello.Application"],
                                                    :Stage 0.0,
                                                    :Cmd "entrypoint",
                                                    :SubCmd "",
                                                    :JSON true}]]}
               (api/parse-go* {:keywordize? true} "test-resources/test.Dockerfile")
               (api/parse-as* {:keywordize? true} "dockerfile" "test-resources/test.Dockerfile")
               (api/parse-go-as* {:keywordize? true} "dockerfile" "test-resources/test.Dockerfile"))))
      (testing "ignore"
        (testing "dockerignore"
          (is (= {"test-resources/test.dockerignore" [[{:Kind "Path", :Value ".idea", :Original ".idea"}
                                                       {:Value "", :Original "", :Kind "Empty"}]]}
                 (api/parse-go* {:keywordize? true} "test-resources/test.dockerignore")
                 (api/parse-as* {:keywordize? true} "ignore" "test-resources/test.dockerignore")
                 (api/parse-go-as* {:keywordize? true} "ignore" "test-resources/test.dockerignore"))))
        (testing "gitignore"
          (is (= {"test-resources/test.gitignore" [[{:Kind "Path", :Value "foo", :Original "foo"}
                                                    {:Kind "NegatedPath", :Value "bar", :Original "!bar"}
                                                    {:Kind "Empty", :Value "", :Original ""}
                                                    {:Kind "Comment", :Value "Baz", :Original "# Baz"}
                                                    {:Kind "Path", :Value "qux/", :Original "qux/"}
                                                    {:Kind "Empty", :Value "", :Original ""}]]}
                 (api/parse-go* {:keywordize? true} "test-resources/test.gitignore")
                 (api/parse-as* {:keywordize? true} "ignore" "test-resources/test.gitignore")
                 (api/parse-go-as* {:keywordize? true} "ignore" "test-resources/test.gitignore")))))
      (testing "properties"
        (is (= {"test-resources/test.properties" {:other.value.url "https://example.com/",
                                                  :secret.value.exception "f9761ebe-d4dc-11eb-8046-1e00e20cdb95",
                                                  :SAMPLE_VALUE "something-here"}}
               (api/parse-go* {:keywordize? true} "test-resources/test.properties")
               (api/parse-as* {:keywordize? true} "properties" "test-resources/test.properties")
               (api/parse-go-as* {:keywordize? true} "properties" "test-resources/test.properties"))))
      (testing "toml"
        (is (= {"test-resources/test.toml" {:defaultEntryPoints ["http" "https"],
                                            :entryPoints {:http {:proxyProtocol {:insecure true,
                                                                                 :trustedIPs ["10.10.10.1" "10.10.10.2"]},
                                                                 :forwardedHeaders {:trustedIPs ["10.10.10.1" "10.10.10.2"]},
                                                                 :address ":80",
                                                                 :compress true,
                                                                 :whitelist {:sourceRange ["10.42.0.0/16"
                                                                                           "152.89.1.33/32"
                                                                                           "afed:be44::/16"],
                                                                             :useXForwardedFor true},
                                                                 :tls {:minVersion "VersionTLS12",
                                                                       :cipherSuites ["TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
                                                                                      "TLS_RSA_WITH_AES_256_GCM_SHA384"],
                                                                       :certificates [{:certFile "path/to/my.cert",
                                                                                       :keyFile "path/to/my.key"}
                                                                                      {:certFile "path/to/other.cert",
                                                                                       :keyFile "path/to/other.key"}],
                                                                       :clientCA {:files ["path/to/ca1.crt" "path/to/ca2.crt"],
                                                                                  :optional false}},
                                                                 :redirect {:entryPoint "https",
                                                                            :regex "^http://localhost/(.*)",
                                                                            :replacement "http://mydomain/$1",
                                                                            :permanent true},
                                                                 :auth {:digest {:removeHeader true,
                                                                                 :users ["test:traefik:a2688e031edb4be6a3797f3882655c05"
                                                                                         "test2:traefik:518845800f9e2bfb1f1f740ec24f074e"],
                                                                                 :usersFile "/path/to/.htdigest"},
                                                                        :forward {:address "https://authserver.com/auth",
                                                                                  :trustForwardHeader true,
                                                                                  :authResponseHeaders ["X-Auth-User"],
                                                                                  :tls {:caOptional true,
                                                                                        :cert "path/to/foo.cert",
                                                                                        :key "path/to/foo.key",
                                                                                        :insecureSkipVerify true,
                                                                                        :ca "path/to/local.crt"}},
                                                                        :headerField "X-WebAuth-User",
                                                                        :basic {:removeHeader true,
                                                                                :users ["test:$apr1$H6uskkkW$IgXLP6ewTrSuBkTrqE8wj/"
                                                                                        "test2:$apr1$d9hr9HBB$4HxwgUir3HP4EsggP/QNo0"],
                                                                                :usersFile "/path/to/.htpasswd"}}}}}}
               (api/parse-go* {:keywordize? true} "test-resources/test.toml")
               (api/parse-as* {:keywordize? true} "toml" "test-resources/test.toml")
               (api/parse-go-as* {:keywordize? true} "toml" "test-resources/test.toml"))))
      (testing "xml"
        (is (= {"test-resources/test.xml" {:project {:properties {:environments "development.test,development",
                                                                  :activejdbc.version "2.3"},
                                                     :groupId "org.javalite",
                                                     :packaging "jar",
                                                     :name "ActiveJDBC - Simple Maven Example",
                                                     :-schemaLocation "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd",
                                                     :-xmlns "http://maven.apache.org/POM/4.0.0",
                                                     :build {:plugins {:plugin [{:configuration {:source "1.8",
                                                                                                 :target "1.8",
                                                                                                 :encoding "UTF-8"},
                                                                                 :groupId "org.apache.maven.plugins",
                                                                                 :artifactId "maven-compiler-plugin",
                                                                                 :version "3.6.0"}
                                                                                {:executions {:execution {:phase "process-classes",
                                                                                                          :goals {:goal "instrument"}}},
                                                                                 :groupId "org.javalite",
                                                                                 :artifactId "activejdbc-instrumentation",
                                                                                 :version "${activejdbc.version}"}
                                                                                {:artifactId "db-migrator-maven-plugin",
                                                                                 :version "${activejdbc.version}",
                                                                                 :configuration {:configFile "${project.basedir}/src/main/resources/database.properties",
                                                                                                 :environments "${environments}"},
                                                                                 :executions {:execution {:goals {:goal "migrate"},
                                                                                                          :id "dev_migrations",
                                                                                                          :phase "validate"}},
                                                                                 :dependencies {:dependency {:groupId "mysql",
                                                                                                             :artifactId "mysql-connector-java",
                                                                                                             :version "5.1.34"}},
                                                                                 :groupId "org.javalite"}
                                                                                {:configuration {:useFile "false",
                                                                                                 :includes {:include ["**/*Spec*.java"
                                                                                                                      "**/*Test*.java"]},
                                                                                                 :excludes {:exclude ["**/helpers/*"
                                                                                                                      "**/*$*"]},
                                                                                                 :reportFormat "brief",
                                                                                                 :trimStackTrace "true"},
                                                                                 :groupId "org.apache.maven.plugins",
                                                                                 :artifactId "maven-surefire-plugin",
                                                                                 :version "2.18.1"}]}},
                                                     :artifactId "simple-example",
                                                     :version "1.0-SNAPSHOT",
                                                     :dependencies {:dependency [{:groupId "junit",
                                                                                  :artifactId "junit",
                                                                                  :version "4.13.1",
                                                                                  :scope "test"}
                                                                                 {:exclusions {:exclusion {:groupId "opensymphony",
                                                                                                           :artifactId "oscache"}},
                                                                                  :groupId "org.javalite",
                                                                                  :artifactId "activejdbc",
                                                                                  :version "${activejdbc.version}"}
                                                                                 {:artifactId "mysql-connector-java",
                                                                                  :version "5.1.34",
                                                                                  :groupId "mysql"}
                                                                                 {:groupId "org.slf4j",
                                                                                  :artifactId "slf4j-simple",
                                                                                  :version "1.7.9"}]},
                                                     :modelVersion "4.0.0",
                                                     :-xsi "http://www.w3.org/2001/XMLSchema-instance"}}}
               (api/parse-go* {:keywordize? true} "test-resources/test.xml")
               (api/parse-as* {:keywordize? true} "xml" "test-resources/test.xml")
               (api/parse-go-as* {:keywordize? true} "xml" "test-resources/test.xml"))))
      (testing "hcl1"
        (is (= {"test-resources/test.hcl1.tf" {:provider [{:google [{:version "2.5.0", :project "instrumenta", :region "europe-west2"}]}],
                                               :resource [{:google_container_cluster [{:primary [{:initial_node_count 1,
                                                                                                  :master_auth [{:username "",
                                                                                                                 :password ""}],
                                                                                                  :name "my-gke-cluster",
                                                                                                  :location "us-central1",
                                                                                                  :remove_default_node_pool true}]}]}
                                                          {:google_container_node_pool [{:primary_preemptible_nodes [{:name "my-node-pool",
                                                                                                                      :location "us-central1",
                                                                                                                      :cluster "${google_container_cluster.primary.name}",
                                                                                                                      :node_count 1,
                                                                                                                      :node_config [{:preemptible true,
                                                                                                                                     :machine_type "n1-standard-1",
                                                                                                                                     :metadata [{:disable-legacy-endpoints "true"}],
                                                                                                                                     :oauth_scopes ["https://www.googleapis.com/auth/logging.write"
                                                                                                                                                    "https://www.googleapis.com/auth/monitoring"]}]}]}]}],
                                               :output [{:client_certificate [{:value "${google_container_cluster.primary.master_auth.0.client_certificate}"}]}
                                                        {:client_key [{:value "${google_container_cluster.primary.master_auth.0.client_key}"}]}
                                                        {:cluster_ca_certificate [{:value "${google_container_cluster.primary.master_auth.0.cluster_ca_certificate}"}]}]}}
               #_(api/parse-go* {:keywordize? true} "test-resources/test.hcl1.tf") ; interpreted as hcl2
               (api/parse-as* {:keywordize? true} "hcl1" "test-resources/test.hcl1.tf")
               (api/parse-go-as* {:keywordize? true} "hcl1" "test-resources/test.hcl1.tf"))))
      (testing "hcl2"
        (is (= {"test-resources/test.hcl2.tf" {:resource {:aws_alb_listener {:my-alb-listener {:port "80", :protocol "HTTP"}},
                                                          :aws_db_security_group {:my-group {}},
                                                          :aws_s3_bucket {:valid {:acl "private",
                                                                                  :bucket "validBucket",
                                                                                  :tags {:environment "prod", :owner "devops"}}},
                                                          :aws_security_group_rule {:my-rule {:cidr_blocks ["0.0.0.0/0"],
                                                                                              :type "ingress"}},
                                                          :azurerm_managed_disk {:source {:encryption_settings {:enabled false}}}}}}
               (api/parse-go* {:keywordize? true} "test-resources/test.hcl2.tf")
               (api/parse-as* {:keywordize? true} "hcl2" "test-resources/test.hcl2.tf")
               (api/parse-go-as* {:keywordize? true} "hcl2" "test-resources/test.hcl2.tf")))))))