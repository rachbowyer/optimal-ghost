(ns optimal-ghost.game-over-page
  (:require
    [optimal-ghost.events :refer [state]]
    [optimal-ghost.styles :as styles]
    [re-frame.core :as rf]))

(defn- won-page-on-click [e]
  (.preventDefault e)
  (swap! state assoc :word "")
  (rf/dispatch [:navigate-replace! :home]))

(defn- get-won-reason []
  (case (-> state deref :status)
    "computer-completes-word" "I have completed a word of over 3 letters"
    "computer-unable-to-move" "These letters cannot form a word of at least 4 letters"))

(defn- won-page []
  [:section.section>div.container>div.content
   [:div
    {:class "flex flex-col justify-around items-center"}
    [:h2 {:class "text-3xl font-bold my-4 py-4"} "You have won!"]
    [:p {:style styles/big-stuff} (-> state deref :word)]
    [:p {:class "my-2 py-2"} (get-won-reason)]
    [:button
     {:type :button
      :on-click won-page-on-click
      :class "my-4 px-4 py-2 border-2 border-black rounded-lg text-white bg-blue-700"}
      "Play again"]]])



(defn- get-lost-reason []
  (case (-> state deref :status)
    "opponent-completes-word" "Your have completed a word of over 3 letters"
    "opponent-invalid-word" "These letters cannot be used to form a word"
    "opponent-unable-to-move" "These letters cannot form a word of at least 4 letters"))

(defn- lost-page []
  [:section.section>div.container>div.content
   [:div
    {:class "flex flex-col justify-around items-center"}
    [:h2 {:class "text-3xl font-bold my-4 py-4"} "I have won!"]
    [:p {:style styles/big-stuff} (-> state deref :word)]
    [:p {:class "my-2 py-2"} (get-lost-reason)]
    [:button
     {:type :button
      :on-click won-page-on-click
      :class "my-4 px-4 py-2 border-2 border-black rounded-lg text-white bg-blue-700"}
     "Play again"]]])


