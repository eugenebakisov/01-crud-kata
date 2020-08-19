(ns backend.system
  (:gen-class)
  (:require [backend.config :as config]
            [backend.db :as db]
            [backend.handler :as handler]
            [integrant.core :as ig]
            [org.httpkit.server :refer [run-server]]))

(set! *warn-on-reflection* true)

(defonce state (atom nil))

(def system-config
  {::server {:config (ig/ref ::config)
             :handler (ig/ref ::handler)}
   ::handler {:db (ig/ref ::db)}
   ::db {:config (ig/ref ::config)}
   ::config nil})

(defmethod ig/init-key ::config [_ _]
  (config/init))

(defmethod ig/init-key ::db [_ {:keys [config]}]
  (db/init (:db config)))

(defmethod ig/halt-key! ::db [_ db]
  (db/halt db))

(defmethod ig/init-key ::handler [_ {:keys [db]}]
  (handler/init db))

(defmethod ig/init-key ::server [_ {:keys [config handler]}]
  (run-server handler (:server config)))

(defmethod ig/halt-key! ::server [_ server]
  (server))

(defn start []
  (when-not @state
    (->> system-config
         ig/init
         (reset! state))))

(defn stop []
  (when @state
    (reset! state (ig/halt! @state))))

(defn -main []
  (start))

(comment
  (start)
  (stop)
  )
