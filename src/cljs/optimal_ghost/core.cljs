(ns optimal-ghost.core
  (:require
    [clojure.string :as str]
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [optimal-ghost.ajax :as ajax]
    [optimal-ghost.events :refer [state]]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string])
  (:import goog.History))

(defn header []
  ;[:h1 "Optimal Ghost"]
  [:img {:src "/img/logo.png"}])

(defn- start-on-click
  [e]
  (.preventDefault e)
  (rf/dispatch [:navigate-replace! :game]))

(defn home-page []
  [:section.section>div.container>div.content
   [:div
    [:p [:i (str "\"In the game of Ghost, two players take turns building up an English word "
             "from left to right. Each player adds one letter per turn. The goal is to not "
             "complete the spelling of a word: if you add a letter that completes a word "
             "(of 4+ letters), or if you add a letter that produces a string that cannot be "
             "extended into a word, you lose...\"")]]]
    [:div
      [:p]
      [:button {:type :button :on-click start-on-click} "Start"]
      [:p]
      [:p]
     ]
   [:div
    [:a {:href "https://bowyer.info"} "By Rachel Bowyer"]
    [:span "       "]
    [:a {:href "https://www.nellshaw.com"} "Designed by Nell Shaw"]
    ]
   ])

(defn- game-page-on-click [e]
  (.preventDefault e)
  ;(let [submit-letter-element (.getElementById js/document "submit-letter")
  ;      letter (.-value submit-letter-element)]
  ; )
  (rf/dispatch [:submit-word]))

(defn- submit-letter-on-click [event]
  (.log js/console "on change" (-> event .-target .-value))
  (swap! state assoc :letter (-> event .-target .-value)))

(def ^:private big-stuff {:font-size "50px"
                          :font-family "Courier New, Courier, monospace"})

(defn game-page []
  ;(js/console.log "game-page" @state)
  [:section.section>div.container>div.content
  [:div
   [:p {:style big-stuff}
    (-> state deref :word)
    [:input {:type "text"
             :id "submit-letter"
             :style big-stuff
             :maxLength 1
             :size 1
             :value (-> state deref :letter)
             :on-change submit-letter-on-click}]
    "..."]]
   [:p]
   [:button
    (cond-> {:type :button :on-click game-page-on-click}
            (-> state deref :letter str/blank?)
            (assoc :disabled "disabled"))
    "Submit"]
   [:p]
   [:p "Enter a letter to add to the word"]])

(defn- won-page-on-click [e]
  (.preventDefault e)
  (swap! state assoc :word "")
  (rf/dispatch [:navigate-replace! :home]))

(defn- won-page []
  [:section.section>div.container>div.content
   [:div
    [:p {:style big-stuff} (-> state deref :word)]
    [:p]
    [:p "You won!"]
    [:p]
    [:p "You have forced me to complete a word. Well done." ]
     [:p]
    [:p]
    [:button {:type :button :on-click won-page-on-click} "Play again"]
   ]])


(defn- lost-page []
  [:section.section>div.container>div.content
   [:div
    [:p {:style big-stuff} (-> state deref :word)]
    [:p]
    [:p "Loser!"]
    [:p]
    (cond
      (= (-> state deref :status) "opponent-completes-word" )
      [:p "You have completed a word"]

      (= (-> state deref :status) "opponent-invalid-word" )
      [:p "That's not a word silly"])
    [:p]
    [:p]
    [:button {:type :button :on-click won-page-on-click} "Play again"]
    ]])

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [header]
     [page]]))


(defn navigate! [match _]
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
