(ns lifetime-visualisation.events
  (:require [cljs-time.core :as time.core]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [lifetime-visualisation.db :as db]
            [lifetime-visualisation.utils :as utils]
            [re-frame.core :as re-frame :refer [reg-event-db reg-event-fx
                                                reg-sub]]))

(reg-event-db ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(reg-event-fx ::navigate
  (fn-traced [_ [_ handler]]
   {:navigate handler}))

(reg-event-fx ::set-active-panel
 (fn-traced [{:keys [db]} [_ active-panel]]
   {:db (assoc db :active-panel active-panel)}))

(reg-sub :form
  (fn [db _]
    (:form db)))

(reg-sub :start-date
  (fn [db _]
    (when-let [d (get-in db [:form :start-date])]
      (utils/date->iso-date-str d))))

(reg-sub :end-date
  (fn [db _]
    (when-let [d (get-in db [:form :end-date])]
      (utils/date->iso-date-str d))))

(reg-sub :dates-sequence
  (fn [db _]
    (let [{:keys [start-date end-date frequency]} (get-in db [:form])]
      (if (and start-date end-date frequency)
        (-> (utils/create-dates-during-period start-date end-date frequency)
            (utils/produce-date-sequence (time.core/today)))
        []))))

(reg-sub :value
  :<- [:form]
  (fn [doc [_ path]]
    (get-in doc path)))

(reg-event-db :set-value
  (fn [db [_ path value]]
    (assoc-in db (into [:form] path) value)))

(reg-event-db :update-value
  (fn [db [_ f path value]]
    (update-in db (into [:form] path) f value)))

(reg-event-db :toggle-end-date
  (fn [db [_ _]]
    (let [enabled? (get-in db [:form :enable-end-date?] false)]
      (-> db
          (assoc-in [:form :enable-end-date?] (not enabled?))
          (assoc-in [:form :enable-occurences?] enabled?)))))

(reg-event-db :toggle-occurences
  (fn [db [_ _]]
    (let [enabled? (get-in db [:form :enable-occurences?] false)]
      (-> db
          (assoc-in [:form :enable-occurences?] (not enabled?))
          (assoc-in [:form :enable-end-date?] enabled?)))))

(reg-event-fx :initialize-form
  (fn [{:keys [db]} _]
    {:db (assoc-in db [:form] {:start-date (utils/str->date "2000-01-01")
                               :end-date   (utils/str->date "2080-01-01")
                               :enable-end-date? true
                               :enable-occurences? false
                               :frequency  :monthly})
     :dispatch [:recalculate-dates-sequence]}))

(reg-event-db :calculate-end-date-and-dates-sequence
  (fn [db _]
    (let [{:keys [start-date occurences frequency]} (get-in db [:form])
          dates-sequence (if (and start-date occurences frequency)
                           (-> (utils/create-dates-of-occurence start-date occurences frequency)
                               (utils/produce-date-sequence (time.core/today))
                               vec)
                           [])]
      (update-in db [:form] merge {:end-date       (-> dates-sequence last :date)
                                   :dates-sequence dates-sequence}))))

(reg-event-db :calculate-occurences-and-dates-sequence
  (fn [db _]
    (let [{:keys [start-date end-date frequency]} (get-in db [:form])
          dates-sequence (if (and start-date end-date frequency)
                           (-> (utils/create-dates-during-period start-date end-date frequency)
                               (utils/produce-date-sequence (time.core/today))
                               vec)
                           [])]
      (update-in db [:form] merge {:occurences     (count dates-sequence)
                                   :dates-sequence dates-sequence}))))

(reg-event-fx :recalculate-dates-sequence
  (fn [{:keys [db]} _]
    (let [{:keys [start-date end-date occurences frequency]} (get-in db [:form])]
      {:dispatch (cond
                   (and start-date end-date frequency)
                   [:calculate-occurences-and-dates-sequence]

                   (and start-date occurences frequency)
                   [:calculate-end-date-and-dates-sequence]

                   :else
                   nil)})))

