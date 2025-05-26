(ns api-test
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [pod-ilmoraunio-conjtest.api :as api]))

(defn create-file
  []
  (fs/create-file "../file.test"))

(defn delete-file
  []
  (fs/delete "../file.test"))

(defn file-fixture
  [f]
  (create-file)
  (f)
  (delete-file))

(def project-dir
  (last (str/split (str (fs/cwd)) #"/")))

(use-fixtures :once file-fixture)

(defn ls-files
  [& args]
  (into #{} (map str (apply api/ls-files args))))

(deftest ls-files-test
  (testing "relative paths"
    (testing "no directory wildcard"
      (testing "static filename"
        (is (= #{"test-resources/test.json"}
               (ls-files "test-resources/test.json"))))
      (testing "wildcard in filename"
        (is (= #{"test-resources/test.json"}
               (ls-files "test-resources/*.json"))))
      (testing "no filename, directory given"
        (is (= #{"./test.bb.edn"
                 "./cli-test.bb.edn"
                 "./LICENSE"
                 "./CHANGELOG.md"
                 "./Makefile"
                 "./.gitmodules"
                 "./README.md"
                 "./.gitignore"
                 "./bb.edn"
                 "./bb.ci.edn"}
               (ls-files ".")))
        (is (= #{"test-resources/test.hcl2.tf"
                 "test-resources/test.yaml"
                 "test-resources/test.yml"
                 "test-resources/test.jsonnet"
                 "test-resources/test.spdx"
                 "test-resources/test.ini"
                 "test-resources/test.json"
                 "test-resources/test.dockerignore"
                 "test-resources/test.env"
                 "test-resources/test.properties"
                 "test-resources/test.xml"
                 "test-resources/test.Dockerfile"
                 "test-resources/test.hcl1.tf"
                 "test-resources/test.cue"
                 "test-resources/test.gitignore"
                 "test-resources/test.vcl"
                 "test-resources/test.toml"
                 "test-resources/test.edn"}
               (ls-files "test-resources")))))
    (testing "wildcard in directory"
      (testing "static filename"
        (is (= #{"test-resources/test.json"}
               (ls-files "*/test.json")))
        (is (= #{"pod-ilmoraunio-conftest/test-resources/test.json" "test-resources/test.json"}
               (ls-files "**/test.json"))))
      (testing "wildcard in filename"
        (is (= #{"test-resources/test.json"}
               (ls-files "*/*.json")))
        (is (= #{"pod-ilmoraunio-conftest/test-resources/test.json" "test-resources/test.json"}
               (ls-files "**/*.json")))
        (is (= #{"pod-ilmoraunio-conftest/test-resources/hocon.conf" "test-resources/hocon/hocon.conf"}
               (ls-files "**/hocon*"))))
      (testing "no filename, directory given"
        (is (= #{"test-resources/yaml/combine.yaml"
                 "test-resources/yaml/lambda.yaml"}
               (ls-files "**/yaml")))))
    (testing "current directory"
      (testing "static filename"
        (is (= #{"bb.edn"}
               (ls-files "bb.edn"))))
      (testing "wildcard in filename"
        (is (= #{"bb.ci.edn" "bb.edn" "cli-test.bb.edn" "test.bb.edn"}
               (ls-files "*.edn"))))))

  (testing "relative, backtracking paths"
    (testing "no directory wildcard"
      (testing "static filename"
        (is (= #{"../file.test"}
               (ls-files "../file.test")))
        (is (= #{"bb.edn"}
               (ls-files "test-resources/../bb.edn"))))
      (testing "wildcard in filename"
        (is (= #{"../file.test"}
               (ls-files "../*.test")))
        (is (= #{"bb.ci.edn" "bb.edn" "cli-test.bb.edn" "test.bb.edn"}
               (ls-files "test-resources/../*.edn"))))
      (testing "no filename, directory given"
        (is (= #{(format "../%s/test-resources/yaml/combine.yaml" project-dir)
                 (format "../%s/test-resources/yaml/lambda.yaml" project-dir)}
               (ls-files (format "../%s/test-resources/yaml" project-dir))))))
    (testing "wildcard in directory"
      (testing "static filename"
        (is (= #{"test-resources/hocon/hocon.conf"}
               (ls-files "../*/test-resources/hocon/hocon.conf")))
        (is (= #{"pod-ilmoraunio-conftest/test-resources/hocon.conf" "test-resources/hocon/hocon.conf"}
               (ls-files (format "../%s/**/hocon.conf" project-dir)))))
      (testing "wildcard in filename"
        (is (= #{"test-resources/hocon/hocon.conf"}
               (ls-files "../*/test-resources/hocon/*.conf")))
        (is (= #{"pod-ilmoraunio-conftest/test-resources/hocon.conf" "test-resources/hocon/hocon.conf"}
               (ls-files (format "../%s/**/*.conf" project-dir)))))
      (testing "no filename, directory given"
        (is (= #{"test-resources/yaml/combine.yaml"
                 "test-resources/yaml/lambda.yaml"}
               (ls-files "../pod-ilmoraunio-conjtest/**/yaml")))
        (is (= #{"test-resources/hocon/hocon.conf"
                 "test-resources/test.Dockerfile"
                 "test-resources/test.cue"
                 "test-resources/test.dockerignore"
                 "test-resources/test.edn"
                 "test-resources/test.env"
                 "test-resources/test.gitignore"
                 "test-resources/test.hcl1.tf"
                 "test-resources/test.hcl2.tf"
                 "test-resources/test.ini"
                 "test-resources/test.json"
                 "test-resources/test.jsonnet"
                 "test-resources/test.properties"
                 "test-resources/test.spdx"
                 "test-resources/test.toml"
                 "test-resources/test.vcl"
                 "test-resources/test.xml"
                 "test-resources/test.yaml"
                 "test-resources/test.yml"
                 "test-resources/yaml/combine.yaml"
                 "test-resources/yaml/lambda.yaml"}
               (ls-files (format "../%s/test-resources/**/" project-dir)))))))

  (testing "absolute paths"
    (testing "no directory wildcard"
      (testing "static filename"
        (is (= #{"bb.edn"}
               (ls-files (str (fs/cwd) "/bb.edn")))))
      (testing "wildcard in filename"
        (is (= #{"bb.ci.edn" "bb.edn" "cli-test.bb.edn" "test.bb.edn"}
               (ls-files (str (fs/cwd) "/*.edn")))))
      (testing "no filename, directory given"
        (let [dir-from-root (str (fs/cwd) "/test-resources")]
          (is (= #{(str dir-from-root "/test.hcl2.tf")
                   (str dir-from-root "/test.yaml")
                   (str dir-from-root "/test.yml")
                   (str dir-from-root "/test.jsonnet")
                   (str dir-from-root "/test.spdx")
                   (str dir-from-root "/test.ini")
                   (str dir-from-root "/test.json")
                   (str dir-from-root "/test.dockerignore")
                   (str dir-from-root "/test.env")
                   (str dir-from-root "/test.properties")
                   (str dir-from-root "/test.xml")
                   (str dir-from-root "/test.Dockerfile")
                   (str dir-from-root "/test.hcl1.tf")
                   (str dir-from-root "/test.cue")
                   (str dir-from-root "/test.gitignore")
                   (str dir-from-root "/test.vcl")
                   (str dir-from-root "/test.toml")
                   (str dir-from-root "/test.edn")}
                 (ls-files dir-from-root))))))
    (testing "wildcard in directory"
      (testing "static filename"
        (is (= #{"test-resources/test.json"}
               (ls-files (str (fs/cwd) "/*/test.json"))))
        (is (= #{"pod-ilmoraunio-conftest/test-resources/test.json" "test-resources/test.json"}
               (ls-files (str (fs/cwd) "/**/test.json")))))
      (testing "wildcard in filename"
        (is (= #{"test-resources/test.json"}
               (ls-files (str (fs/cwd) "/*/*.json"))))
        (is (= #{"pod-ilmoraunio-conftest/test-resources/test.json" "test-resources/test.json"}
               (ls-files (str (fs/cwd) "/**/*.json"))))))
      (testing "no filename, directory given"
        (is (= #{"test-resources/hocon/hocon.conf"}
               (ls-files (str (fs/cwd) "/**/hocon")))))))