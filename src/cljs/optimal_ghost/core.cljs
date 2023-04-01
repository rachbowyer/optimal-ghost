(ns optimal-ghost.core
  (:require
    [day8.re-frame.http-fx]
    [optimal-ghost.ajax :as ajax]
    [optimal-ghost.events] ;; To ensure code is included
    [optimal-ghost.home-page :refer [home-page]]
    [optimal-ghost.game-page :refer [game-page]]
    [optimal-ghost.game-over-page :refer [game-over-page]]
    [re-frame.core :as rf]
    [reagent.dom :as rdom]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe])
  (:import goog.History))


(defn- header []
  (let [error @(rf/subscribe [:error])]
    [:div
     (if error
       [:div
          {:class "bg-red-100 border border-red-400 px-4 py-3 rounded relative"}
        [:h1
         {:class "text-red-700 font-bold text-xl"}
         "Error"]
        [:p error]]
       [:div])
     [:div
      {:class "flex flex-col justify-centre"}
      [:div {:class "flex justify-center items-center"}
       [:img {:src "/img/ghost.png" :width "100px" :height "100px" :alt "Picture of a ghost"}]
       [:h1 {:class "text-6xl font-bold text-blue-700"}
        "Optimal Ghost"]]]]))

(defn page []
  (let [requested-page @(rf/subscribe [:page])
        initialised?   @(rf/subscribe [:initialised?])
        page           (if (and initialised? requested-page)
                         requested-page #'home-page)]
    (when (not initialised?)
      (rf/dispatch [:initialise]))
    (when page
      [:div
       [header]
       [page]])))

(defn navigate! [match _]
  (rf/dispatch [:navigate match]))

(def ^:private router
  (reitit/router
    [["/"           {:name        :home
                     :view        #'home-page}]
     ["/game"       {:name        :game
                     :view        #'game-page}]
     ["/game-over"  {:name        :game-over
                     :view        #'game-over-page}]]))

(defn- start-router! []
  (rfe/start!
    router
    navigate!
    {}))


;;;; Initialize app

(defn- ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))





