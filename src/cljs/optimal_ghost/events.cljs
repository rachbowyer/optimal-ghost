(ns optimal-ghost.events
  (:require
    [ajax.core :as ajax]
    [day8.re-frame.http-fx]
    [re-frame.core :as rf]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))

;;;; Constants

(def ^:private network-error
  "Unable to connect to the server. Please check your internet connection. Automatically retrying in 15 secs...")

(def ^:private server-retry-timeout 15000)


;;;; Helper
(defn get-winner
  [status]
  (cond
    (#{"computer-completes-word" "computer-unable-to-move"} status)
    :human

    (#{"opponent-completes-word" "opponent-invalid-word" "opponent-unable-to-move"} status)
    :computer

    (= status "in-progress")
    :in-progress

    :else
    :unknown))


;;;; Dispatchers

(rf/reg-event-db
  :navigate
  (fn [db [_ match]]
    (let [old-match (:route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :route new-match))))

(rf/reg-fx
  :navigate-replace-fx!
  (fn [[k & [params query]]]
    (rfe/replace-state k  params query)))

(rf/reg-event-fx
  :navigate-replace!
  (fn [_ [_ url-key params query]]
    {:navigate-replace-fx! [url-key params query]}))

(rf/reg-event-fx
  :set-focus-submit-letter
  (fn [_ _ ]
    (js/setTimeout #(-> js/document (.getElementById "submit-letter") .focus) 100)
    {}))

(rf/reg-event-fx
  :submit-word-success
  (fn [{:keys [db]}  [_ letter {:keys [status move] :as _result}]]
    (rf/dispatch
      (if (= :in-progress (get-winner status))
        [:set-focus-submit-letter]
        [:navigate-replace! :game-over]))
    {:db
     (-> db
         (assoc :letter ""
                :letter-submitted false
                :error nil
                :status status)
         (update :word #(str % letter move)))}))

(rf/reg-event-fx
  :submit-word-failure
  (fn [{:keys [db]} _]
    (js/setTimeout #(rf/dispatch [:submit-word]) server-retry-timeout)
    {:db (assoc db :error network-error)}))

(rf/reg-event-fx
  :submit-word
  (fn [{:keys [db]} [_]]
    (let [letter      (:letter db)
          body-params {:word (str (:word db) letter)}]
      {:http-xhrio {:uri             (str "/api/get-move")
                    :method          :post
                    :timeout         10000
                    :params          body-params
                    :format          (ajax/json-request-format)
                    :request-format  (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:submit-word-success letter]
                    :on-failure      [:submit-word-failure]}

       :db (assoc db :letter-submitted true)})))

(rf/reg-event-fx
  :submit-version-success
  (fn [{:keys [db]} [_ {:keys [version] :as _result}]]
    {:db (assoc db :version version
                   :error nil)}))
(rf/reg-event-fx
  :submit-version-failure
  (fn [{:keys [db]} _]
    (js/setTimeout #(rf/dispatch [:submit-version]) server-retry-timeout)
    {:db (assoc db :error network-error)}))


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

(rf/reg-event-db
  :initialise
  (fn [db _]
    (rf/dispatch [:submit-version])
    (assoc db :initialised? true
              :word ""
              :letter ""
              :status nil
              :version ""
              :letter-submitted false
              :error nil)))

(rf/reg-event-db
  :letter
  (fn [db [_ letter]]
    (assoc db :letter letter)))

(rf/reg-event-db
  :clear-word
  (fn [db _]
    (assoc db :word "")))


;;;; Subscriptions

(rf/reg-sub
  :initialised?
  (fn [db _]
    (-> db :initialised?)))

(rf/reg-sub
  :version
  (fn [db _]
    (:version db)))

(rf/reg-sub
  :error
  (fn [db _]
    (:error db)))

(rf/reg-sub
  :word
  (fn [db _]
    (:word db)))

(rf/reg-sub
  :letter
  (fn [db _]
    (:letter db)))

(rf/reg-sub
  :letter-submitted
  (fn [db _]
    (:letter-submitted db)))

(rf/reg-sub
  :status
  (fn [db _]
    (:status db)))

(rf/reg-sub
  :route
  (fn [db _]
    (:route db)))

(rf/reg-sub
  :page
  :<- [:route]
  (fn [route _]
    (-> route :data :view)))
