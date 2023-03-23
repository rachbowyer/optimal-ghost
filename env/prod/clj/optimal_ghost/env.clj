(ns optimal-ghost.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[optimal-ghost started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[optimal-ghost has shut down successfully]=-"))
   :middleware identity})
