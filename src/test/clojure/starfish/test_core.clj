(ns starfish.test-core
  (:require [clojure.test :refer [is testing deftest run-all-tests]])
  (:require [starfish.core :refer :all]))

(deftest test-byte-strings
  (is (= "Foo" (to-string (to-bytes "Foo"))))
  (is (= "Foo" (to-string (to-string (to-bytes "Foo")))))
  (is (= "Foo" (to-string (to-bytes (to-bytes "Foo"))))))


(comment
  (run-all-tests)
  )