(defproject sg.dex/starfish-clj "0.7.7"
  :url "https://github.com/DEX-Company/starfish-clj"
  :dependencies [[sg.dex/starfish-java "0.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.csv "0.1.4"]
                 [clojurewerkz/propertied "1.3.0"]

                 ;; Used to fix JCL issues with Apache HTTP logging via JCL
                 [org.slf4j/jcl-over-slf4j "1.7.28"]]

  ;; :javac-options ["-target" "8", "-source" "8"] ; TODO figure out of this is helpful? Causes a warning
  :target-path "target/%s/"
  :java-source-paths ["src/main/java"]
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure" "src/test/java"]
  :test-selectors {:default (complement :integration)
                   :integration :integration}
  :plugins [[lein-codox "0.10.7"]]
  :codox {:output-path "codox"}
  :min-lein-version "2.8.1"
  :lein-release {:deploy-via :clojars}

  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]

  :profiles {:dev {:source-paths ["src/dev"]
                   :resource-paths ["src/test/resources"]
                   :dependencies [[org.clojure/clojure "1.10.1"]
                                  [net.mikera/cljunit "0.7.0" :scope "test"]]}

             :test {:dependencies []
                    :java-source-paths ["src/main/java" "src/test/java"]
                    :resource-paths ["src/main/resources" "src/test/resources"]}})
