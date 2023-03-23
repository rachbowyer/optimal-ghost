(ns optimal-ghost.app
  (:require [optimal-ghost.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
