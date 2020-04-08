(ns starfish.test-ddo
  (:require [clojure.test :refer [is are testing deftest run-all-tests]]
            [starfish.core :as sf])
  (:import (sg.dex.starfish.impl.memory LocalResolverImpl)))

(deftest test-install-ddo
  (binding [sf/*resolver* (LocalResolverImpl.)]
    (testing "Local Resolver"
      (is (instance? LocalResolverImpl sf/*resolver*)))

    (testing "Unregistered DDO"
      (let [random-did (sf/random-did)]
        (is (nil? (sf/ddo-string random-did)))
        (is (nil? (sf/ddo random-did)))))

    (testing "Install DDO"
      (let [random-did (sf/random-did)
            ddo-string "{}"]
        (sf/install-ddo random-did ddo-string)

        (is (= ddo-string (sf/ddo-string random-did)))
        (is (= {} (sf/ddo random-did)))))))

