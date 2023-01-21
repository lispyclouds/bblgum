(ns bblgum.core
  (:require [bblgum.impl :as i]))

(defn gum
  "Main driver fn for using gum: https://github.com/charmbracelet/gum

  Options map:
  cmd: The interaction command. Can be a keyword. Required
  opts: A map of options to be passed as optional params to gum
  args: A vec of args to be passed as positional params to gum
  in: An input stream than can be passed to gum
  as: Coerce the output. Currently supports :bool or defaults to a seq of strings
  gum-path: Path to the gum binary. Defaults to gum

  Returns a map of:
  status: The exit code from gum.
  result: The output from the execution: seq of lines or coerced via :as."
  [{:keys [cmd opts args in as gum-path]}]
  (let [gum-path           (or gum-path "gum")
        with-opts          (->> opts
                                (map (fn [[opt value]]
                                       (str "--" (i/->str opt) "=" (i/multi-opt value))))
                                (into [gum-path (i/->str cmd)]))
        {:keys [exit out]} (i/exec (into with-opts args) in)]
    {:status exit
     :result (case as
               :bool (zero? exit)
               out)}))
