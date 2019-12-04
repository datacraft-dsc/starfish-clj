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
  (pprint (asset-metadata as1))
  
  ;; validate the content hash
  (digest "This is a test")
  
  ;; Print the content
  (println (to-string (asset-content as1)))
  
  
  ;; ======================================================================================
  ;; USING REMOTE AGENTS
  ;; Agents are remote services providing asset and capabilities to the Ocean ecosystem
  (def my-agent (let [did (random-did)
                      ddostring (create-ddo "http://localhost:8080")]
                  (remote-agent did ddostring "Aladdin" "OpenSesame")))
  
  ;; agents have a DID
  (str (did' my-agent))
  
  ;; Get an asset
  (def as2 (get-asset my-agent "4b95d8956ab9a503540d62ac7db2fbcaa99f7f78b2d4f4d8edd6d9d19d750403"))
 
  ;; assets also have a DID, starting with the DID of the agent
  (str (did' as2))
  
  ;; Upload an asset
  (def as3 (upload my-agent as1))
 
  (get-asset my-agent (asset-id as3))
  
  ;; ======================================================================================
 ;; Operations
 
  ;; define a new operation
 (def op (create-operation [:input] 
                            (fn [{input :input}]
                              {:output (memory-asset (str (count (to-string input))))})))
   
  (pprint (asset-metadata op))
  
  ;; compute the result, getting the output asset from the result map
 (def as4 (:output (invoke-result op {:input as1})))
  
  ;; see the reuslt
 (println (to-string (asset-content as4)))
  
  ;; ======================================================================================
 ;; Register new asset on our agent
 
  ;; upload the result of our invoke
 (def as5 (upload my-agent (memory-asset "Remote test asset data"))) 
  
  ;; asset now has a full remote DID
 (str (did' as5))
  
  ;; double check remote content
 (println (to-string (asset-content as5)))

  ;; ======================================================================================
  ;;invoke a remote operation

  (def invkres 
    (let [oper (get-asset my-agent "f994e155382044caedd76bd2af2f8a1244aa31ad9818b955848032c8ecb9dabb")
          res (get-result (invoke oper {"input" "Supercalifragilisticexpialidocious"}))]
      res))

  ;;response is a map
 (-> invkres)

 
 
)
 
