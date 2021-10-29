(ns advertisers.model
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [advertisers.location :as loc]))

(def advertisers-uri
  "https://5b87a97d35589600143c1424.mockapi.io/api/v1/advertisers")

(rf/reg-event-fx
 ::init
 [(rf/inject-cofx ::loc/location)]
 (fn [{db :db l :location :as fx} _]
   {:db (assoc db :location l)
    :dispatch-n [[::fetch-advertisers]]}))

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

(rf/reg-sub
 ::sort
 (fn [db _]
   (let [sort-field (get-in db [:location :search-parsed "sort"])
         sort-order (get-in db [:location :search-parsed "order"])]
     [sort-field sort-order])))


(rf/reg-sub
 ::advertiser-items
 (fn [db _]
   (map adv-xf (get-in db [:advertisers :items]))))


(defn sort-adv [[field order] items]
  (sort-by (keyword field) items))


(rf/reg-sub
 ::advertisers
 :<- [::advertiser-items]
 :<- [::sort]
 (fn [[items sort-params] _]
   (if (empty? sort-params)
     items
     (sort-adv sort-params items))))

(def order-change-map
  {nil :desc
   :desc :asc
   :asc nil})

#_(rf/reg-event-fx
 ::sort
 (fn [{db :db} [_ field-name]]
   {:}))
