(ns api-test
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [pod-ilmoraunio-conjtest.api :as api]))

(defn ls-files
  [& args]
  (map str (apply api/ls-files args)))

(deftest ls-files-test
  (testing "relative paths"
    (testing "no directory wildcard"
      (testing "static filename"
        (is (= ["test-resources/test.json"]
               (ls-files "test-resources/test.json"))))
      (testing "wildcard in filename"
        (is (= ["test-resources/test.json"]
               (ls-files "test-resources/*.json"))))
      (testing "no filename, directory given"
        (is (= ["./test.bb.edn"
                "./LICENSE"
                "./CHANGELOG.md"
                "./deps.edn"
                "./Makefile"
                "./.gitmodules"
                "./README.md"
                "./.gitignore"
                "./package.json"
                "./bb.edn"
                "./bb.ci.edn"]
               (ls-files ".")))
        (is (= ["test-resources/test.yaml"
                "test-resources/test.yml"
                "test-resources/test.json"
                "test-resources/.dockerignore"
                "test-resources/test.edn"]
               (ls-files "test-resources")))))
    (testing "wildcard in directory"
      (testing "static filename"
        (is (= ["test-resources/test.json"]
               (ls-files "*/test.json")))
        (is (= ["pod-ilmoraunio-conftest/test-resources/test.json" "test-resources/test.json"]
               (ls-files "**/test.json"))))
      (testing "wildcard in filename"
        (is (= ["test-resources/test.json"]
               (ls-files "*/*.json")))
        (is (= ["pod-ilmoraunio-conftest/test-resources/test.json" "test-resources/test.json"]
               (ls-files "**/*.json"))))
      (testing "no filename, directory given"
        ;; FIXME
        (is (= ["test-resources/yaml/combine.yaml"
                "test-resources/yaml/lambda.yaml"]
               (ls-files "**/yaml")))))
    (testing "current directory"
      (testing "static filename"
        (is (= ["bb.edn"]
               (ls-files "bb.edn"))))
      (testing "wildcard in filename"
        (is (= ["test.bb.edn" "deps.edn" "bb.edn" "bb.ci.edn"]
               (ls-files "*.edn"))))))

  (testing "relative, backtracking paths"
    (fs/create-file "../file.test")
    (testing "no directory wildcard"
      (testing "static filename"
        ;; FIXME
        (is (= ["../file.test"]
               (ls-files "../file.test")))
        (is (= ["deps.edn"]
               (ls-files "test-resources/../deps.edn"))))
      (testing "wildcard in filename"
        (is (= ["../file.test"]
               (ls-files "../*.test")))
        (is (= ["test.bb.edn" "deps.edn" "bb.edn" "bb.ci.edn"]
               (ls-files "test-resources/../*.edn"))))
      (testing "no filename, directory given"
        (is (= ["../pod-ilmoraunio-conjtest/test-resources/yaml/combine.yaml"
                "../pod-ilmoraunio-conjtest/test-resources/yaml/lambda.yaml"]
               (ls-files "../pod-ilmoraunio-conjtest/test-resources/yaml")))))
    (testing "wildcard in directory"
      (testing "static filename"
        (is (= ["test-resources/hocon/hocon.conf"]
               (ls-files "../*/test-resources/hocon/hocon.conf")))
        (is (= ["pod-ilmoraunio-conftest/test-resources/hocon.conf" "test-resources/hocon/hocon.conf"]
               (ls-files "../pod-ilmoraunio-conjtest/**/hocon.conf"))))
      (testing "wildcard in filename"
        (is (= ["test-resources/hocon/hocon.conf"]
               (ls-files "../*/test-resources/hocon/*.conf")))
        (is (= ["pod-ilmoraunio-conftest/test-resources/hocon.conf" "test-resources/hocon/hocon.conf"]
               (ls-files "../pod-ilmoraunio-conjtest/**/*.conf"))))
      (testing "no filename, directory given"
        ;; FIXME
        (is (= ["test-resources/yaml/combine.yaml"
                "test-resources/yaml/lambda.yaml"]
               (ls-files "../pod-ilmoraunio-conjtest/**/yaml")))
        (is (= ["test-resources/test.yaml"
                "test-resources/test.yml"
                "test-resources/hocon/hocon.conf"
                "test-resources/test.json"
                "test-resources/.dockerignore"
                "test-resources/yaml/combine.yaml"
                "test-resources/yaml/lambda.yaml"
                "test-resources/test.edn"]
               (ls-files "../pod-ilmoraunio-conjtest/test-resources/**/")))))
    (fs/delete "../file.test"))

  (testing "absolute paths"
    (testing "no directory wildcard"
      (testing "static filename"
        (is (= ["deps.edn"]
               (ls-files (str (fs/cwd) "/deps.edn")))))
      (testing "wildcard in filename"
        (is (= ["test.bb.edn" "deps.edn" "bb.edn" "bb.ci.edn"]
               (ls-files (str (fs/cwd) "/*.edn")))))
      (testing "no filename, directory given"
        (is (= ["/Users/ilmo.raunio/Devel/personal/pod-ilmoraunio-conjtest/test-resources/test.yaml"
                "/Users/ilmo.raunio/Devel/personal/pod-ilmoraunio-conjtest/test-resources/test.yml"
                "/Users/ilmo.raunio/Devel/personal/pod-ilmoraunio-conjtest/test-resources/test.json"
                "/Users/ilmo.raunio/Devel/personal/pod-ilmoraunio-conjtest/test-resources/.dockerignore"
                "/Users/ilmo.raunio/Devel/personal/pod-ilmoraunio-conjtest/test-resources/test.edn"]
               (ls-files (str (fs/cwd) "/test-resources"))))))
    (testing "wildcard in directory"
      (testing "static filename"
        (is (= ["test-resources/test.json"]
               (ls-files (str (fs/cwd) "/*/test.json"))))
        (is (= ["pod-ilmoraunio-conftest/test-resources/test.json" "test-resources/test.json"]
               (ls-files (str (fs/cwd) "/**/test.json")))))
      (testing "wildcard in filename"
        (is (= ["test-resources/test.json"]
               (ls-files (str (fs/cwd) "/*/*.json"))))
        (is (= ["pod-ilmoraunio-conftest/test-resources/test.json" "test-resources/test.json"]
               (ls-files (str (fs/cwd) "/**/*.json"))))))
      (testing "no filename, directory given"
        ;; FIXME
        (is (= ["/Users/ilmo.raunio/Devel/personal/pod-ilmoraunio-conjtest/test-resources/hocon/hocon.conf"]
               (ls-files (str (fs/cwd) "/**/hocon")))))))