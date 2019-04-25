(ns starfish.samples.demo;
  (:use [starfish.core :refer :all])
  (:require [clojure.pprint :refer [pprint]]))

(fn [] ;; Quick hack to compile this file without executing on load
  
  ;; ======================================================================================
  ;; BASIC ASSETS
  ;; Let's talk about assets
 
  ;; create a new asset
  (def as1 (memory-asset {:name "My Asset"} "This is a test"))
  
  ;; display the metadata
  (pprint (metadata as1))
  
  ;; validate the content hash
  (digest "This is a test")
  
  ;; Print the content
  (println (to-string (content as1)))
  
  
  ;; ======================================================================================
  ;; USING REMOTE AGENTS
  ;; Agents are remote services providing asset and capabilities to the Ocean ecosystem
  (def my-agent (surfer "http://13.67.33.157:8080/"))
  
  ;; agents have a DID
  (str (did my-agent))
  
  ;; Get an asset
  (def as2 (get-asset my-agent "68895130aa3105381c67f7e71107f81d46c849f1968db8665e80bc06dd790fd2"))
  
  ;; assets also have a DID, starting with the DID of the agent
  (str (did as2))
  
  ;; print the content of asset data
  (println (to-string (content as2)))
  
  ;; ======================================================================================
  ;; Operations
  
  ;; define a new operation
  (def op (create-operation [:input] 
                            (fn [{input :input}]
                              (asset (.toUpperCase (to-string input))))))
  
  
  (pprint (metadata op))
  
  ;; compute the result
  (def result (invoke-result op {:input as2}))
  
  ;; see the reuslt
  (println (to-string (content result)))
  
  ;; ======================================================================================
  ;; Register new asset on our agent
  
  ;; upload the result of our invoke
  (def as3 (upload my-agent result)) 
  
  ;; asset now has a full remote DID
  (str (did as3)) 
  
  ;; double check remote content
  (println (to-string (content as3)))
 
)
 
