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

  (sf/default-operation-metadata #'demo-operation1)
  (sf/default-operation-metadata #'demo-operation2)

  (sf/metadata (sf/in-memory-operation #'demo-operation1))

  )
