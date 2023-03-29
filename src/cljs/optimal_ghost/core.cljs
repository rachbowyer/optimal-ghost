(ns optimal-ghost.core
  (:require
    [day8.re-frame.http-fx]
    [optimal-ghost.ajax :as ajax]
    [optimal-ghost.home-page :refer [home-page]]
    [optimal-ghost.game-page :refer [game-page]]
    [optimal-ghost.game-over-page :refer [won-page lost-page]]
    [reagent.dom :as rdom]
    [re-frame.core :as rf]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe])
  (:import goog.History))

(defn- header []
  [:div.logo-container
   {:class "flex flex-col justify-centre"}
   [:div {:class "flex justify-center items-center"}
    [:img {:src "/img/ghost.png" :width "100px" :height "100px" :alt "Picture of a ghost"}]
    [:h1 {:class "text-6xl font-bold text-blue-700"}
     "Optimal Ghost"]]])


(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [header]
     [page]]))


(defn navigate! [match _]
  (rf/dispatch [:submit-version])
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/game" {:name        :game
               :view        #'game-page}]
     ["/won" {:name         :won
              :view        #'won-page}]
     ["/lost" {:name       :lost
              :view        #'lost-page}]
     ]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
