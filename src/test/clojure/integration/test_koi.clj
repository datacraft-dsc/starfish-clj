(ns integration.test-koi
  (:require [starfish.core :as s]
            [clojure.java.io :as io]
            [clojure.test :as t :refer [deftest testing is use-fixtures]]
            [com.stuartsierra.component :as component]
            [koi.api :as koi-api :refer [default-system]]           
            )
  (:import ;[sg.dex.starfish.impl.squid SquidResolverImpl SquidAgent]
           [sg.dex.starfish.util DID]
           ))

(defn koi-fixture [f]
  (component/start (default-system (aero.core/read-config (io/resource "config.edn")))) 
        (f))
(use-fixtures :once koi-fixture)

(defn get-koi-agent
  ([] (get-koi-agent "http://localhost:8191"))
  ([host]
   (let [;surfer-host "http://localhost:8191" 
         did (s/random-did)
         ddostring (s/create-ddo host)
         remote-account (s/remote-account "sometoken")
         sf (s/remote-agent did ddostring remote-account)
         ;sf (s/remote-agent did ddostring "Aladdin" "OpenSesame")
         ]
     sf)))

(defn get-surfer-agent
  ([] (get-surfer-agent "http://localhost:8080"))
  ([host]
   (let [;surfer-host "http://localhost:8080" 
         did (s/random-did)
         ddostring (s/create-ddo host)
                                        ;remote-account (s/remote-account )
         sf (s/remote-agent did ddostring "Aladdin" "OpenSesame")]
     sf)))


(deftest sha-raw-hash
  (testing "default "
    (-> (get-koi-agent)
        (s/get-asset "868f40dce1242d6351c3cc7ded486c854df48c4d0fa7fc14c7158d9573a14cb8" )
        (s/invoke-result {:to-hash "longstring"})
        (get "hash-val") string? is
        )))

(comment 


(deftest sha-asset-hash
  (testing "default case"
    (let [koi-agent (get-koi-agent)
          surfer-agent (get-surfer-agent)
          ;;register the asset with surfer 
          ast (s/asset (s/memory-asset {"metadata" "data"} "content"))
          r1 (s/register surfer-agent ast)
          ;;upload the asset to surfer
          rda (s/upload surfer-agent ast)

          ;;get the operation object
          sha-oper (s/get-asset koi-agent "e5574e68df2dd9b1ebe277687c7e5cdd0051f4a70d0069647da948e60da47b59" )

          ;;operation result

          result (s/invoke-result sha-oper {:to-hash rda})
          ]
      (is (map? result))
      (is (string? (get result "hash-val")))
      (is (string? (get (get result "hash-val") "did")))))))

(def inp-datasets
  (mapv #(str "/home/kiran/src/ocn/koi-clj/resources/" %)
        ["input_service_data.json"
         "input_engine_data.json"
         "input_iot_sensor_data.json"]))

(comment 
(deftest concatenate-dataset
  (testing "default case"
    (let [
          koi-agent (get-koi-agent)
          surfer-agent (get-surfer-agent)
          ;;register the asset with surfer
          datasets (mapv #(s/asset (s/memory-asset {}
                                                   (slurp %)))
                         inp-datasets)
          ;;register and upload
          reg-res (mapv #(do (s/register surfer-agent %)
                             (s/upload surfer-agent %))
                        datasets)

          ;;get the operation object
          concat-oper (s/get-asset
                       koi-agent
                       "c172b5482c943b9eb799ee52170da5fce268765c1a30c275708738d65e83e061"
                       )

          ;;operation result

          result (s/invoke-result concat-oper
                                  (zipmap [:dataset1 :dataset2 :dataset3]
                                          reg-res))
          ]
      (def concat-res result)
      (is (map? result))
                                        ;result
      (is (string? (get result "concatenated-dataset")))
      (is (string? (get result "onchain-did")))
      (is (string? (get (get result "concatenated-dataset") "did"))))))
)

(comment 
  (let [
        koi-agent (get-koi-agent "http://13.70.20.203:8191/")
        surfer-agent (get-surfer-agent "http://13.70.20.203:8092/")
        ;koi-agent (get-koi-agent)
        ;surfer-agent (get-surfer-agent)
        ;;register the asset with surfer
        datasets (mapv #(s/asset (s/memory-asset {"name" %1
                                                  "author" %3}
                                                 (slurp %2)))
                       ["Engine Logbook"
                        "Engine Removal Notification"
                        "Engine Shop Visit Report"]
                       inp-datasets
                       ["Rolls Royce"
                        "SIAEC LAE"
                        "SIAEC LAE"]
                       )
        ;;register and upload
        reg-res (mapv #(do (s/register surfer-agent %)
                           (s/upload surfer-agent %))
                      datasets)

        ;;get the operation object
        concat-oper (s/get-asset
                     koi-agent
                     "4d10307f393e8939f29cd843e84fdcf8a617409310d5d3ef74b386af6c15f8ce")

        ;;operation result

        result (s/invoke-result concat-oper
                                (zipmap
                                 [:engine-logbook :engine-removal-notification :engine-shop-visit-report]
                                        reg-res))
        ]
    (def concat-res result)
    (is (map? result))
                                        result
    ;(is (string? (get result "concatenated-engine-dataset")))
    #_(is (string? (get (get result "concatenated-engine-dataset") "did")))))
(comment
  (def koi-agent (get-koi-agent))
  (def surfer-agent (get-surfer-agent))
  (def ast (s/asset (s/memory-asset {"metadata" "data"} "content")))

  (def squid-agent (SquidAgent/create (SquidResolverImpl.) (DID/createRandom)))

  (def r2 (s/register squid-agent ast))
  (spit "/tmp/link.csv" (str "https://commons.nile.dev-ocean.com/" (.getDID r2)))
  (s/metadata r2)
  (def r1 (s/register surfer-agent ast))
  (def rda (s/upload surfer-agent ast))

  (def sha-oper (s/get-asset koi-agent e5574e68df2dd9b1ebe277687c7e5cdd0051f4a70d0069647da948e60da47b59"" ))

  (s/invoke-result sha-oper {:to-hash rda})
  (def j1 (s/invoke sha-oper {:to-hash rda}))
  (-> j1)
  (-> (s/get-asset surfer-agent (get (get concat-res "concatenated-dataset") "did")) s/metadata)

  (def koi-agent-2 (let [did (s/random-did)
                         ddostring (s/create-ddo "http://localhost:8191" )
                         sf (s/remote-agent did ddostring "Aladdin" "OpenSesame")]
                     sf))

  (s/get-asset koi-agent-2 "e5574e68df2dd9b1ebe277687c7e5cdd0051f4a70d0069647da948e60da47b59" )
  )
