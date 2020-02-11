(ns starfish.test-remote
  (:require [clojure.test :refer :all]
            [starfish.core :as sf :refer :all])
  (:import (sg.dex.starfish.impl.remote RemoteAgent)))

(deftest remote-agent-test
  (testing "Remote Instantiation with DID"
    (let [d (random-did)
          ra (remote-agent d "{}" nil)]
      (is (instance? RemoteAgent ra))
      (is (= d (did ra)))
      )))
