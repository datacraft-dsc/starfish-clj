(ns starfish.test-ddo
  (:require [clojure.test :refer [is are testing deftest run-all-tests]])
  (:require [starfish.core :refer :all]))

;;===================================
;; Local DDO tests

(deftest test-install-ddo
  (testing "missing ddo"
    (let [missing-did (random-did)]
      (is (nil? (ddo-string missing-did)))
      (is (nil? (ddo-map missing-did))))))
  
(comment
  (run-all-tests)
  )
