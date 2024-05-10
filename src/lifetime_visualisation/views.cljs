(ns lifetime-visualisation.views
  (:require [lifetime-visualisation.events :as events]
            [lifetime-visualisation.main :as main]
            [lifetime-visualisation.routes :as routes]
            [lifetime-visualisation.subs :as subs]
            [lifetime-visualisation.ui-components :as ui]
            [re-frame.core :as re-frame]))


;; home

(defn home-panel []
  [ui/container
   [:h1
    {:class "page-heading"}
    "Visualization of Lifetime"]

   [:hr]

   [main/component]
   #_[:div
      [:a {:on-click #(re-frame/dispatch [::events/navigate :about])}
       "go to About Page"]]])

(defmethod routes/panels :home-panel [] [home-panel])
;; FIXME: This is a hack to make the app display home-panel
;; even when the route is not found
(defmethod routes/panels :default [] [home-panel])

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
