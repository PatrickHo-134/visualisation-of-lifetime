(ns time-remained.utils
  (:require [cljs-time.coerce :as time.coerce]
            [cljs-time.core :as time.core]
            [cljs-time.format :as time.format]))

(defn uuid-str []
  (str (cljs.core/random-uuid)))

(defmulti get-next-date (fn [frequency date] frequency))

(defmethod get-next-date :weekly [_ date-inst]
  (-> date-inst
      (time.coerce/to-date-time)
      (time.core/plus (time.core/days 7))
      (time.coerce/to-date)))

(defmethod get-next-date :fortnightly [_ date-inst]
  (-> date-inst
      (time.coerce/to-date-time)
      (time.core/plus (time.core/days 14))
      (time.coerce/to-date)))

(defmethod get-next-date :monthly [_ date-inst]
  (-> date-inst
      (time.coerce/to-date-time)
      (time.core/plus (time.core/months 1))
      (time.coerce/to-date)))

(defmethod get-next-date :yearly [_ date-inst]
  (-> date-inst
      (time.coerce/to-date-time)
      (time.core/plus (time.core/years 1))
      (time.coerce/to-date)))

(defn create-dates-during-period [start end frequency]
  (assert (#{:weekly :fortnightly :monthly :yearly} frequency))
  (loop [start-date start
         end-date   end
         results    []]
    (if (time.core/after? (time.coerce/to-date-time start-date)
                          (time.coerce/to-date-time end-date))
      results
      (recur (get-next-date frequency start-date)
             end-date
             (conj results start-date)))))

(defn create-dates-of-occurence [start occurences frequency]
  (loop [this-date  start
         occurences occurences
         results    []]
    (if (zero? occurences)
      results
      (recur (get-next-date frequency this-date)
             (dec occurences)
             (conj results this-date)))))

(defn passed-today? [this-date today]
  (time.core/after? (time.coerce/to-date-time this-date)
                    (time.coerce/to-date-time today)))

(defn produce-date-sequence [date-coll today]
  (for [d    date-coll
        :let [in-future? (passed-today? d today)]]
    {:date       d
     :in-future? in-future?}))

(defn get-value-from-event [e]
  (-> e .-target .-value))

(defn str->date [date-str]
  (-> date-str
      (time.coerce/to-date-time)
      (time.coerce/to-date)))

(def iso-date (time.format/formatters :date))
(def ui-date (time.format/formatter "dd/MM/yyyy"))

(defn date->iso-date-str [d]
  (->> d
       (time.coerce/to-date-time)
       (time.core/to-default-time-zone)
       (time.format/unparse iso-date)))

(defn ->ui-date-str [date]
  (->> (time.coerce/to-date-time date)
       (time.format/unparse ui-date)))
