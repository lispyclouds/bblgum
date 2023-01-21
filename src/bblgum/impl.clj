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
  [cmd in]
  (let [{:keys [exit out]} @(p/process cmd
                                       {:out :string
                                        :in  (or in :inherit)
                                        :err :inherit})]
    {:exit exit
     :out  (->> out
                str/trim
                str/split-lines
                (filter seq))}))
