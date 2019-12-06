(ns integration.test-surfer
  (:require [starfish.core :as s]
            [clojurewerkz.propertied.properties :as p]
            [clojure.java.io :as io]
            [clojure.test :refer [is are testing deftest run-all-tests]]))

(defn get-remote-agent
  []
  (let [props (p/load-from (io/resource "squid_test.properties"))
        surfer-host (str (get props "surfer.host") ":" (get props "surfer.port"))
        did (s/random-did)
        ddostring (s/create-ddo surfer-host)
        sf (->> (s/remote-account "Aladdin" "OpenSesame")
                (s/remote-agent did))]
    sf))

(deftest ^:integration register-with-surfer
  (testing "registration "
    (let [sf (get-remote-agent)
          a1 (s/get-asset sf "test asset")
          remote-asset (s/register sf a1)]
      (is (s/asset? remote-asset))
      (is (s/did? (s/did remote-asset)))))
  (testing "registration with keyword metadata "
    (let [sf (get-remote-agent)
          a1 (s/get-asset sf (s/memory-asset {:meta :data} "test asset"))
          remote-asset (s/register sf a1)]
      (is (s/asset? remote-asset))))
  (testing "registration with string metadata "
    (let [sf (get-remote-agent)
          a1 (s/get-asset sf (s/memory-asset {"meta" "data"} "test asset"))
          remote-asset (s/register sf a1)]
      (is (s/asset? remote-asset))))
  (testing "upload "
    (let [con-str "test asset2"
          sf (get-remote-agent)
          a1 (s/get-asset sf con-str)
          remote-asset (s/register sf a1)]
      (s/upload sf a1)
      (is (s/asset? remote-asset))
      (is (s/did? (s/did remote-asset)))
      (is (= con-str (s/to-string (s/asset-content remote-asset)))))))

(deftest ^:integration prov-metadata
  (testing "publish case  "
    (let [mdata (s/publish-prov-metadata {"hello" "world"} "abc" "def")
          sf (get-remote-agent)
          a1 (s/get-asset sf (s/memory-asset mdata "content"))
          remote-asset (s/register sf a1)]
      (is (every? #{"entity" "agent" "wasGeneratedBy" "activity" "prefix" "wasAssociatedWith"}
                  (-> remote-asset s/asset-metadata :provenance keys)))))
  (testing "invoke case(without actually invoking operation)  "
    (let [sf (get-remote-agent)
          a1 (s/get-asset sf (s/memory-asset "content"))
          remote-asset (s/register sf a1)
          mdata (s/invoke-prov-metadata {"hello" "world"} "abc" "def"
                                        [remote-asset]
                                        "input params encoded"
                                        "output-param-name")
          a1 (s/get-asset sf (s/memory-asset mdata "content2"))
          remote-asset (s/register sf a1)]
      (is (every? #{"entity" "agent" "wasGeneratedBy" "activity" "prefix"
                      "wasAssociatedWith" "wasDerivedFrom"}
                  (-> remote-asset s/asset-metadata :provenance keys)))))
  )
