(ns lifetime-visualisation.events
  (:require [cljs-time.coerce :as time.coerce]
            [cljs-time.core :as time.core]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [lifetime-visualisation.db :as db]
            [lifetime-visualisation.utils :as utils]
            [re-frame.core :as re-frame :refer [reg-event-db reg-event-fx
                                                reg-sub]]))

(defn default-form-data [title]
  (let [today              (time.coerce/to-date (time.core/now))
        same-day-next-year (-> (time.core/now)
                               (time.coerce/to-date-time)
                               (time.core/plus (time.core/years 1))
                               (time.coerce/to-date))]
    {:title              title
     :start-date         today
     :end-date           same-day-next-year
     :enable-end-date?   true
     :enable-occurences? false
     :disable-title?     true
     :frequency          :weekly}))

(reg-sub :sections
  (fn [db _]
    (:sections db)))

(reg-sub :section-data
  (fn [db [_ section-id]]
    (get-in db [:sections section-id])))

(reg-sub :start-date
  (fn [db [_ form-id]]
    (when-let [d (get-in db [:sections form-id :start-date])]
      (utils/date->iso-date-str d))))

(reg-sub :end-date
  (fn [db [_ form-id]]
    (when-let [d (get-in db [:sections form-id :end-date])]
      (utils/date->iso-date-str d))))

(reg-sub :value
  (fn [db [_ form-id key-path]]
    (get-in db (into [:sections form-id] key-path))))

(reg-event-db ::initialize-db
  (fn-traced [_ _]
             db/default-db))

(reg-event-fx ::navigate
  (fn-traced [_ [_ handler]]
             {:navigate handler}))

(reg-event-fx ::set-active-panel
  (fn-traced [{:keys [db]} [_ active-panel]]
             {:db (assoc db :active-panel active-panel)}))

(reg-event-db :set-value
  (fn [db [_ form-id path value]]
    (assoc-in db (into [:sections form-id] path) value)))

(reg-event-db :toggle-end-date
  (fn [db [_ form-id]]
    (let [enabled? (get-in db [:sections form-id :enable-end-date?] false)]
      (-> db
          (assoc-in [:sections form-id :enable-end-date?] (not enabled?))
          (assoc-in [:sections form-id :enable-occurences?] enabled?)))))

(reg-event-db :toggle-occurences
  (fn [db [_ form-id]]
    (let [enabled? (get-in db [:sections form-id :enable-occurences?] false)]
      (-> db
          (assoc-in [:sections form-id :enable-occurences?] (not enabled?))
          (assoc-in [:sections form-id :enable-end-date?] enabled?)))))

(reg-event-fx :initialize-main-page
  (fn [{:keys [db]} _]
    {:db       (assoc-in db [:sections] {:form-1 (default-form-data "Example 1")})
     :dispatch [:recalculate-dates-sequence :form-1]}))

(reg-event-db :calculate-end-date-and-dates-sequence
  (fn [db [_ form-id]]
    (let [{:keys [start-date occurences frequency]} (get-in db [:sections form-id])
          dates-sequence (if (and start-date occurences frequency)
                           (-> (utils/create-dates-of-occurence start-date occurences frequency)
                               (utils/produce-date-sequence (time.core/today))
                               vec)
                           [])]
      (update-in db [:sections form-id] merge {:end-date       (-> dates-sequence last :date)
                                               :dates-sequence dates-sequence}))))

(reg-event-db :calculate-occurences-and-dates-sequence
  (fn [db [_ form-id]]
    (let [{:keys [start-date end-date frequency]} (get-in db [:sections form-id])
          dates-sequence (if (and start-date end-date frequency)
                           (-> (utils/create-dates-during-period start-date end-date frequency)
                               (utils/produce-date-sequence (time.core/today))
                               vec)
                           [])]
      (update-in db [:sections form-id] merge {:occurences     (count dates-sequence)
                                               :dates-sequence dates-sequence}))))

(reg-event-fx :recalculate-dates-sequence
  (fn [{:keys [db]} [_ form-id]]
    (let [{:keys [start-date end-date occurences
                  frequency enable-end-date? enable-occurences?]} (get-in db [:sections form-id])]
      {:dispatch (cond
                   (and start-date end-date enable-end-date? frequency)
                   [:calculate-occurences-and-dates-sequence form-id]

                   (and start-date occurences enable-occurences? frequency)
                   [:calculate-end-date-and-dates-sequence form-id]

                   :else
                   nil)})))

(reg-event-db :add-section
  (fn [db [_ new-form-id]]
    (assoc-in db [:sections new-form-id] (default-form-data (name new-form-id)))))

(reg-event-db :toggle-section-title
  (fn [db [_ form-id]]
    (update-in db [:sections form-id :disable-title?] not)))
