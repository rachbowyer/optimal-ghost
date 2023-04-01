(ns optimal-ghost.game-over-page
  (:require
    [optimal-ghost.events :refer [get-winner]]
    [optimal-ghost.styles :as styles]
    [re-frame.core :as rf]))

(defn- game-over-on-click [e]
  (.preventDefault e)
  (.log js/console "game-over-on-click")
  (rf/dispatch [:clear-word])
  (rf/dispatch [:navigate-replace! :home]))

(defn- get-reason [status]
  (case status
    "computer-completes-word" "I have completed a word of over 3 letters"
    "computer-unable-to-move" "These letters cannot form a word of at least 4 letters"
    "opponent-completes-word" "Your have completed a word of over 3 letters"
    "opponent-invalid-word" "These letters cannot be used to form a word"
    "opponent-unable-to-move" "These letters cannot form a word of at least 4 letters"
    ""))

(defn- game-over-page []
  (let [word     @(rf/subscribe [:word])
        status   @(rf/subscribe [:status])
        winner   (get-winner status)]
    [:section
     [:div
      {:class styles/flex-vertical-list}
      [:h2 {:class "text-3xl font-bold my-4 py-4"}
       (case winner
         :human      "You have won!"
         :computer  "I have won!"
         "")]
      [:p {:style styles/the-word} word]
      [:p {:class "my-2 py-2"} (get-reason status)]
      [:button
       {:type :button
        :auto-focus true
        :on-click game-over-on-click
        :class styles/button-style}
        "Play again"]]]))
