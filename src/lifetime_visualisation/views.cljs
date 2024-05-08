(ns lifetime-visualisation.views
  (:require
   [re-frame.core :as re-frame]
   [lifetime-visualisation.styles :as styles]
   [lifetime-visualisation.events :as events]
   [lifetime-visualisation.routes :as routes]
   [lifetime-visualisation.subs :as subs]
   [lifetime-visualisation.main :as main]))


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
