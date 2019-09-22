(ns starfish.test-ddo
  (:require [clojure.test :refer [is are testing deftest run-all-tests]])
  (:require [starfish.core :refer :all]))

;;===================================
;; Local DDO tests

(deftest test-install-ddo
  (testing "missing ddo"
    (is (nil? (ddo (random-did)))))
  
  (testing "install local ddo"
    (let [did (random-did)
          ddostring "{}"]
      (install-ddo did ddostring)
      
      (is (= ddostring (ddo did))))))
  
(comment
  (run-all-tests)
  )
