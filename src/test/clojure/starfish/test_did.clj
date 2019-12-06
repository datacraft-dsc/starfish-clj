(ns starfish.test-did
  (:require [clojure.test :refer [is are testing deftest run-all-tests]])
  (:require [starfish.core :refer :all]))

;;===================================
;; DID tests

(deftest test-did
  (testing "did?"
    (is (did? (did "did:op:123/456")))
    (is (not (did? "did:op:123/456"))))
  
  (testing "did id"
    (is (= "123" (did-id "did:op:123/456")))
    (is (= "123" (did-id (did "did:op:123/456")))))
  
  (testing "did path"
    (is (= "456" (did-path "did:op:123/456")))
    (is (= "456" (did-path (did "did:op:123/456"))))
    (is (nil? (did-path (did "did:op:123")))))
  )

(deftest test-did-ids
  (testing "did ids"
    (is (= "456" (asset-id "did:op:123/456")))))
  
(comment
  (run-all-tests)
  )
