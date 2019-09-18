(ns myshoppingapp.core
  (:gen-class)
  (:require [myshoppingapp.dbutil :as dbutil]
            ;[org.httpkit.server :as httpkitserver]
            [ring.adapter.jetty :as jetty]
            [reitit.ring :as rr]
            [reitit.coercion.spec :as rcs]
            [reitit.ring.coercion :as rrc]          
            ))

(def app
  (rr/ring-handler
    (rr/router
      ["/api"
       ["/math" {:get {:parameters {:query {:x int?, :y int?}}
                       :responses {200 {:body {:total pos-int?}}}
                       :handler (fn [{{{:keys [x y]} :query} :parameters}]
                                  (println "entered !!!!!")
                                  {:status 200
                                   :body {:total (+ x y)}})}}]]
      ;; router data effecting all routes
      {:data {:coercion rcs/coercion
              :middleware [rrc/coerce-exceptions-middleware
                           rrc/coerce-request-middleware
                           rrc/coerce-response-middleware]}})))

(defn -main
  "This is invoked at the start of the REPL. This will do all pre-processing required
  for this app, like creating DB tables etc."
  [& args]
  (println "Initializing application...")
  (dbutil/init-db-main nil)
  ;(httpkitserver/run-server #'app {:host "192.168.10.12" :port 3000})
  (jetty/run-jetty #'app {:host "192.168.10.12" :port 3000})
  )
