(ns starfish.test-surfer
  (:require [starfish.core :as s]
            [clojure.test :refer [is are testing deftest run-all-tests]]))

(deftest register-with-surfer
  (testing "registration "
    (let [a1 (s/asset "test asset")
          sf (s/surfer "http://localhost:8080/")]
      (is (not (nil? (s/register sf a1)))))))

