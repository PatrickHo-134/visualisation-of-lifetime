(ns time-remained.main
  (:require ["reactstrap" :as rs]
            [cljs-time.core :as time.core]
            [re-frame.core :as rf]
            [reagent.core :as ra]
            [time-remained.utils :as utils]))

(def row (ra/adapt-react-class rs/Row))
(def col (ra/adapt-react-class rs/Col))
(def dropdown-menu (ra/adapt-react-class rs/DropdownMenu))
(def dropdown-item (ra/adapt-react-class rs/DropdownItem))
(def container (ra/adapt-react-class rs/Container))
(def popover (ra/adapt-react-class rs/Popover))
(def popover-body (ra/adapt-react-class rs/PopoverBody))
(def popover-header (ra/adapt-react-class rs/PopoverHeader))

(def form-group (ra/adapt-react-class rs/FormGroup))

; re-frame events
(rf/reg-event-db
  :init
  (fn [_ _]
    {:doc {}}))

(rf/reg-sub
  :form
  (fn [db _]
    (:form db)))

(rf/reg-sub
  :start-date
  (fn [db _]
    (when-let [d (get-in db [:form :start-date])]
      (utils/date->iso-date-str d))))

(rf/reg-sub
  :end-date
  (fn [db _]
    (when-let [d (get-in db [:form :end-date])]
      (utils/date->iso-date-str d))))

(rf/reg-sub
  :dates-sequence
  (fn [db _]
    (let [{:keys [start-date end-date frequency]} (get-in db [:form])]
      (if (and start-date end-date frequency)
        (-> (utils/create-dates-during-period start-date end-date frequency)
            (utils/produce-date-sequence (time.core/today)))
        []))))

(rf/reg-sub
  :value
  :<- [:form]
  (fn [doc [_ path]]
    (get-in doc path)))

(rf/reg-event-db
  :set-value
  (fn [db [_ path value]]
    (assoc-in db (into [:form] path) value)))

(rf/reg-event-db
  :update-value
  (fn [db [_ f path value]]
    (update-in db (into [:form] path) f value)))

(rf/reg-event-db :initialize-form
  (fn [db _]
    (assoc-in db [:form] {:start-date (utils/str->date "1993-04-13")
                          :end-date   (utils/str->date "2073-04-13")
                          :frequency  :monthly})))

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
       (conj [:set-value [:occurences]])
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

(defn form []
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

(defn component []
  [form])