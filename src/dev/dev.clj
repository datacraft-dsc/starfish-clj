(ns dev
  (:require [starfish.core :as sf]))

(comment
  (defn demo-operation1
    "Demo Operation 1"
    [x]
    nil)

  (defn demo-operation2
    [asset-x]
    nil)

  (sf/operation-var-metadata #'demo-operation1)
  (sf/operation-var-metadata #'demo-operation2)

  (sf/metadata (sf/in-memory-operation (sf/operation-var-metadata #'demo-operation1)))

  )
