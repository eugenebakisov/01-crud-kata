(ns backend.db
  (:require [clojure.data.json :as json]
            [hikari-cp.core :as hikari]
            [hugsql.adapter :as adapter]
            [hugsql.adapter.clojure-java-jdbc :as jdbc-adapter]
            [hugsql.core :as hugsql]))

(set! *warn-on-reflection* true)

(defn init [config]
  {:datasource (hikari/make-datasource config)})

(defn halt
  [{:keys [datasource]}]
  (hikari/close-datasource datasource))

(defn -pgobj-json?
  [^org.postgresql.util.PGobject pgobj]
  (->> pgobj
       .getType
       (= "jsonb")))

(defn -pgobj->edn
  [^org.postgresql.util.PGobject pgobj]
  (-> pgobj
      .toString
      json/read-json))

(defn pgobj->edn
  [{:keys [data] :as record}]
  (if (and data
           (instance? org.postgresql.util.PGobject data)
           (-pgobj-json? data))
    (update-in record [:data] -pgobj->edn)
    record))

(defn pgobjs->edn
  [items]
  (map pgobj->edn items))

(deftype PgobjToEdnConvertingAdapter [delegate]
  adapter/HugsqlAdapter
  (execute [this db sqlvec options]
    (.println System/out sqlvec)
    (adapter/execute delegate db sqlvec options))
  (query [_ db sqlvec options]
    (adapter/query delegate db sqlvec options))
  (result-one [this result options]
    (-> delegate
        (adapter/result-one result options)
        pgobj->edn))
  (result-many [this result options]
    (-> delegate
        (adapter/result-many result options)
        pgobjs->edn))
  (result-affected [this result options]
    (-> delegate
        (adapter/result-affected result options)
        pgobjs->edn))
  (result-raw [this result options]
    (adapter/result-raw delegate result options))
  (on-exception [this exception]
    (adapter/on-exception delegate exception)))

; TODO: think about copy-pasting HS solution
(let [default-adapter (jdbc-adapter/->HugsqlAdapterClojureJavaJdbc)
      pgobjs->end-adapter (PgobjToEdnConvertingAdapter. default-adapter)]
  (hugsql/def-db-fns "patients.sql" {:adapter pgobjs->end-adapter}))

(comment
  (create-patients-table db-spec)
  (drop-patients-table db-spec)
  (pgobjs->edn (get-active-patients db-spec))
  (get-active-patients {:datasource datasource})

  (pgobj->edn (get-active-patient-by-id db-spec {:id 1}))

  (get-active-patient-by-id (:backend.system/db @backend.system/state) {:id 16})

  (json/write-str {:name "Peter"})
  (second (insert-patient (:backend.system/db @backend.system/state) {:data (json/write-str {:first-name "Sofia"})}))

  (update-whole-patient  {:data (json/write-str {:name "Peter"
                                                 :age 30})
                          :version 0
                          :id 1})

  (deactivate-patient db-spec {:id 2})
  (deactivate-patient db-spec {:id 1
                               :version
                               (:version (first (get-patient-by-id db-spec {:id 1})))})
  (activate-patient db-spec {:id 1})
  (activate-patient db-spec {:id 2})
  (pg->edn (get-patients db-spec))
  (pgobj->edn (first (get-patients db-spec)))
  (instance? java.lang.String nil)
                                        ;
  )
