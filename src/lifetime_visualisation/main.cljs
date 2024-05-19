(ns lifetime-visualisation.main
  (:require [re-frame.core :as rf]
            [reagent.core :as ra]
            [lifetime-visualisation.utils :as utils]
            [lifetime-visualisation.ui-components :as ui
             :refer [container row col
                     popover popover-body button]]))

(defn set-start-date-dispatch [form-id e]
  (->> e
       utils/get-value-from-event
       utils/str->date
       (conj [:set-value form-id [:start-date]])
       (rf/dispatch)))

(defn start-date [form-id]
  [:input {:class     "input-field-control"
           :type      :date
           :id        :start-date
           :name      "From date"
           :value     @(rf/subscribe [:start-date form-id])
           :on-change (partial set-start-date-dispatch form-id)}])

(defn set-end-date-dispatch [form-id e]
  (->> e
       utils/get-value-from-event
       utils/str->date
       (conj [:set-value form-id [:end-date]])
       (rf/dispatch)))

(defn end-date [form-id disabled?]
  [:input {:class     "input-field-control"
           :type      :date
           :id        :end-date
           :name      "To date"
           :value     @(rf/subscribe [:end-date form-id])
           :disabled  disabled?
           :on-change (partial set-end-date-dispatch form-id)}])

(defn set-occurences-dispatch [form-id e]
  (->> e
       utils/get-value-from-event
       int
       (conj [:set-value form-id [:occurences]])
       (rf/dispatch)))

(defn occurences [form-id disabled?]
  [:input {:class     "input-field-control"
           :type      :numeric
           :id        :occurences
           :disabled  disabled?
           :min       0
           :value     @(rf/subscribe [:value form-id [:occurences]])
           :on-change (partial set-occurences-dispatch form-id)}])

(defn set-frequency-dispatch [form-id e]
  (->> e
       utils/get-value-from-event
       keyword
       (conj [:set-value form-id [:frequency]])
       (rf/dispatch)))

