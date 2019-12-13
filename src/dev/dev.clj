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

  (sf/invokable-metadata #'demo-operation1)
  (sf/invokable-metadata #'demo-operation2)

  (sf/asset-metadata (sf/memory-operation (sf/invokable-metadata #'demo-operation1)))

  )
