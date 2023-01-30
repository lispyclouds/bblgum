(ns bblgum.impl-test
  (:require [bblgum.impl :as i]
            [clojure.java.io :as io]
            [clojure.test :as t]))

(t/deftest internal-fns
  (t/testing "coerce keywords to string"
    (t/is (= "foo" (i/->str :foo))))
  (t/testing "do not coerce others to string"
    (t/is (= 42 (i/->str 42))))

  (t/testing "join sequential multi opts"
    (t/is (= "foo,bar,baz" (i/multi-opt ["foo" "bar" "baz"]))))
  (t/testing "dont join non-sequential multi opts"
    (t/is (= "foo" (i/multi-opt "foo"))))

  (t/testing "exec as string"
    (let [{:keys [exit out]} (i/exec ["ls"] nil :string)]
      (t/is (zero? exit))
      (t/is (contains? (set out) "LICENSE"))))
  (t/testing "exec as ignored"
    (t/is (= {:exit 0}
             (i/exec ["ls"] nil :ignored))))
  (t/testing "exec invalid"
    (t/is (not= 0 (:exit (i/exec ["cat" "nope"] nil :ignored)))))
  (t/testing "exec with in"
    (t/is (= {:exit 0 :out ["21"]}
             (i/exec ["wc" "-l"] (io/input-stream "LICENSE") :string)))))
