(ns test.clojure.integration.test-koi
  (:require [starfish.core :as s]
            [clojurewerkz.propertied.properties :as p]
            [clojure.java.io :as io]
            [clojure.test :refer [is are testing deftest run-all-tests]])
  (:import [sg.dex.starfish.util
            JSON DID Hex Utils RemoteAgentConfig])
  )

(defn get-remote-agent
  []
  (let [props (p/load-from (io/resource "squid_test.properties"))
        surfer-host (str (get props "surfer.host") ":" (get props "surfer.port"))
        did (s/random-did)
        ddostring (s/create-ddo surfer-host)
        sf (s/remote-agent did ddostring "Aladdin" "OpenSesame")]
    sf))

(deftest ^:integration register-with-surfer
  (testing "registration "
    (let [a1 (s/asset "test asset")
          sf (get-remote-agent)
          remote-asset (s/register sf a1)]
      (is (s/asset? remote-asset))
      (is (s/did? (s/did remote-asset)))))
  (testing "registration with keyword metadata "
    (let [a1 (s/asset (s/memory-asset {:meta :data} "test asset"))
          sf (get-remote-agent)
          remote-asset (s/register sf a1)]
      (is (s/asset? remote-asset))))
  (testing "registration with string metadata "
    (let [a1 (s/asset (s/memory-asset {"meta" "data"} "test asset"))
          remote-asset (s/register (get-remote-agent)a1)]
      (is (s/asset? remote-asset))))
  (testing "upload "
    (let [con-str "test asset2"
          a1 (s/asset con-str)
          sf (get-remote-agent)
          remote-asset (s/register sf a1)]
      (s/upload sf a1)
      (is (s/asset? remote-asset))
      (is (s/did? (s/did remote-asset)))
      (is (= con-str (s/to-string (s/content remote-asset)))))))
