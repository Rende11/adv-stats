(ns advertisers.layout
  (:require [advertisers.model :as model]
            [re-frame.core :as rf]
            [clojure.string :as str]))

(def styles
  {:adv-wrapper {:background-color "#222222"}})


(def top-arrow
  [:svg {:xmlns "http://www.w3.org/2000/svg"
         :class "h-6 w-6"
         :fill "none"
         :viewBox "0 0 24 24"
         :stroke "currentColor"}
   [:path {:strokeLinecap "round"
           :strokeLinejoin "round"
           :strokeWidth "{2}"
           :d "M8 7l4-4m0 0l4 4m-4-4v18"}]])

(def down-arrow
  [:svg {:xmlns "http://www.w3.org/2000/svg"
         :class "h-6 w-6"
         :fill "none"
         :viewBox "0 0 24 24"
         :stroke "currentColor"}
   [:path {:strokeLinecap "round"
           :strokeLinejoin "round"
           :strokeWidth "{2}"
           :d "M16 17l-4 4m0 0l-4-4m4 4V3"}]])

(defn sort-arrow []
  (when-let [order @(rf/subscribe [::model/sort-order])]
    (case order
      "asc" top-arrow
      "desc" down-arrow)))

(def place-holder
  [:span.h-6.w-6])

(defn sort-view [curr-field]
  (let [field @(rf/subscribe [::model/sort-field])]
    (if (= curr-field field)
      [sort-arrow]
      place-holder)))


(defn view []
  [:div.adv-wrapper.mx-auto.mt-8.p-5.border.border-gray-500 {:class "w-4/5"
                                                             :style (:adv-wrapper styles)}
   [:div.adv-headline
    [:div.adv-title.text-lg "Overview of Advertisers"]
    [:hr.mt-2.border-gray-500]]
   [:div.adv-grid.border.border-gray-500.mt-6
    [:div.adv-grid-header.flex.flex-row.border-b.border-gray-500.px-3.py-4
     [:div.font-bold.cursor-pointer.flex
      {:class "w-1/4"
       :on-click #(rf/dispatch [::model/sort "name"])}
      [:span "ADVERTISER"]
      [sort-view "name"]]

     [:div.font-bold.text-center.cursor-pointer.flex
      {:class "w-1/6"
       :on-click #(rf/dispatch [::model/sort "createdAt"])}
      [:span "CREATION DATE"]
      [sort-view "createdAt"]]

     [:div.font-bold.text-center.cursor-pointer.flex.justify-center
      {:class "w-1/6"
       :on-click #(rf/dispatch [::model/sort "camp"])}
      place-holder
      [:span "# CAMPAIGNS"]
      [sort-view "camp"]]

     [:div.font-bold.text-center.cursor-pointer.flex.justify-center
      {:class "w-1/6"
       :on-click #(rf/dispatch [::model/sort "impressions"])}
      place-holder
      [:span "IMPRESSIONS"]
      [sort-view "impressions"]]

     [:div.font-bold.text-center.cursor-pointer.flex.justify-center
      {:class "w-1/6"
       :on-click #(rf/dispatch [::model/sort "clicks"])}
      place-holder
      [:span "CLICKS"]
      [sort-view "clicks"]]]

    (case @(rf/subscribe [::model/adv-state])
      :error [:div.p-3.text-sm "Could not load advertisers"]
      :loading [:div.p-3.text-sm "Loading..."]
      (doall
       (for [{:keys [id name date camp impressions clicks]} @(rf/subscribe [::model/advertisers])] 
         [:div.adv-item.flex.flex-row.border-b.border-gray-500.py-3.px-3.cursor-pointer {:key id}
          [:div.adv-name.text-sm {:class "w-1/4"} name]
          [:div.adv-date.text-sm {:class "w-1/6"} date]
          [:div.adv-camp.text-sm.text-center {:class "w-1/6"} camp]
          [:div.adv-camp.text-sm.text-center {:class "w-1/6"} (or impressions "n/a")]
          [:div.adv-camp.text-sm.text-center {:class "w-1/6"} (or clicks "n/a")]])))]])
