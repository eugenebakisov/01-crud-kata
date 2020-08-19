(ns backend.config
  (:require [clojure.core :refer [slurp]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(set! *warn-on-reflection* true)

(defn read-config-file
  [env]
  (let [json (-> env
                 (str ".json")
                 io/resource
                 slurp
                 (json/read-json))]
    json))

(defn init []
  (read-config-file (System/getProperty "env" "dev")))

(comment
  (init)

)
