(ns integration.test-squid
  (:require [starfish.core :as s]
            [clojurewerkz.propertied.properties :as p]
            [clojure.java.io :as io]
            [clojure.test :refer [is are testing deftest run-all-tests]])
  (:import
           [sg.dex.starfish.impl.squid SquidAgent]))

(defn get-properties
  []
  (p/load-from (io/resource "squid_test.properties")))

(defn get-squid-agent
  []
  (let [props (get-properties)
        did1 (s/random-did)
        squid-agent (SquidAgent/create props did1)]
    squid-agent))

(defn get-surfer-agent
  []
  (let [did (s/random-did)
        props (get-properties)
        surfer-host (str (get props "surfer.host") ":" (get props "surfer.port"))
        ddostring (s/create-ddo surfer-host)
        sf (s/remote-agent did ddostring "Aladdin" "OpenSesame")]
    sf))

(deftest ^:integration register-with-squid
  (testing "registration "
    (let [con-str "testdata"
          a1 (s/memory-asset {"random" "metadata"}
                             con-str)
          squid-agent (get-squid-agent)
          remote-asset (s/register squid-agent a1)
          did1 (s/dido' remote-asset)
          rasset (s/get-asset squid-agent did1)
          rmetadata (s/asset-metadata rasset)]
      (is (map? rmetadata ))))
  (testing "registration and surfer upload "
    (let [con-str "testdata"
          a1 (s/memory-asset {"random" "metadata2"} con-str)
          squid-agent (get-squid-agent)
          remote-asset (s/register squid-agent a1)
          surfer (get-surfer-agent)
          remote-surfer-asset (s/register surfer a1)]
      (s/upload surfer a1)
      (is (s/asset? remote-surfer-asset))
      (is (s/did? (s/dido' remote-surfer-asset)))
      (is (= con-str (s/to-string (s/asset-content remote-surfer-asset))))
      ))
  )
