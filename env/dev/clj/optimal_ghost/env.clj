(ns optimal-ghost.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [optimal-ghost.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[optimal-ghost started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[optimal-ghost has shut down successfully]=-"))
   :middleware wrap-dev})
