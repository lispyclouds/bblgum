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

(defn prepare-options [m]
  (let [fmted-keys [:opts :in :as :gum-path]
        fmted (select-keys m fmted-keys)
        opts (apply dissoc m fmted-keys)]
    (update fmted :opts merge opts)))

(defn prepare-cmd-map
  "Prepares command map to be passed to `gum*`. Tries to be smart and figure out what user wants."
  ([cmd]
   (if (map? cmd)
     cmd
     (prepare-cmd-map cmd [] nil)))
  ([cmd args-or-opts]
   (if (vector? args-or-opts)
     (prepare-cmd-map cmd args-or-opts nil)
     (prepare-cmd-map cmd [] args-or-opts)))
  ([cmd args & options]
   (let [args* (if (vector? args) args [])
         foptions (filter some? options)
         options* (if (keyword? args) (conj foptions args) foptions)]
     (merge {:cmd cmd :args args*} (if (seq options*) (prepare-options (apply hash-map options*)) {})))))

(comment
  "Testing examples"
  (prepare-cmd-map :file)                                        ;; => {:cmd :file, :args []}
  (prepare-cmd-map :file :directory true)                        ;; => {:cmd :file, :args [], :opts {:directory true}}
  (prepare-cmd-map :choose ["foo" "bar"])                        ;; => {:cmd :choose, :args ["foo" "bar"]}
  (prepare-cmd-map :choose ["foo" "bar"] :header "select a foo") ;; => {:cmd :choose, :args ["foo" "bar"], :opts {:header "select a foo"}}
  (prepare-cmd-map {:cmd :file :args ["src"] :directory true})   ;; => {:cmd :file, :args ["src"], :directory true}
  (prepare-cmd-map :table :in "some.in" :height 10) ;; => {:cmd :table, :args [], :in "some.in", :opts {:height 10}}
  (prepare-cmd-map {:cmd :table :in "some.in"})     ;; => {:cmd :table, :in "some.in"}
  (prepare-cmd-map :table :in "input" :height 10)   ;; => {:cmd :table, :args [], :in "input", :opts {:height 10}}
  (prepare-cmd-map :confirm ["Are you sure?"] :as :bool :negative "Never" :affirmative "Always") ;; => {:cmd :confirm, :args ["Are you sure?"], :as :bool, :opts {:affirmative "Always", :negative "Never"}}
  )
