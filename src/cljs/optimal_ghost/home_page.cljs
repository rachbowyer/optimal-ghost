(ns optimal-ghost.home-page
  (:require
    [re-frame.core :as rf]
    [optimal-ghost.events :refer [state]]))


(defn- start-on-click
  [e]
  (.preventDefault e)
  (rf/dispatch [:navigate-replace! :game]))


(defn home-page []
  [:section.section>div.container>div.content
   [:div.home-page-container
    {:class "flex flex-col justify-around items-center"}
    [:p
     {:class "my-4 px-4 py-2 italic"}
     (str "\"In the game of Ghost, two players take turns building up an English word "
                 "from left to right. Each player adds one letter per turn. The goal is to not "
                 "complete the spelling of a word: if you add a letter that completes a word "
                 "(of 4+ letters), or if you add a letter that produces a string that cannot be "
                 "extended into a word, you lose...\"")]

    [:button {:type :button
              :on-click start-on-click
              :auto-focus true
              :class "my-4 px-4 py-2 border-2 border-black rounded-lg text-white bg-blue-700"}
     "Start"]

    [:div {:class "flex justify-between px-4 py-2" :style {:width "100%" :margin-top "100px"}}
     [:a {:class "hover:text-blue-700 text-sm" :href "https://bowyer.info"} "By Rachel Bowyer"]
     [:p {:class "italic px-4 text-sm"} "Version: " (:version @state)]]
    ]
   ])
