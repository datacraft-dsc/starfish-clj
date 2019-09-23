(ns starfish.test-in-memory
  (:require [clojure.test :refer [is are testing deftest run-all-tests]])
  (:require [starfish.core :refer :all]))


(deftest simple-memory-assets
  (testing "memory asset with default metadata"
    (let [a1 (memory-asset "abc")]
      (is (= "abc" (to-string (content a1))))
      (is (= "abc" (slurp (content-stream a1))))
      (is (identical? a1 (asset a1)))
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
        (is (= ["test" "data"] (:tags m1)))
        (is (= m1 (read-json-string (metadata-string a1))))))))

(deftest simple-operation
  (let [op (create-operation [:input] 
                             (fn [{:keys [input]}] {:output input}) {:name "Identity"})
        op-meta (metadata op)]
    (is (= "Identity" (:name op-meta)))
    (is (= "operation" (:type op-meta)))
    
    (let [a (memory-asset "TestIdentity")
          r (invoke-result op {:input a})
          output (:output r)]
      (is (map? r)) ;; check invoke result is a map
      (is (asset? output)) ;; check the output field is populated
      (is (= "TestIdentity" (to-string (content output)))) ;; check identity has been maintained
      )
    
    (let [a (memory-asset "TestIdentity2")
          jb (invoke op {:input a})]
      (is (job? jb)) ;; check invoke results in a job
      (let [r (get-result jb)
            output (:output r)]
        (is (= r (poll-result jb)))
        (is (map? r)) ;; check invoke result is a map
        (is (asset? output)) ;; check the output field is populated
        (is (= "TestIdentity2" (to-string (content output)))) ;; check identity has been maintained) 
        ))))


(comment
  (run-all-tests)
  )
