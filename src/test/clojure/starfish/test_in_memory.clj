(ns starfish.test-in-memory
  (:require [clojure.test :refer [is are testing deftest run-all-tests]])
  (:require [starfish.core :refer :all]))


(deftest simple-memory-assets
  (testing "memory asset with default metadata"
    (let [a1 (memory-asset "abc")]
      (is (= "abc" (to-string (content a1))))
      (is (= "abc" (slurp (content-stream a1))))
      (let [m1 (metadata a1)]
        (is (= "dataset" (:type m1)))
        (is (= "3" (:size m1))))))
  
  (testing "memory asset with metadata"
    (let [tagdata ["test" "data"]
          mdata {:tags tagdata}
          a1 (memory-asset mdata "abc")]
      (is (= tagdata (:tags (metadata a1))))
      (is (= "abc" (to-string (content a1))))
      (let [m1 (metadata a1)]
        (is (= "dataset" (:type m1)))
        (is (= ["test" "data"] (:tags m1)))))))



(comment
  (run-all-tests)
  )
