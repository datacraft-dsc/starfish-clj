(ns dev
  (:require [starfish.core :as sf]
            [clojure.tools.namespace.repl :refer [refresh] :rename {refresh reset}]))

(comment
  (defn demo-operation1
    "Demo Operation 1"
    [x]
    nil)

  (defn demo-operation2
    [asset-x]
    nil)

  (sf/invokable-metadata #'demo-operation1)
  (sf/invokable-metadata #'demo-operation2)

  (sf/metadata (sf/in-memory-operation (sf/invokable-metadata #'demo-operation1)))

  )
