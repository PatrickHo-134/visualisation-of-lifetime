(ns time-remained.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [time-remained.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
