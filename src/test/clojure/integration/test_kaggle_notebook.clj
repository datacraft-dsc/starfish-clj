(ns integration.test-kaggle-notebook
  (:require [starfish.core :as s]
            [clojure.walk :refer [stringify-keys]])
  (:import [sg.dex.starfish.impl.remote RemoteAgent RemoteAccount]
           [sg.dex.starfish.impl.memory LocalResolverImpl]
           [sg.dex.starfish Resolver]
           [sg.dex.starfish.util Utils ]))
;;resolver, did, remoteaccount


(def remote-account
  (RemoteAccount/create 
   (Utils/createRandomHexString 32)
   {"username" "Aladdin" "password" "OpenSesame"}))

(def resolver (new LocalResolverImpl))
(def koi-did  (s/random-did))
(def surfer-did  (s/random-did))
(def invoke-url "http://localhost:8191")
(def surfer-url "http://localhost:8080")
(defn get-ddo
  [url]
  {"service" [
                     {:type "Ocean.Invoke.v1"
                      :serviceEndpoint (str url "/api/v1/invoke")}
                     {:type "Ocean.Meta.v1"
                      :serviceEndpoint (str url "/api/v1/meta")}
                     {:type "Ocean.Auth.v1"
                      :serviceEndpoint (str url "/api/v1/auth")}
              {:type "Ocean.Storage.v1"
               :serviceEndpoint (str url "/api/v1/assets")}
              ]})

(def invoke-ddo (get-ddo invoke-url))
(def surfer-ddo (get-ddo surfer-url))


(.installLocalDDO resolver koi-did (stringify-keys invoke-ddo))
(.installLocalDDO resolver surfer-did (stringify-keys surfer-ddo))
(def koi-agent (RemoteAgent/create resolver koi-did remote-account))
(def surfer-agent (RemoteAgent/create resolver surfer-did remote-account))
(def nb1-oper (s/get-asset koi-agent "14c6a6f272c5dac84db8fd5765c53823fcdc963159e9374a4e46b7716de562aa"))
(def nb-oper (s/get-asset surfer-agent "14c6a6f272c5dac84db8fd5765c53823fcdc963159e9374a4e46b7716de562aa"))
(def t1 (s/register surfer-agent (s/file-asset "/home/kiran/src/ocn/koi-clj/resources/test.ipynb")))
(def t2 (s/upload surfer-agent (s/file-asset "/home/kiran/src/ocn/koi-clj/resources/test.ipynb")))

(def t3 (let [train-asset (s/file-asset "/home/kiran/src/ocn/koi-clj/resources/train.csv")]
          (s/register surfer-agent train-asset)
          (s/upload surfer-agent train-asset)
          ))
(-> t2)
(-> nb-oper s/asset-id)
(s/invoke nb-oper {:notebook t2 :input-asset t3
                   :notebook-name "test.ipynb"
                   :input-asset-name "train.csv"
                   :result-asset-name "train2.csv"})
()
(s/invoke-result )
