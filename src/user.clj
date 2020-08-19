(ns user
  (:require [backend.system :as system]
            [clojure.tools.deps.alpha.repl :refer [add-lib]]
            [clojure.tools.gitlibs :as gitlibs]
            [integrant.repl :as ig-repl]))

(ig-repl/set-prep! (constantly system/system-config))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(defn reload []
  (system/stop)
  (system/start))

(defn load-master
  [lib]
  (let [git (str "https://github.com/" lib ".git")]
    (add-lib lib {:git/url git :sha (gitlibs/resolve git "master")})))

(defn handle
  [req]
  ((:backend.system/handler @system/state) req))

(comment
  (system/stop)
  (reload)

  (do
    (reload)
    (handle {:request-method :post
             :headers {"content-type" "application/json"}
             :body "{\"first-name\": \"Sofia\"}"
             :uri "/api/patients"}))

  (add-lib
;   'com.layerware/hugsql {:mvn/version "0.5.1"}
;   'hikari-cp {:mvn/version "2.13.0"})
   )

  (go)
  (halt)
  (reset)
  (reset-all))

