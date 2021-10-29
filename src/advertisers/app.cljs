(ns advertisers.app
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [advertisers.model :as model]
            [advertisers.layout :as layout]))


(defn app []
  [:div#app
   [layout/view]])

(defn render []
  (rdom/render [app] (.getElementById js/document "root")))

(defn ^:export ^:dev/after-load init []
  (rf/dispatch-sync [::model/init])
  (render))
