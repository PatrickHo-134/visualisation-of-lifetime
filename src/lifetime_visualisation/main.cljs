(ns lifetime-visualisation.main
  (:require [re-frame.core :as rf]
            [reagent.core :as ra]
            [lifetime-visualisation.utils :as utils]
            [lifetime-visualisation.ui-components :as ui
             :refer [container row col form-group popover popover-body]]))

(defn set-start-date-dispatch [e]
  (->> e
       utils/get-value-from-event
       utils/str->date
       (conj [:set-start-date])
       (rf/dispatch)))

(defn start-date []
  [:input {:type      :date
           :id        :start-date
           :name      "From date"
           :style     {:height "2rem"}
           :value     @(rf/subscribe [:start-date])
           :on-change set-start-date-dispatch}])

(defn set-end-date-dispatch [e]
  (->> e
       utils/get-value-from-event
       utils/str->date
       (conj [:set-end-date])
       (rf/dispatch)))

(defn end-date []
  [:input {:type      :date
           :id        :end-date
           :name      "To date"
           :style     {:height "2rem"}
           :value     @(rf/subscribe [:end-date])
           :on-change set-end-date-dispatch}])

(defn set-occurences-dispatch [e]
  (->> e
       utils/get-value-from-event
       int
       (conj [:set-occurences])
       (rf/dispatch)))

(defn occurences []
  [:input {:type :numeric
           :id :occurences
           :style {:height "2rem"}
           :min 0
           :value @(rf/subscribe [:value [:occurences]])
           :on-change set-occurences-dispatch}])

(defn set-frequency-dispatch [e]
  (->> e
       utils/get-value-from-event
       keyword
       (conj [:set-frequency])
       (rf/dispatch)))

(defn frequency []
  (let [menu [{:label "Weekly" :value :weekly :id :weekly}
              {:label "Fortnightly" :value :fortnightly :id :fortnightly}
              {:label "Mothly" :value :monthly :id :monthly}
              {:label "Yearly" :value :yearly :id :yearly}]]
    [:select {:name "select-frequency"
              :id :select-frequency
              :style {:height "2rem"}
              :on-change set-frequency-dispatch}
     (for [{:keys [label id value]} menu]
       [:option {:key   (str "item-" id)
                 :value value}
        label])]))

(defn square-check-icon []
  [:i {:class "fa fa-check-square fa-fw"
       :style {:font-size "15px"}}])

(defn square-icon []
  [:i {:class "fa fa-square-o fa-fw"
       :style {:font-size "15px"}}])

(defn render-checkbox [{:keys [in-future?] :as this-date}]
  (ra/with-let [show-popover? (ra/atom false)
                element-id    (str "popover-" (utils/uuid-str))]
    [:<>
     [:div {:id element-id
            :style {:display :inline}}
      (if in-future?
        [square-icon]
        [square-check-icon])]
     [popover {:trigger   :hover
               :placement :top
               :target    element-id
               :toggle    (fn [_]
                            (ra/rswap! show-popover? not))
               :isOpen   @show-popover?}
      [popover-body {:style {:background-color "yellow"}}
       [:div (utils/->ui-date-str (:date this-date))]]]]))

(defn checkbox-sequence [dates]
  [row
   [col
    (for [d dates]
      ^{:key (str (:date d))} [render-checkbox d])]])

(defn component []
  (ra/with-let [dates (rf/subscribe [:dates-sequence])
                _     (rf/dispatch [:initialize-form])]
    [container
     [form-group
      [row
       [col {:class "col-3"}
        [row "From Date:" [start-date]]]
       [col {:class "col-3"}
        [row "To Date:" [end-date]]]
       [col {:class "col-3"}
        [row "Occurences:" [occurences]]]
       [col {:class "col-3"}
        [row "Frequency:" [frequency]]]]]
     (when (seq @dates)
       (if (< (count @dates) 2000)
         [container
          [checkbox-sequence @dates]]
         (let [n-dates (take 2000 @dates)]
           [container
            [checkbox-sequence n-dates]
            [row [col "......"]]
            [row [col "View has reached limit"]]])))]))
