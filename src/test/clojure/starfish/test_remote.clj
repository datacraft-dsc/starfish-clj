(ns starfish.test-remote
  (:require [clojure.test :refer :all]
            [starfish.core :as sf])
  (:import (sg.dex.starfish.impl.remote RemoteAgent)
           (sg.dex.starfish.impl.memory LocalResolverImpl)))

(deftest remote-agent-test
  (testing "New Remote Agent"
    (binding [sf/*resolver* (LocalResolverImpl.)]
      (let [random-did (sf/random-did)
            empty-ddo-string "{}"
            remote-agent (sf/remote-agent random-did empty-ddo-string nil)]
        (is (instance? RemoteAgent remote-agent))

        (testing "Agent DID"
          (is (= random-did (sf/did remote-agent))))

        (testing "Agent DDO"
          (is (= {} (sf/ddo random-did)))
          (is (= empty-ddo-string (sf/ddo-string random-did))))))))