(defn frequency [form-id]
  (let [menu [{:label "Weekly" :value :weekly :id :weekly}
              {:label "Fortnightly" :value :fortnightly :id :fortnightly}
              {:label "Mothly" :value :monthly :id :monthly}
              {:label "Yearly" :value :yearly :id :yearly}]]
    [:select {:class     "input-field-control"
              :name      "select-frequency"
              :id        :select-frequency
              :value     (or @(rf/subscribe [:value form-id [:frequency]]) :monthly)
              :on-change (partial set-frequency-dispatch form-id)}
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
     [:div {:id    element-id
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
      [popover-body {:class "checkbox-popover"}
       [:div (utils/->ui-date-str (:date this-date))]]]]))

(defn checkbox-sequence [dates]
  [row
   [col
    (for [d dates]
      ^{:key (str (:date d))} [render-checkbox d])]])

(defn calculate-button [form-id disabled?]
  [button {:on-click #(rf/dispatch [:recalculate-dates-sequence form-id])
           :color    "success"
           :outline  true
           :disabled disabled?}
   [:b "Calculate"]])

(defn end-date-toggle-switch [form-id]
  [:div {:class "form-switch"}
   [ui/input {:class     "custom-control-input"
              :type      "switch"
              :on-change #(rf/dispatch [:toggle-end-date form-id])
              :checked   @(rf/subscribe [:value form-id [:enable-end-date?]])}]
   [ui/label {:class "toggle-label"
              :check true}
    "To Date"]])

(defn occurences-toggle-switch [form-id]
  [:div {:class "form-switch"}
   [ui/input {:class     "custom-control-input"
              :type      "switch"
              :on-change #(rf/dispatch [:toggle-occurences form-id])
              :checked   @(rf/subscribe [:value form-id [:enable-occurences?]])}]
   [ui/label {:class "toggle-label"
              :check true}
    "Occurences"]])

(defn form-input-fields [form-id enable-end-date? enable-occurences?]
  (let [input-col-config {:class "col-6 col-md-3 mt-3"}]
    [row {:class "m-2"}
     [col input-col-config
      [row
       [:span.input-field-label "From Date"]
       [start-date form-id]]]
     [col input-col-config
      [row
       [end-date-toggle-switch form-id]
       [end-date form-id (not enable-end-date?)]]]
     [col input-col-config
      [row
       [occurences-toggle-switch form-id]
       [occurences form-id (not enable-occurences?)]]]
     [col input-col-config
      [row
       [:span.input-field-label "Frequency"]
       [frequency form-id]]]]))

(defn set-title-dispatch [form-id e]
  (->> e
       utils/get-value-from-event
       (conj [:set-value form-id [:title]])
       (rf/dispatch)))

(defn section-title-input [form-id]
  (ra/create-class
   {:reagent-render         (fn []
                              [:input {:class     "title-input-field"
                                       :type      :text
                                       :id        (str "section-title-" form-id)
                                       :value     @(rf/subscribe [:value form-id [:title]])
                                       :on-change (partial set-title-dispatch form-id)
                                       :on-blur   #(rf/dispatch [:toggle-section-title form-id])}])
    :component-did-mount    (fn [] (-> js/document
                                       (.getElementById (str "section-title-" form-id))
                                       (.focus)))}))

(defn section-title-div [current-title form-id edit-disabled?]
  (if edit-disabled?
    [:div {:class "section-title"}
     [:span current-title]
     [:i {:class "fa fa-edit"
          :style {:margin-left "1rem"
                  :margin-top "2px"}
          :on-click (fn [] (rf/dispatch [:toggle-section-title form-id]))}]]
    [section-title-input form-id]))

(defn show-hide-button [form-id]
  [button {:on-click #(rf/dispatch [:toggle-display-setting form-id])
           :color    "success m-1"
           :size     :sm
           :outline  true}
   [:b "Show/Hide"]])

(defn remove-section-button [form-id]
  [button {:color    "danger m-1"
           :outline  true
           :size     :sm
           :on-click (fn [] (rf/dispatch [:remove-section form-id]))}
   [:b "Remove"]])

(defn section-heading [form-id title disable-title?]
  [row {:class "m-1"}
   [col {:class "col-lg-9 col-md-6"
         :style {:text-align :left}}
    [section-title-div title form-id disable-title?]]
   [col {:class "col-lg-3 col-md-6"
         :style {:text-align :right}}
    [show-hide-button form-id]
    [remove-section-button form-id]]])

(defn section [form-id]
  (ra/with-let [form-data (rf/subscribe [:section-data form-id])]
    (let [{:keys [title dates-sequence
                  enable-end-date? enable-occurences?
                  disable-title? hide-section?]} @form-data]
      [:div {:class "mt-5"}
       [section-heading form-id title disable-title?]

       [ui/collapse {:is-open (not hide-section?)}
        [ui/card
         [ui/card-body
          [form-input-fields form-id enable-end-date? enable-occurences?]

          [row {:class "my-3"}
           [col {:style {:text-align :center}}
            [calculate-button form-id false]]] ;; TODO: validation here!

          (when (seq dates-sequence)
            (if (< (count dates-sequence) 2000)
              [container
               [checkbox-sequence dates-sequence]]
              (let [n-dates (take 2000 dates-sequence)]
                [container
                 [checkbox-sequence n-dates]
                 [row [col "......"]]
                 [row [col "View has reached limit"]]])))]]]

       [:hr]])))

(defn render-sections [sections]
  [:div
   (for [[section-id section-data] sections]
    ^{:key section-id} [section section-id])])

(defn add-section-button [section-count]
  (let [new-form-id (->> (inc section-count)
                         (str "form-")
                         (keyword))]
    [button {:on-click #(rf/dispatch [:add-section new-form-id])
             :color    "success"
             :outline  false}
     [:b "Add New Section"]]))

(defn component []
  (ra/with-let [_        (rf/dispatch [:initialize-main-page])
                sections (rf/subscribe [:sections])]
    [ui/container
     [:h1
      {:class "page-heading"}
      "Visualisation of Lifetime"]

     [:hr]

     [render-sections @sections]

     [:br]

     [:p {:style {:text-align :center}}
      [add-section-button (count @sections)]]]))
