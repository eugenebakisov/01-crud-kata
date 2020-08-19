(ns backend.handler
  (:require
   [backend.middleware :as middlware]
   [backend.routes :refer [routes]]
   [muuntaja.core :as m]
   reitit.coercion.spec
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.dev :refer [print-request-diffs]]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]))

(set! *warn-on-reflection* true)

(defn init [db]
  (ring/ring-handler
   (ring/router
    (routes)
    {;:reitit.middleware/transform print-request-diffs
     :data {:db db
            :coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware [;; content-negotiation
                         muuntaja/format-negotiate-middleware
                         ;; encoding response body
                         muuntaja/format-response-middleware
                         ;; exception handling
                         ;exception/exception-middleware
                         ;; decoding request body
                         muuntaja/format-request-middleware
                         ;; coercing response bodies
                         coercion/coerce-response-middleware
                         ;; coercing request parameters
                         coercion/coerce-request-middleware
                         parameters/parameters-middleware
                         middlware/db-injecting-middleware]}})
   (ring/routes
    (ring/redirect-trailing-slash-handler)
    (ring/create-default-handler
     {:not-found (constantly {:status 404
                              :body "Route not found"})}))))
