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
  (let [{:keys [exit out]} (apply p/shell
                                  {:out out-stream
                                   :in (or in-stream :inherit)
                                   :err :inherit
                                   :continue true}
                                  cmd)]
    (cond-> {:exit exit}
      (= :string out-stream)
      (assoc :out (->> out
                       str/trim
                       str/split-lines
                       (filter seq))))))

(defn run
  "Low level API. If you don't know why you are using `run` then you're probably looking for `gum`
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
                              (str "--" (->str opt) "=" (multi-opt value))))
                       (into [gum-path (->str cmd)]))
        out (if (= :ignored as) :inherit :string)
        args (if (or (empty? args) (= "--" (first args)))
               args
               (cons "--" args))
        {:keys [exit out]} (exec (into with-opts args) in out)]
    {:status exit
     :result (case as
               :bool (zero? exit)
               out)}))

(defn prepare-options [m]
  (let [fmted-keys [:opts :in :as :gum-path]
        fmted (select-keys m fmted-keys)
        opts (apply dissoc m fmted-keys)]
    (update fmted :opts merge opts)))

(defn prepare-cmd-map
  "Prepares command map to be passed to `run`. Tries to be smart and figure out what user wants."
  ([cmd]
   (if (map? cmd)
     cmd
     (prepare-cmd-map cmd [] nil)))
  ([cmd args-or-opts]
   (if (sequential? args-or-opts)
     (prepare-cmd-map cmd args-or-opts nil)
     (prepare-cmd-map cmd [] args-or-opts)))
  ([cmd args & options]
   (let [args* (if (sequential? args) args [])
         foptions (filter some? options)
         options* (if (keyword? args) (conj foptions args) foptions)]
     (merge {:cmd cmd :args args*} (if (seq options*) (prepare-options (apply hash-map options*)) {})))))

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
   (run (prepare-cmd-map cmd)))
  ([cmd args-or-opts]
   (run (prepare-cmd-map cmd args-or-opts)))
  ([cmd args & opts]
   (run (apply prepare-cmd-map cmd args opts))))

(comment
  "Testing examples"
  (prepare-cmd-map :file) ;; => {:cmd :file, :args []}
  (prepare-cmd-map :file :directory true) ;; => {:cmd :file, :args [], :opts {:directory true}}
  (prepare-cmd-map :choose ["foo" "bar"]) ;; => {:cmd :choose, :args ["foo" "bar"]}
  (prepare-cmd-map :choose ["foo" "bar"] :header "select a foo") ;; => {:cmd :choose, :args ["foo" "bar"], :opts {:header "select a foo"}}
  (prepare-cmd-map {:cmd :file :args ["src"] :directory true}) ;; => {:cmd :file, :args ["src"], :directory true}
  (prepare-cmd-map :table :in "some.in" :height 10) ;; => {:cmd :table, :args [], :in "some.in", :opts {:height 10}}
  (prepare-cmd-map {:cmd :table :in "some.in"}) ;; => {:cmd :table, :in "some.in"}
  (prepare-cmd-map :table :in "input" :height 10) ;; => {:cmd :table, :args [], :in "input", :opts {:height 10}}
  (prepare-cmd-map :confirm ["Are you sure?"] :as :bool :negative "Never" :affirmative "Always")) ;; => {:cmd :confirm, :args ["Are you sure?"], :as :bool, :opts {:affirmative "Always", :negative "Never"}}
