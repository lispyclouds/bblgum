(ns bblgum.impl
  "Internal fns, not intended for public use"
  (:require
    [babashka.process :as p]
    [clojure.string :as str]))

(defn ->str
  [thing]
  (if (keyword? thing)
    (name thing)
    thing))

(defn multi-opt
  [opt]
  (if (sequential? opt)
    (str/join "," opt)
    opt))

(defn exec
  [cmd in-stream out-stream]
  (let [{:keys [exit out]} @(apply p/process
                                   {:out out-stream
                                    :in  (or in-stream :inherit)
                                    :err :inherit}
                                   cmd)
        result             {:exit exit}
        result             (if (= :inherit out-stream)
                             result
                             (assoc result
                                    :out
                                    (->> out
                                         str/trim
                                         str/split-lines
                                         (filter seq))))]
    result))
