(defproject sg.dex/starfish-clj "0.0.1-SNAPSHOT"
  :url "https://github.com/DEX-Company/starfish-clj"
  :dependencies [[sg.dex/starfish-java "0.0.1-SNAPSHOT"]
                 [org.slf4j/jcl-over-slf4j "1.7.26"]
                 [org.clojure/data.json "0.2.6"]
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
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]]
                   ;; :resource-paths ["src/main/resources"]
                   }
             :test {:dependencies [[net.mikera/cljunit "0.6.0" :scope "test"]]
                    :java-source-paths ["src/main/java" "src/test/java"]
                    ;; :source-paths ["src/main/clojure" "src/test/clojure"]
                    ;; :resource-paths ["src/main/resources" "src/test/resources"]
                    }}
  )
