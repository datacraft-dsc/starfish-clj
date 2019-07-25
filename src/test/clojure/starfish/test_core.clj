(ns starfish.test-core
  (:require [clojure.test :refer [is are testing deftest run-all-tests]])
  (:require [starfish.core :refer :all]))

;;===================================
;; Utility functions, coercion etc.

(deftest test-utility-functions
  (testing "test byte-strings"
    (is (= "Foo" (to-string (to-bytes "Foo"))))
    (is (= "Foo" (to-string (to-string (to-bytes "Foo")))))
    (is (= "Foo" (to-string (to-bytes (to-bytes "Foo"))))))
  (testing "test JSON conversion"
    (are [json-str] (= json-str (-> json-str read-json-string json-string))
      "1"
      "1.0"
      "null"
      "false"
      "{\"k\":\"foobar\"}"
      "{\"k\":\"foo\\nbaz\\tbar\"}"
      "{\"k\":\"&\\uffff\"}"
      "{\"k\":\"foo\\/bar\\/baz\"}"
      "[1,2,3]"
      "[1,{},true,\"bar\"]"
      "{}"
      "{\"foo\\/bar\":{}}"
      ))
  (testing "test DID"
    (let [full-did "did:ocn:1234/foo/bar#fragment"]
      (is (= "did" (did-scheme full-did)))
      (is (= "ocn" (did-method full-did)))
      (is (= "1234" (did-id full-did)))
      (is (= "foo/bar" (did-path full-did)))
      (is (= "fragment" (did-fragment full-did)))
      (is (valid-did? full-did))
      (is (not (valid-did? "nonsense:ocn:1234")))
      ))
  (testing "test HEX"
    (are [hex] (= hex (-> hex hex->bytes bytes->hex))
      "0123456789"
      "abcdef"
      "7fffffff"
      ;; "7fffffffffffffff" ERROR with Long
      )
    (are [i] (= i (-> i int->hex hex->int))
      0
      1
      1024
      2147483647 ;; Integer/MAX_VALUE
      ;; 9223372036854775807 Long/MAX_VALUE ERROR with Long in int->hex
      )))

(deftest test-json-roundtrip
  (let [rt #(read-json-string (json-string %))]
    (are [x] (= x (rt x))
         "Foo"
         {:A "Foo" :B "Bar"}
         [1 2 3]
         ["A" {} [] 0]
         0.0)
    )) 

;;===================================
;; Hash digest keccak

(deftest test-hash-digest-keccak
  (testing "Hash digest keccak"
    (are [output input] (= output (digest input))
      "c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470"
      ""

      "4e03657aea45a94fc7d47ba826c8d667c0d1e6e33a64a036ec44f58fa12d6c45"
      "abc"

      "953d0c27f84a9649b0e121099ffa9aeb7ed83e65eaed41d3627f895790c72d41"
      "EVWithdraw(address,uint256,bytes32)"

      "a08302ed7c06ecccbbc8eb73b91f9a57e097e9c79cff0bfbb2597a9c25a1c439"
      "Niki")))

(deftest asset-creation
  (testing "memory asset without metadata"
    (let [ast (memory-asset "abc")]
      (is (= "abc" (to-string (content ast))))))
  
  (testing "memory asset with metadata"
    (let [tagdata ["test" "data"]
          mdata {:tags tagdata}
          ast (memory-asset mdata "abc")]
      (is (= tagdata (:tags (metadata ast))))
      (is (= "abc" (to-string (content ast)))))))

(comment
  (run-all-tests)
  )
