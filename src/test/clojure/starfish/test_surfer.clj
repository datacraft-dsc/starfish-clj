(ns starfish.test-surfer
  (:require [starfish.core :as s]
            [clojure.test :refer [is are testing deftest run-all-tests]])
  (:import [sg.dex.starfish.util
            JSON DID Hex Utils RemoteAgentConfig]))

(deftest ^:integration register-with-surfer
  (testing "registration "
    (let [a1 (s/asset "test asset")
          did (s/random-did)
          ddostring (s/default-ddo "http://localhost:8080/")
          sf (s/remote-agent did ddostring "Aladdin" "OpenSesame")
          remote-asset (s/register sf a1)]
      (is (s/asset? remote-asset))
      (is (s/did? (s/did remote-asset))))))
