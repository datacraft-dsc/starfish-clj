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
  (are [json-val] (= json-val (-> json-val json-string read-json-string))
      1
      1.0 
      nil
      false
      "just a string" 
      ["foo" "bar"]
      [1 2 3]
      {:a 1 :b 2}
      {}
      {:a {:b 1} :c []} 
      ) 
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
         true
         false
         nil
         ["A" {} [] 0 true false [1] {:a "Baz"} nil]
         0.0)
    )) 

;;===================================
;; Hash digest keccak

(deftest test-hash-digest-keccak
  (testing "Hash digest keccak"
    (are [output input] (= output (digest input))
      "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a"
      ""

      "3a985da74fe225b2045c172d6bd390bd855f086e3e9d525b46bfe24511431532"
      "abc"

      "6b6ec8a93f763079ff903b707c7b28ca46da38c4d5b18b6fc11e9c8d8a97ca83"
      "EVWithdraw(address,uint256,bytes32)"

      "97121be303a9ad92d0927e4a2effa527cd49fe45de1a4c9e967ef22b223f50af"
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


(defn demo-operation1
  "Demo Operation 1"
  [x]
  nil)

(defn demo-operation2
  [asset-x]
  nil)

(deftest default-operation-metadata-test
  (let [{:keys [name type operation] :as default-medatadata} (operation-var-metadata #'demo-operation1)]
    ;; =>
    {:name "Demo Operation 1",
     :type "operation",
     :dateCreated "2019-11-05T05:05:45.814411Z",
     :operation {"modes" ["sync" "async"], "params" {"x" {"type" "json"}}}}

    (is (= "Demo Operation 1" name))
    (is (= "operation" type))
    (is (= {"modes" ["sync" "async"], "params" {"x" {"type" "json"}}} operation))

    ;; Generated metadata - `default-medatadata` - must be
    ;; equivalent to the one returned by the asset `metadata` function.
    (is (= (select-keys default-medatadata [:name :type :operation])
           (select-keys (metadata (in-memory-operation default-medatadata)) [:name :type :operation]))))

  (let [{:keys [name type operation] :as default-medatadata} (operation-var-metadata #'demo-operation2)]
    ;; =>
    {:name "Unnamed Operation",
     :type "operation",
     :dateCreated "2019-11-05T05:05:59.877363Z",
     :operation {"modes" ["sync" "async"], "params" {"asset-x" {"type" "asset"}}}}

    (is (= "Unnamed Operation" name))
    (is (= "operation" type))
    (is (= {"modes" ["sync" "async"], "params" {"asset-x" {"type" "asset"}}} operation))

    ;; Generated metadata - `default-medatadata` - must be
    ;; equivalent to the one returned by the asset `metadata` function.
    (is (= (select-keys default-medatadata [:name :type :operation] )
           (select-keys (metadata (in-memory-operation default-medatadata)) [:name :type :operation])))))

(comment
  (run-all-tests)
  )
