(ns lifetime-visualisation.main
  (:require [re-frame.core :as rf]
            [reagent.core :as ra]
            [lifetime-visualisation.utils :as utils]
            [lifetime-visualisation.ui-components :as ui
             :refer [container row col form form-group
                     popover popover-body button]]))

(defn set-start-date-dispatch [e]
  (->> e
       utils/get-value-from-event
       utils/str->date
       (conj [:set-value [:start-date]])
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
       (conj [:set-value [:end-date]])
       (rf/dispatch)))

(defn end-date [disabled?]
  [:input {:type      :date
           :id        :end-date
           :name      "To date"
           :style     {:height "2rem"}
           :value     @(rf/subscribe [:end-date])
           :disabled  disabled?
           :on-change set-end-date-dispatch}])

(defn set-occurences-dispatch [e]
  (->> e
       utils/get-value-from-event
       int
       (conj [:set-value [:occurences]])
       (rf/dispatch)))

(defn occurences [disabled?]
  [:input {:type      :numeric
           :id        :occurences
           :style     {:height "2rem"}
           :disabled  disabled?
           :min       0
           :value     @(rf/subscribe [:value [:occurences]])
           :on-change set-occurences-dispatch}])

(defn set-frequency-dispatch [e]
  (->> e
       utils/get-value-from-event
       keyword
       (conj [:set-value [:frequency]])
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

(defn calculate-button [disabled?]
  [button {:on-click #(rf/dispatch [:recalculate-dates-sequence])
           :color    "success"
           :outline  false
           :disabled disabled?}
   "Calculate"])

(defn end-date-toggle-switch []
  [form {:class "form-switch"}
   [ui/input {:type     "switch"
              :on-click #(rf/dispatch [:toggle-end-date])
              :checked  @(rf/subscribe [:value [:enable-end-date?]])
              :outline  true}]
   [ui/label {:check true
              :style {:margin-left "5px"}} "To Date"]])

(defn occurences-toggle-switch []
  [form {:class "form-switch"}
   [ui/input {:type     "switch"
              :on-click #(rf/dispatch [:toggle-occurences])
              :checked  @(rf/subscribe [:value [:enable-occurences?]])
              :outline  true}]
   [ui/label {:check true
              :style {:margin-left "5px"}} "Occurences"]])

(defn component []
  (ra/with-let [form-data (rf/subscribe [:form])
                dates (rf/subscribe [:value [:dates-sequence]])
                _     (rf/dispatch [:initialize-form])]
    (let [{:keys [dates-sequence enable-end-date? enable-occurences?]} @form-data]
      [container
       [form-group
        [row
         [col {:class "col-3"}
          [row "From Date" [start-date]]]
         [col {:class "col-3"}
          [row [end-date-toggle-switch] [end-date (not enable-end-date?)]]]
         [col {:class "col-3"}
          [row [occurences-toggle-switch] [occurences (not enable-occurences?)]]]
         [col {:class "col-3"}
          [row "Frequency" [frequency]]]]]
       [row {:class "mb-2"}
        [col {:style {:text-align :center}}
         [calculate-button false]]] ;; TODO: validation here!
       (when (seq dates-sequence)
         (if (< (count dates-sequence) 2000)
           [container
            [checkbox-sequence dates-sequence]]
           (let [n-dates (take 2000 dates-sequence)]
             [container
              [checkbox-sequence n-dates]
              [row [col "......"]]
              [row [col "View has reached limit"]]])))])))
