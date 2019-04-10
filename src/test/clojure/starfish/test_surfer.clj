(ns starfish.test-surfer
  (:require [starfish.core :as s]
            [clojure.test :refer [is are testing deftest run-all-tests]]))

(deftest register-with-surfer
  (testing "registration "
    (let [a1 (s/asset "test asset")
          sf (s/surfer "http://localhost:8080/")
          remote-asset (s/register sf a1)]
      (is (s/asset? remote-asset))
      (is (s/did? (s/did remote-asset))))))

