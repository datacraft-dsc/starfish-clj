(ns starfish.samples.demo
  (:use [starfish.core :refer :all])
  (:require 
    [clojure.repl :refer :all]
    [clojure.pprint :refer [pprint]]
    [clojure.data.json :as json ])
  (:import [sg.dex.starfish.util DDOUtil JSON]))

(fn [] ;; Quick hack to compile this file without executing on load
 
  ;; ======================================================================================
  ;; BASIC ASSETS
  ;; Let's talk about assets

  ;; create a new asset
  (def as1 (memory-asset             ;; type of asset to construct
             "This is a test")       ;; content (as a String))
    )
  
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
                      ddostring (create-ddo "http://13.70.20.203:8090/")]
                  (remote-agent did ddostring "Aladdin" "OpenSesame")))
  
  ;; agents have a DID
  (str (did my-agent))
  
  ;; Get an asset
  (def as2 (get-asset my-agent "17f14c2ac039225a627365d83069dad300ea38c5de5e114eb93cbcc7fcf4cbe9"))
 
  ;; assets also have a DID, starting with the DID of the agent
  (str (did as2))
  
  ;; Upload an asset
  (def as3 (upload my-agent as1))
 
  (get-asset my-agent (asset-id as3))
  
  ;; ======================================================================================
 ;; Operations
 
  ;; define a new operation
 (def op (create-operation [:input] 
                            (fn [{input :input}]
                              {:output (asset (.toUpperCase (to-string input)))})))
  
  
  (pprint (metadata op))
  
  ;; compute the result, getting the output asset from the result map
 (def as4 (:output (invoke-result op {:input as1})))
  
  ;; see the reuslt
 (println (to-string (content as4)))
  
  ;; ======================================================================================
 ;; Register new asset on our agent
 
  ;; upload the result of our invoke
 (def as3 (upload my-agent as4)) 
  
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
 
