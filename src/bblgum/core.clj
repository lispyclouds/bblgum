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
  ([cmd]
   (i/gum* (i/prepare-cmd-map cmd)))
  ([cmd args-or-opts]
   (i/gum* (i/prepare-cmd-map cmd args-or-opts)))
  ([cmd args & opts]
   (i/gum* (apply i/prepare-cmd-map cmd args opts))))
