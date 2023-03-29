(ns optimal-ghost.events
  (:require
    [day8.re-frame.http-fx]
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reagent.core :as r]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))


(def state (r/atom {:word ""
                    :status nil
                    :letter ""
                    :version ""}))

;;dispatchers

(rf/reg-event-db
  :common/navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :common/route new-match))))

(rf/reg-fx
  :common/navigate-fx!
  (fn [[k & [params query]]]
    (.log js/console "navigate-fx!!" k params query)
    (rfe/push-state k params query)))

(rf/reg-fx
  :common/navigate-replace-fx!
  (fn [[k & [params query]]]
    (.log js/console "navigate-replace-fx!!" k params query)
    (rfe/replace-state k  params query)))

(rf/reg-event-fx
  :navigate-replace!
  (fn [_ [_ url-key params query]]
    (.log js/console ":navigate-replace!" url-key params query)
    {:common/navigate-replace-fx! [url-key params query]}))

(rf/reg-event-fx
  :common/navigate!
  (fn [_ [_ url-key params query]]
    (.log js/console "navigate!" url-key params query)
    {:common/navigate-fx! [url-key params query]}))


(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-event-fx
  :page/init-home
  (fn [_ _]))


(rf/reg-event-fx
  :submit-word-success
  (fn [_cofx  [_ letter {:keys [status move] :as _result}]]
    (swap! state update :word #(str % letter move))
    (swap! state assoc :status status)

    (cond
      (#{"computer-completes-word" "computer-unable-to-move"} status)
      (rf/dispatch [:navigate-replace! :won])

      (#{"opponent-completes-word" "opponent-invalid-word" "opponent-unable-to-move"}  status)
      (rf/dispatch [:navigate-replace! :lost])

      :else
      (-> js/document (.getElementById "submit-letter") .focus))))

(rf/reg-event-fx
  :submit-word-failure
  (fn [cofx _]
    (js/alert "Submit word failure")))

(rf/reg-event-fx
  :submit-word
  (fn [_cofx [_]]
    (let [letter      (-> state deref :letter)
          body-params {:word (str (-> state deref :word) letter)}]
      (swap! state assoc :letter "")
      {:http-xhrio {:uri       (str "/api/get-move")
                    :method          :post
                    :timeout         10000
                    :params          body-params
                    :format          (ajax/json-request-format)
                    :request-format  (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:submit-word-success letter]
                    :on-failure      [:submit-word-failure]}})))


(rf/reg-event-fx
  :submit-version-success
  (fn [_cofx  [_  {:keys [version] :as _result}]]
    (swap! state assoc :version version)))

(rf/reg-event-fx
  :submit-version-failure
  (fn [cofx _]
    (js/alert "Submit version failure")))


(rf/reg-event-fx
  :submit-version
  (fn [_cofx [_]]
    {:http-xhrio {:uri             (str "/api/get-version")
                  :method          :get
                  :timeout         10000
                  :format          (ajax/json-request-format)
                  :request-format  (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:submit-version-success]
                  :on-failure      [:submit-version-failure]}}))

;;subscriptions

(rf/reg-sub
  :common/route
  (fn [db _]
    (-> db :common/route)))

(rf/reg-sub
  :common/page-id
  :<- [:common/route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :common/page
  :<- [:common/route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))
