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
  [:input {:class     "input-field"
           :type      :date
           :id        :start-date
           :name      "From date"
           :value     @(rf/subscribe [:start-date])
           :on-change set-start-date-dispatch}])

(defn set-end-date-dispatch [e]
  (->> e
       utils/get-value-from-event
       utils/str->date
       (conj [:set-value [:end-date]])
       (rf/dispatch)))

(defn end-date [disabled?]
  [:input {:class     "input-field"
           :type      :date
           :id        :end-date
           :name      "To date"
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
  [:input {:class     "input-field"
           :type      :numeric
           :id        :occurences
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
    [:select {:class     "input-field"
              :name      "select-frequency"
              :id        :select-frequency
              :value     (or @(rf/subscribe [:value [:frequency]]) :monthly)
              :on-change set-frequency-dispatch}
     (for [{:keys [label id value]} menu]
       [:option {:key   (str "item-" id)
                 :value value}
        label])]))

(defn square-check-icon []
  [:i {:class "fa fa-check-square fa-fw custom-icon"}])

(defn square-icon []
  [:i {:class "fa fa-square-o fa-fw custom-icon"}])

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
              :on-change #(rf/dispatch [:toggle-end-date])
              :checked  @(rf/subscribe [:value [:enable-end-date?]])}]
   [ui/label {:class "toggle-label"
              :check true}
    "To Date"]])

(defn occurences-toggle-switch []
  [form {:class "form-switch"}
   [ui/input {:type     "switch"
              :on-change #(rf/dispatch [:toggle-occurences])
              :checked  @(rf/subscribe [:value [:enable-occurences?]])}]
   [ui/label {:class "toggle-label"
              :check true}
    "Occurences"]])

(defn component []
  (ra/with-let [form-data (rf/subscribe [:form])
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
