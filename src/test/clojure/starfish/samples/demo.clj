(ns starfish.samples.demo
  (:use [starfish.core :refer :all]
        [clojure.data.json :as json :exclude [pprint]])
  (:require [clojure.pprint :refer [pprint]])
  (:import [sg.dex.starfish.util DDOUtil JSON]))

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
  (def my-agent (let [did (random-did)
                      ddostring (create-ddo "http://localhost:8080/")]
                  (remote-agent did ddostring "Aladdin" "OpenSesame")))
  
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

  ;; ======================================================================================
  ;;invoke a remote operation
  (def inv-ddo
    (let [k (json/read-str (DDOUtil/getDDO "http://localhost:8080"))]
      (update-in k ["service" 2]
                 (fn[i] (update-in i ["serviceEndpoint"] (fn[_] "http://localhost:3000/api/v1"))) )
      ))

  ;;ddo points to koi-clj for invoke, and Surfer for the rest
  (-> inv-ddo)

  (def invkres 
    (let [did (random-did)
          rema (remote-agent did (json-string inv-ddo) "Aladdin" "OpenSesame")
          oper (get-asset rema "0e48ad0c07f6fe87762e24cba3e013a029b7cd734310bface8b3218280366791")
          res (invoke-sync oper {"first-n" "20"})]
      res))

  ;;response is a map
  (-> invkres)
  ;;view the content
  (to-string (content (invkres "primes")))
  ;;view the metadata with added provenance
  (metadata (invkres "primes"))
)
 
