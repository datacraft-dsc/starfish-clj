(ns starfish.test-squid
  (:require [starfish.core :as s]
            [clojurewerkz.propertied.properties :as p]
            [clojure.java.io :as io]
            [clojure.test :refer [is are testing deftest run-all-tests]])
  (:import
   [sg.dex.starfish.util JSON DID Hex Utils RemoteAgentConfig]
   [sg.dex.starfish Ocean]
           [com.oceanprotocol.squid.api OceanAPI]
           [com.oceanprotocol.squid.api.config OceanConfig]
           [sg.dex.starfish.impl.squid SquidAgent SquidAsset]))

(defn get-squid-agent
  []
  (let [props (p/load-from (io/resource "squid_test.properties"))
        ocean (Ocean/connect (OceanAPI/getInstance props))
        did1 (s/random-did)
        squid-agent (SquidAgent/create props ocean did1)]
    squid-agent))

(defn get-surfer-agent
  []
  (let [did (s/random-did)
        ddostring (s/create-ddo "http://52.187.164.74:8080/")
        sf (s/remote-agent did ddostring "Aladdin" "OpenSesame")]
    sf))

(deftest ^:integration register-with-squid
  (testing "registration "
    (let [a1 (s/memory-asset {"random" "metadata"}
                             "test asset")
          squid-agent (get-squid-agent)
          remote-asset (s/register squid-agent a1)
          did1 (s/did remote-asset)
          rasset (s/get-asset squid-agent did1)
          rmetadata (s/metadata rasset)]
      (is (map? rmetadata )))))
