(ns bblgum.core
  (:require [bblgum.impl :as i]))

(defn- gumv1
  "Main driver fn for using gum: https://github.com/charmbracelet/gum

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

(defn- prepare-cmd-map
  ([cmd]
   (if (map? cmd)
     cmd
     (prepare-cmd-map cmd [] {})))
  ([cmd args-or-opts]
   (if (map? args-or-opts)
     (prepare-cmd-map cmd [] args-or-opts)
     (prepare-cmd-map cmd args-or-opts {}))
)
  ([cmd args options]
   (merge  {:cmd cmd :args args} options)))

(comment
  "Testing examples"
  (prepare-cmd-map :file)                                                  ;; => {:cmd :file, :args []}
  (prepare-cmd-map :choose ["foo" "bar"])                                  ;; => {:cmd :choose, :args ["foo" "bar"]}
  (prepare-cmd-map :choose ["foo" "bar"] {:opts {:header "select a foo"}}) ;; => {:cmd :choose, :args ["foo" "bar"], :opts {:header "select a foo"}}
  (prepare-cmd-map {:cmd :file :args ["src"] :opts {:directory true}}) ;; => {:cmd :file, :args ["src"], :opts {:directory true}}
  (prepare-cmd-map {:cmd :table :in "some.in"})                        ;; => {:cmd :table, :in "some.in"}

  "The arity 2 example, only options"
  (prepare-cmd-map :files {:opts {:directory true}}) ;; => {:cmd :files, :args [], :opts {:directory true}}

  "The arity 2 exmaple, only args"
  (prepare-cmd-map :choose ["foo" "bar"]) ;; => {:cmd :choose, :args ["foo" "bar"]}
  )

(defn gum
  "Main driver fn for using gum: https://github.com/charmbracelet/gum

  You can use it with one map argument with following keys:
  cmd: The interaction command. Can be a keyword. Required
  opts: A map of options to be passed as optional params to gum
  args: A vec of args to be passed as positional params to gum
  in: An input stream than can be passed to gum
  as: Coerce the output. Currently supports :bool, :ignored or defaults to a seq of strings
  gum-path: Path to the gum binary. Defaults to gum

  You can also use a simplified API which extracts cmd and args, allowing for slightly less
  code in many use cases:

  Calling command without any args or opts:
    (gum :file)

  Calling a command with args only:
    (gum :choose [\"foo\" \"bar\"])

  Calling a command with args and opts:
    (gum :choose [\"foo\" \"bar\"] {:opts {:header \"select a foo\"}})

  Calling a command with opts only:
    (gum :file {:opts {:directory true}}])
  
  Returns a map of:
  status: The exit code from gum
  result: The output from the execution: seq of lines or coerced via :as."
  ([cmd]
   (gumv1 (prepare-cmd-map cmd)))
  ([cmd args-or-opts]
   (gumv1 (prepare-cmd-map cmd args-or-opts)))
  ([cmd args opts]
   (gumv1 (prepare-cmd-map cmd args opts))))
