(ns advertisers.layout
  (:require [advertisers.model :as model]
            [re-frame.core :as rf]
            [clojure.string :as str]))

(def styles
  {:adv-wrapper {:background-color "#222222"}})


(defn view []
  [:div.adv-wrapper.mx-auto.mt-8.p-5.border.border-gray-500 {:class "w-4/5"
                                                             :style (:adv-wrapper styles)}
   [:div.adv-headline
    [:div.adv-title.text-lg "Overview of Advertisers"]
    [:hr.mt-2.border-gray-500]]
   [:div.adv-grid.border.border-gray-500.mt-6
    [:div.adv-grid-header.flex.flex-row.border-b.border-gray-500.px-3.py-4
     [:div.font-bold.cursor-pointer {:class "w-1/4"
                                     :on-click #(rf/dispatch [::model/sort "name"])} "ADVERTISER"]
     [:div.font-bold {:class "w-1/6"} "CREATION DATE"]
     [:div.font-bold.text-center {:class "w-1/6"} "# CAMPAIGNS"]]
    (doall
     (for [{:keys [id name date-display camp-display]} @(rf/subscribe [::model/advertisers])] 
       [:div.adv-item.flex.flex-row.border-b.border-gray-500.py-3.px-3.cursor-pointer {:key id}
        [:div.adv-name.text-sm {:class "w-1/4"} name]
        [:div.adv-date.text-sm {:class "w-1/6"} date-display]
        [:div.adv-camp.text-sm.text-center {:class "w-1/6"} camp-display]]))]])
