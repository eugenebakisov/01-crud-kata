(ns backend.routes
  (:require [backend.patients :as p]
            [clojure.spec.alpha :as s]))

(set! *warn-on-reflection* true)

(s/def ::first-name string?)
(s/def ::third-name string?)
(s/def ::put-body (s/keys :opt-un [::first-name ::third-name]))

(defn routes []
  [["/api"
    ["/patients"
     ["" {:get p/get-patients
          :post {:parameters {:body ::put-body}
                 :handler p/create-patient}}]
     ["/:id" {:parameters {:path {:id int?}}
              :put {:parameters {:body ::put-body}
                    :handler p/update-patient}
              :get p/get-patient-by-id
              :delete p/deactivate-patient}]]]
   ["/health" {:get (constantly
                     {:status 200
                      :body "ok"})}]])

(comment
; todo: get-routes
  )
