(ns time-remained.views
  (:require
   [re-frame.core :as re-frame]
   [time-remained.styles :as styles]
   [time-remained.events :as events]
   [time-remained.routes :as routes]
   [time-remained.subs :as subs]
   [time-remained.main :as main]))


;; home

(defn home-panel []
  [:div
   [:h1
    {:class (styles/level1)}
    "Visualization of lifetime"]

   [main/component]
   #_[:div
      [:a {:on-click #(re-frame/dispatch [::events/navigate :about])}
       "go to About Page"]]])

(defmethod routes/panels :home-panel [] [home-panel])

;; about

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:on-click #(re-frame/dispatch [::events/navigate :home])}
     "go to Home Page"]]])

(defmethod routes/panels :about-panel [] [about-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    (routes/panels @active-panel)))
