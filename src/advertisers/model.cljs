(ns advertisers.model
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [advertisers.location :as loc]))

(def advertisers-uri
  "https://5b87a97d35589600143c1424.mockapi.io/api/v1/advertisers")

(def advertisers-stats-uri
  "https://5b87a97d35589600143c1424.mockapi.io/api/v1/advertiser-statistics")


(rf/reg-event-fx
 ::init
 [(rf/inject-cofx ::loc/location)]
 (fn [{db :db l :location :as fx} _]
   {:db (-> db
            (assoc :location l)
            (assoc-in [:sort :order] (get-in l [:search-parsed "order"]))
            (assoc-in [:sort :field] (get-in l [:search-parsed "sort"])))
    :dispatch-n [[::fetch-advertisers]
                 [::fetch-advertisers-stats]]}))


(rf/reg-event-fx
 ::fetch-advertisers
 (fn [{db :db} _]
   {:http-xhrio {:method          :get
                 :uri             advertisers-uri
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::load-advertisers-success]
                 :on-failure      [::load-advertisers-fail]}
    :db (-> db
            (assoc-in [:advertisers :state] :loading)
            (update :advertisers dissoc :error))}))

(rf/reg-event-fx
 ::load-advertisers-success
 (fn [{db :db} [_ body]]
   {:db (-> db
          (assoc-in [:advertisers :items] body)
          (assoc-in [:advertisers :state] :done))}))

(rf/reg-event-fx
 ::load-advertisers-fail
 (fn [{db :db} [_ body]]
   {:db (-> db
          (assoc-in [:advertisers :error] body)
          (assoc-in [:advertisers :state] :error))}))

(defn adv-xf [item]
  (assoc item
         :camp (count (:campaignIds item))
         :date (.toLocaleString (js/Date. (:createdAt item)) "nl" #js {"year"   "numeric"
                                                                       "month"  "2-digit"
                                                                       "day"    "2-digit"
                                                                       "hour"   "2-digit"
                                                                       "minute" "2-digit" })))


(rf/reg-event-fx
 ::fetch-advertisers-stats
 (fn [{db :db} _]
   {:http-xhrio {:method          :get
                 :uri             advertisers-stats-uri
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::load-advertisers-stats-success]
                 :on-failure      [::load-advertisers-stats-fail]}
    :db (-> db
          (assoc-in [:advertisers-stats :state] :loading)
          (update-in [:advertisers-stats] dissoc :error))}))

(rf/reg-event-fx
 ::load-advertisers-stats-success
 (fn [{db :db} [_ body]]
   {:db (-> db
          (assoc-in [:advertisers-stats :items] body)
          (assoc-in [:advertisers-stats :state] :done))}))

(rf/reg-event-fx
 ::load-advertisers-stats-fail
 (fn [{db :db} [_ body]]
   {:db (-> db
          (assoc-in [:advertisers-stats :error] body)
          (assoc-in [:advertisers-stats :state] :error))}))


(rf/reg-sub
 ::adv-state
 (fn [db _]
   (get-in db [:advertisers :state])))

(rf/reg-sub
 ::sort-field
 (fn [db _]
   (get-in db [:sort :field])))

(rf/reg-sub
 ::sort-order
 (fn [db _]
   (get-in db [:sort :order])))

(rf/reg-sub
 ::advertiser-items
 (fn [db _]
   (map adv-xf (get-in db [:advertisers :items]))))

(rf/reg-sub
 ::advertiser-stat-items
 (fn [db _]
   (group-by :advertiserId (get-in db [:advertisers-stats :items]))))


(rf/reg-sub
 ::advertiser-data
 :<- [::advertiser-items]
 :<- [::advertiser-stat-items]
 (fn [[items stats] _]
   (map (fn [{id :id :as item}]
          (let [st (first (get stats id))]
            (assoc item :impressions (:impressions st) :clicks (:clicks st)))) items)))


(defn sort-adv [field order items]
  (sort-by (keyword field) (if (= "desc" order) > <) items))


(rf/reg-sub
 ::advertisers
 :<- [::advertiser-data]
 :<- [::sort-field]
 :<- [::sort-order]
 (fn [[items sort-field sort-order] _]
   (if (or sort-field sort-order)
     (sort-adv sort-field sort-order items)
     items)))

(def order-change-map
  {nil    "desc"
   "desc" "asc"
   "asc"  nil})

(rf/reg-event-fx
 ::sort
 (fn [{db :db} [_ field-name]]
   (let [curr-order (get-in db [:sort :order])
         curr-field (get-in db [:sort :field])
         sort-order (if (= field-name curr-field)
                      (get order-change-map curr-order)
                      "desc")]
     (if sort-order
       {:dispatch [::loc/redirect-merge {"sort"  field-name
                                         "order" sort-order}]
        :db (-> db
              (assoc-in [:sort :field] field-name)
              (assoc-in [:sort :order] sort-order))}

       {:dispatch [::loc/redirect {}]
        :db (-> db
              (assoc-in [:sort :field] nil)
              (assoc-in [:sort :order] nil))}))))
