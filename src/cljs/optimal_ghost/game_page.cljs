(ns optimal-ghost.game-page
  (:require
    [clojure.string :as str]
    [optimal-ghost.styles :as styles]
    [re-frame.core :as rf]))

(defn- game-page-on-click [e]
  (.preventDefault e)
  (rf/dispatch [:submit-word]))

(defn- submit-letter-on-click [event]
  (let [letter (-> event .-target .-value str/lower-case)]
    (rf/dispatch [:letter letter])))

(defn game-page []
  (let [word              @(rf/subscribe [:word])
        letter            @(rf/subscribe [:letter])
        letter-submitted  @(rf/subscribe [:letter-submitted])]
    [:section
     [:div
      {:class styles/flex-vertical-list}
      [:p {:class "my-4 px-4 py-2 italic"} "Enter letter"]
      [:p {:class "my-4 px-4 py-2"
           :style styles/the-word}
       word
       [:input (cond-> {:type       "text"
                        :id         "submit-letter"
                        :class      "border-2 border-black"
                        :auto-focus true
                        :style      styles/the-word
                        :maxLength  1
                        :size       1
                        :value      letter
                        :on-change  submit-letter-on-click}
                       letter-submitted
                       (assoc :disabled "disabled"))]
       "…"]
      [:button
       (cond-> {:type     :button
                :on-click game-page-on-click
                :class    styles/button-style}
               letter-submitted
               (assoc :disabled "disabled"))
       "Submit"]]]))