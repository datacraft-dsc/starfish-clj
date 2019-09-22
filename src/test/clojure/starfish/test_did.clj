(ns starfish.test-did
  (:require [clojure.test :refer [is are testing deftest run-all-tests]])
  (:require [starfish.core :refer :all]))

;;===================================
;; DID tests

(deftest test-did
  (testing "did?"
    (is (did? (did "did:op:123/456")))
    (is (not (did? "did:op:123/456"))))
  
  )
  
(comment
  (run-all-tests)
  )
