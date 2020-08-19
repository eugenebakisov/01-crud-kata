(ns backend.patients
  (:require [backend.db :as db]
            [clojure.data.json :as json]))

(set! *warn-on-reflection* true)

(defn create-patient
  [{:keys [parameters db]}]
  (let [record {:data (-> parameters
                          :body
                          json/write-str)}
        created-id (second (db/insert-patient db record))
        created (db/get-active-patient-by-id db {:id created-id})]
    {:status 201
     :body created}))

(defn get-patients
  [{:keys [db]}]
  {:status 200
   :body (db/get-active-patients db)})

; TODO: tests: sql -> integration tests, e2e? same, tests against handler
(defn get-patient-by-id
  [{:keys [parameters db]}]
  (let [id (:path parameters)]
    (if-let [patient (db/get-active-patient-by-id db id)]
      {:status 200
       :body patient}
      {:status 404})))

; TODO: move out
(defn deep-merge* [& maps]
  (let [f (fn [old new]
            (if (and (map? old) (map? new))
              (merge-with deep-merge* old new)
              new))]
    (if (every? map? maps)
      (apply merge-with f maps)
      (last maps))))

(defn deep-merge [& maps]
  (let [maps (filter identity maps)]
    (assert (every? map? maps))
    (apply merge-with deep-merge* maps)))

; TODO: cond?
(defn update-patient
  [{:keys [parameters db]}]
  (let [id (:path parameters)
        body (:body parameters)
        updating-patient (db/get-active-patient-by-id db id)
        new-patient-info (deep-merge updating-patient {:data body})] ;TODO: what if upd is empty
    (if updating-patient
      (if (->> [new-patient-info updating-patient]
               (map :data)
               (apply =))
        {:status 200 :body updating-patient}
        (if (db/update-whole-patient db (update-in new-patient-info [:data] json/write-str))
          {:status 200 :body (db/get-active-patient-by-id db id)}
          {:status 409}))
      {:status 404})))

(defn deactivate-patient
  [{:keys [parameters db]}]
  (let [id (:path parameters)
        before-deactivation (db/get-active-patient-by-id db id)]
    (if before-deactivation
      (do
        (db/deactivate-patient db id)
        {:status 200
         :body {:deleted true
                :patient before-deactivation}})
      {:status 204})))
