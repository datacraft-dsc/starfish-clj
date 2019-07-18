(defproject sg.dex/starfish-clj "0.5.2-SNAPSHOT"
  :url "https://github.com/DEX-Company/starfish-clj"
  :dependencies [
                 [sg.dex/starfish-java "0.6.0"]
                 [org.slf4j/jcl-over-slf4j "1.8.0-alpha2"]
                 [org.clojure/data.json "0.2.6"]
                 [clojurewerkz/propertied "1.3.0"]
                 [org.clojure/data.csv "0.1.4"]]
  :exclusions [commons-logging/commons-logging]
  :managed-dependencies [[com.fasterxml.jackson.core/jackson-databind "2.9.8"]]

  :javac-options ["-target" "1.8", "-source" "1.8"]
  :target-path "target/%s/"
  :java-source-paths ["src/main/java"]
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"]
  :test-selectors {:default (complement :integration)
                   :integration :integration}
  :plugins [[lein-codox "0.10.7"]]
  :lein-release {:deploy-via :clojars}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]]
                   :resource-paths ["src/test/resources"]
                   }
             :test {:dependencies [[net.mikera/cljunit "0.6.0" :scope "test"]
                                   ]
                    :java-source-paths ["src/main/java" "src/test/java"]
                    ;; :source-paths ["src/main/clojure" "src/test/clojure"]
                    :resource-paths ["src/main/resources" "src/test/resources"]
                    }}
  )
