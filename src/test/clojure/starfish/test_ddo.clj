(ns starfish.test-ddo
  (:require [clojure.test :refer [is are testing deftest run-all-tests]])
  (:require [starfish.core :as sf])
  (:import (sg.dex.starfish.dexchain DexResolver)))

(deftest test-install-ddo
  #_(testing "DEX Resolver"
      (sf/ddo-string (DexResolver/create) (sf/random-did)))

  (testing "Unregistered DDO"
    (let [random-did (sf/random-did)]
      (is (nil? (sf/ddo-string random-did)))
      (is (nil? (sf/ddo random-did)))))

  (testing "Install local DDO"
    (let [random-did (sf/random-did)
          ddo-string "{}"]
      (sf/install-ddo random-did ddo-string)

      (is (= ddo-string (sf/ddo-string random-did)))
      (is (= {} (sf/ddo random-did))))))

