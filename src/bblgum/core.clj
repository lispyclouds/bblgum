(ns bblgum.core
  (:require [bblgum.impl :as i]))

(defn gum
  "Main driver of bblgum.

  You can call `gum` like this:

  (gum :command [\"args vector\"] :opt \"value\" :opt2 \"value2\")

  There are several special opts, that are handled by the library:
  :in - An input stream than can be passed to gum
  :as - Coerce the output. Currently supports :bool, :ignored or defaults to a seq of strings
  :gum-path - Path to the gum binary. Defaults to gum

  All other opts are passed to the `gum` CLI. Consult `gum CMD --help` to see available options.
  To pass flags like `--directory` use `:directory true`. Always use full names of the options.

  Usage examples
  --------------
  Command only:
  (gum :file)

  Command with args:
  (gum :choose [\"arg\" \"arg2\"])

  Command with opts:
  (gum :file :directory true)

  Command with arguments and options:
  (gum :choose [\"arg1\" \"arg2\"] :header \"Choose an option\")

  Returns a map of:
  status: The exit code from gum
  result: The output from the execution: seq of lines or coerced via :as."
  [cmd & [args & opts]]
  (when-not
      (or (keyword? cmd)
          (string? cmd)) (throw (IllegalArgumentException. "cmd must be a keyword or a string")))
  (let [{:keys [cmd opts args in as gum-path]} (apply i/prepare-cmd-map cmd args opts)
        gum-path (or gum-path "gum")
        with-opts (->> opts
                       (map (fn [[opt value]]
                              (str "--" (i/->str opt) "=" (i/multi-opt value))))
                       (into [gum-path (i/->str cmd)]))
        out (if (= :ignored as) :inherit :string)
        args (if (or (empty? args) (= "--" (first args)))
               args
               (cons "--" args))
        {:keys [exit out]} (i/exec (into with-opts args) in out)]
    {:status exit
     :result (case as
               :bool (zero? exit)
               out)}))

