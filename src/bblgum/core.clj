(ns bblgum.core
  (:require [bblgum.impl :as i]))

(defn- gum*
  "Low level API. If you don't know why you are using `gum*` then you're probably looking for `gum`

  Options map:
  cmd: The interaction command. Can be a keyword. Required
  opts: A map of options to be passed as optional params to gum
  args: A vec of args to be passed as positional params to gum
  in: An input stream than can be passed to gum
  as: Coerce the output. Currently supports :bool, :ignored or defaults to a seq of strings
  gum-path: Path to the gum binary. Defaults to gum

  Returns a map of:
  status: The exit code from gum
  result: The output from the execution: seq of lines or coerced via :as."
  [{:keys [cmd opts args in as gum-path]}]
  (when-not cmd
    (throw (IllegalArgumentException. ":cmd must be provided or non-nil")))
  (let [gum-path (or gum-path "gum")
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

(defn gum
  "High level gum API. Simpler in usage then `gum*`, but uses it under the hood.

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
  ([cmd]
   (gum* (i/prepare-cmd-map cmd)))
  ([cmd args-or-opts]
   (gum* (i/prepare-cmd-map cmd args-or-opts)))
  ([cmd args & opts]
   (gum* (apply i/prepare-cmd-map cmd args opts))))

