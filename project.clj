(defproject sg.dex/starfish-clj "0.9.0-SNAPSHOT"
  :url "https://github.com/DEX-Company/starfish-clj"
  :dependencies [[sg.dex/starfish-java "0.8.2-SNAPSHOT"]
                 [org.clojure/data.json "1.0.0"]
                 [org.clojure/data.csv "1.0.0"]
                 [clojurewerkz/propertied "1.3.0"]

                 ;; Used to fix JCL issues with Apache HTTP logging via JCL
                 [org.slf4j/jcl-over-slf4j "1.7.30"]]

  ;; :javac-options ["-target" "8", "-source" "8"] ; TODO figure out of this is helpful? Causes a warning
  :target-path "target/%s/"
  :java-source-paths ["src/main/java"]
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure" "src/test/java"]
  :test-selectors {:default (complement :integration)
                   :integration :integration}
  :plugins [[lein-codox "0.10.7"]
            [lein-ancient "0.6.15"]]
  :codox {:output-path "codox"}
  :min-lein-version "2.8.1"
  :lein-release {:deploy-via :clojars}

  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]

  :profiles {:dev {:source-paths ["src/dev"]
                   :resource-paths ["src/test/resources"]
                   :dependencies [[org.clojure/clojure "1.10.1"]
                                  [org.clojure/tools.namespace "1.0.0"]
                                  [net.mikera/cljunit "0.7.0" :scope "test"]]}

             :test {:dependencies []
                    :java-source-paths ["src/main/java" "src/test/java"]
                    :resource-paths ["src/main/resources" "src/test/resources"]}})
