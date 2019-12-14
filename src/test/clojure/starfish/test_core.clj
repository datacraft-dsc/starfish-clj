(ns starfish.test-core
  (:require [clojure.test :refer [is are testing deftest run-all-tests]]
            [starfish.core :as sf :refer :all])
  (:import (sg.dex.starfish.impl.remote RemoteAccount)
           (sg.dex.starfish.impl.memory MemoryAgent LocalResolverImpl)
           (sg.dex.starfish.util DID)))

(deftest did-test
  (testing "DID"
    (testing "from Asset"
      (is (sf/ident? (sf/memory-asset "abc")))
      (is (sf/did? (sf/did (sf/memory-asset "abc")))))

    (testing "from Agent"
      (is (sf/ident? (MemoryAgent/create)))
      (is (sf/did? (sf/did (MemoryAgent/create)))))

    (testing "from string"
      (is (= true (sf/ident? "")))
      (is (thrown? IllegalArgumentException (sf/did "")))
      (is (sf/did? (sf/did (sf/random-did-string)))))

    (testing "from nil"
      (is (= false (sf/ident? nil))))

    (testing "from DID"
      (is (sf/ident? (sf/random-did)))
      (is (sf/did? (sf/did (sf/random-did))))
      (let [d (sf/random-did)]
        (is (= d (sf/did d)))))))

(deftest did-scheme-test
  (testing "DID Scheme"
    (is (= "did" (sf/did-scheme "did:op:123")))
    (is (= "did" (sf/did-scheme (sf/random-did-string))))
    (is (= "did" (sf/did-scheme (sf/random-did))))))

(deftest did-method-test
  (testing "DID Method"
    (is (= "op" (sf/did-method "did:op:123")))
    (is (= "op" (sf/did-method (sf/random-did-string))))
    (is (= "op" (sf/did-method (sf/random-did))))))

(deftest did-id-test
  (testing "DID ID"
    (is (= "123" (sf/did-id "did:op:123")))))

(deftest did-path-test
  (testing "DID Path"
    (is (= "456" (sf/did-path "did:op:123/456")))
    (is (= nil (sf/did-path "did:op:1234")))))

(deftest did-fragment-test
  (testing "DID Fragment"
    (is (= "abc" (sf/did-fragment "did:op:123#abc")))
    (is (= nil (sf/did-fragment "did:op:123")))
    (is (= nil (sf/did-fragment "did:op:123/456")))))

(deftest asset-id-test
  (testing "Asset ID"
    (is (= "456" (sf/asset-id "did:op:123/456")))
    (is (= nil (sf/asset-id "did:op:123")))))

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

      (is (and (ident? full-did) (did full-did)))
      (is (ident? "nonsense:ocn:1234"))))

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
             2147483647                                     ;; Integer/MAX_VALUE
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
      (is (= "abc" (to-string (asset-content ast))))))

  (testing "memory asset with metadata"
    (let [tagdata ["test" "data"]
          mdata {:tags tagdata}
          ast (memory-asset mdata "abc")]
      (is (= tagdata (:tags (asset-metadata ast))))
      (is (= "abc" (to-string (asset-content ast)))))))


(defn demo-operation1
  "Demo Operation 1"
  [x]
  nil)

(defn demo-operation2
  [asset-x]
  nil)

(deftest invokable-metadata-test
  (let [{:keys [name type operation] :as default-medatadata} (invokable-metadata #'demo-operation1)]
    ;; =>
    {:name "Demo Operation 1",
     :type "operation",
     :dateCreated "2019-11-05T09:06:43.372606Z",
     :operation {"modes" ["sync" "async"], "params" {"x" {"type" "json"}}},
     :additionalInfo {:function "starfish.test-core/demo-operation1"}}

    (is (= "Demo Operation 1" name))
    (is (= "operation" type))
    (is (= {"modes" ["sync" "async"], "params" {"x" {"type" "json"}}} operation))

    ;; Generated metadata - `default-medatadata` - must be
    ;; equivalent to the one returned by the asset `metadata` function.
    (is (= (select-keys default-medatadata [:name :type :operation])
           (select-keys (asset-metadata (memory-operation default-medatadata)) [:name :type :operation]))))

  (let [{:keys [name type operation] :as default-medatadata} (invokable-metadata #'demo-operation2
                                                                                 {:params {"asset-x" {:type "asset"}}})]
    ;; =>
    {:name "Unnamed Operation",
     :type "operation",
     :dateCreated "2019-11-05T09:07:02.878426Z",
     :operation {"modes" ["sync" "async"], "params" {"asset-x" {"type" "asset"}}},
     :additionalInfo {:function "starfish.test-core/demo-operation2"}}

    (is (= "Unnamed Operation" name))
    (is (= "operation" type))
    (is (= {"modes" ["sync" "async"], "params" {"asset-x" {"type" "asset"}}} operation))

    ;; Generated metadata - `default-medatadata` - must be
    ;; equivalent to the one returned by the asset `metadata` function.
    (is (= (select-keys default-medatadata [:name :type :operation])
           (select-keys (asset-metadata (memory-operation default-medatadata)) [:name :type :operation])))))

(deftest remote-account-test
  (testing "Username & Password"
    (let [credentials (.getCredentials ^RemoteAccount (remote-account "foo" "bar"))]
      (is (= #{"username" "password"} (set (keys credentials))))))

  (testing "Token"
    (let [credentials (.getCredentials ^RemoteAccount (remote-account "x"))]
      (is (= #{"token"} (set (keys credentials)))))))

(deftest get-agent-test
  (testing "Memory Agent"
    (binding [sf/*resolver* (LocalResolverImpl.)
              sf/*registry* (atom {})]
      (let [did1 (sf/random-did)
            did2 (sf/random-did)]
        (testing "Random DID"
          (is (= nil (sf/get-agent (sf/random-did)))))

        (testing "Get Agent 1"
          (is (= nil (sf/get-agent did1)))
          (sf/install did1 {} (constantly (MemoryAgent/create)))
          (is (sf/get-agent did1)))

        (testing "Get Agent 2"
          (is (= nil (sf/get-agent did2)))
          (sf/install did2 {} (constantly (MemoryAgent/create)))
          (is (sf/get-agent did2)))))))

(deftest get-asset-test
  (testing "Memory Asset"
    (binding [sf/*resolver* (LocalResolverImpl.)
              sf/*registry* (atom {})]
      (let [agent-did (sf/random-did)
            agent (MemoryAgent/create ^DID agent-did)
            asset1 (sf/memory-asset "ABC")
            asset2 (sf/memory-asset "DEF")]

        ;; Install Memory Agent
        (sf/install agent-did {} (constantly agent))

        (is (= nil (sf/get-asset agent (sf/memory-asset "ABC"))))
        (is (= nil (sf/get-asset agent (sf/random-did))))
        (is (= nil (sf/get-asset agent nil)))

        ;; Upload (& register) asset 1
        (sf/upload agent asset1)

        (is (sf/get-asset agent asset1))
        (is (= nil (sf/get-asset agent asset2)))

        ;; Upload (& register) asset 2
        (sf/upload agent asset2)

        (is (sf/get-asset agent asset2))))))

(comment
  (run-all-tests)
  )
