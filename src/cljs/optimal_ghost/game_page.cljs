(ns optimal-ghost.game-page
  (:require
    [clojure.string :as str]
    [optimal-ghost.events :refer [state]]
    [optimal-ghost.styles :as styles]
    [re-frame.core :as rf]))

(defn- game-page-on-click [e]
  (.preventDefault e)
  (rf/dispatch [:submit-word]))

(defn- submit-letter-on-click [event]
  (.log js/console "on change" (-> event .-target .-value))
  (let [letter (-> event .-target .-value str/lower-case)]
    (swap! state assoc :letter letter)))

(defn game-page []
  [:section.section>div.container>div.content
   [:div
    {:class "flex flex-col justify-around items-center"}
    [:p {:class "my-4 px-4 py-2 italic"} "Enter letter"]
    [:p {:class "my-4 px-4 py-2"
         :style styles/big-stuff}
     (-> state deref :word)
     [:input {:type "text"
              :id "submit-letter"
              :class "border-2 border-black"
              :tabindex "1"
              :auto-focus true
              :style styles/big-stuff
              :maxLength 1
              :size 1
              :value (-> state deref :letter)
              :on-change submit-letter-on-click}]
     "…"]
    [:button
     (cond-> {:type :button
              :on-click game-page-on-click
              :tabindex "2"
              :class "my-4 px-4 py-2 border-2 border-black rounded-lg text-white bg-blue-700"}
             (-> state deref :letter str/blank?)
             (assoc :disabled "disabled"))
     "Submit"]]
])