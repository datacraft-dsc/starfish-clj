(ns starfish.test-in-memory
  (:require [clojure.test :refer [is are testing deftest run-all-tests]])
  (:require [starfish.core :refer :all]))


(deftest asset-creation
  (testing "memory asset with default metadata"
    (let [a1 (memory-asset "abc")]
      (is (= "abc" (to-string (content a1))))
      (is (= "abc" (slurp (content-stream a1))))
      (let [m1 (metadata a1)]
        (is (= "dataset" (:type m1))))))
  
  (testing "memory asset with metadata"
    (let [tagdata ["test" "data"]
          mdata {:tags tagdata}
          ast (memory-asset mdata "abc")]
      (is (= tagdata (:tags (metadata ast))))
      (is (= "abc" (to-string (content ast)))))))



(comment
  (run-all-tests)
  )
