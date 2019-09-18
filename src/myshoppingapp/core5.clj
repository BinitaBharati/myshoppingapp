(ns myshoppingapp.core
  (:gen-class)
  (:require ;[myshoppingapp.dbutil :as dbutil]
            ;[org.httpkit.server :as httpkitserver]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring.coercion :as coercion]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.ring.middleware.dev :as dev]
            [reitit.ring.spec :as spec]
            [spec-tools.spell :as spell]
            [ring.adapter.jetty :as jetty]
            [muuntaja.core :as m]
            [clojure.java.io :as io] 
            [ring.middleware.session :as session]
            [clostache.parser.render-resource :as render-resource]
            ))
;https://github.com/ring-clojure/ring/wiki/Sessions
;https://github.com/metosin/reitit/issues/205
(defn handler [request]
  (println "home handler : entered with "request)
  (let [session (get request :session)
        user (:user session "None")]
    (println "done!!")
    {:status 200
         :headers {"Content-Type" "text/html"}
         :body (io/input-stream (io/resource "public/home.html"))
         ;:body   (io/file "resources/public/home.html")
         ;:body   "huhu"
    }
  )
  
)

(def app
  (ring/ring-handler
    (ring/router
      [
        ["/home" {:name ::home
                 :get handler
                 :post handler}]
       ["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "my-api"
                                :description "with reitit-ring"}}
               :handler (swagger/create-swagger-handler)}}]
       ["/files"
        {:swagger {:tags ["files"]}}

        ["/upload"
         {:post {:summary "upload a file"
                 :parameters {:multipart {:file multipart/temp-file-part}}
                 :responses {200 {:body {:name string?, :size int?}}}
                 :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                            {:status 200
                             :body {:name (:filename file)
                                    :size (:size file)}})}}]

        ["/download"
         {:get {:summary "downloads a file"
                :swagger {:produces ["image/png"]}
                :handler (fn [_]
                           {:status 200
                            :headers {"Content-Type" "image/png"}
                            :body (-> "reitit.png"
                                      (io/resource)
                                      (io/input-stream))})}}]]

       ["/math"
        {:swagger {:tags ["math"]}}

        ["/plus"
         {:get {:summary "plus with spec query parameters"
                :parameters {:query {:x int?, :y int?}}
                :responses {200 {:body {:total int?}}}
                :handler (fn [{{{:keys [x y]} :query} :parameters}]
                           {:status 200
                            :body {:total (+ x y)}})}
          :post {:summary "plus with spec body parameters"
                 :parameters {:body {:x int?, :y int?}}
                 :responses {200 {:body {:total int?}}}
                 :handler (fn [{{{:keys [x y]} :body} :parameters}]
                            {:status 200
                             :body {:total (+ x y)}})}}]]
       ;;Ref - https://metosin.github.io/reitit/ring/static.html
       ["/assets/*" (ring/create-resource-handler)]]

      {;;:reitit.middleware/transform dev/print-request-diffs ;; pretty diffs
       ;;:validate spec/validate ;; enable spec validation for route data
       ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
       :exception pretty/exception
       :data {:coercion reitit.coercion.spec/coercion
              :muuntaja m/instance
              :middleware [;; swagger feature
                           swagger/swagger-feature
                           ;; query-params & form-params
                           parameters/parameters-middleware
                           ;; content-negotiation
                           muuntaja/format-negotiate-middleware
                           ;; encoding response body
                           muuntaja/format-response-middleware
                           ;; exception handling
                           exception/exception-middleware
                           ;; decoding request body
                           muuntaja/format-request-middleware
                           ;; coercing response bodys
                           coercion/coerce-response-middleware
                           ;; coercing request parameters
                           coercion/coerce-request-middleware
                           ;; multipart
                           multipart/multipart-middleware]}})
    (ring/routes
      (swagger-ui/create-swagger-ui-handler
        {:path "/"
         :config {:validatorUrl nil
                  :operationsSorter "alpha"}})
      (ring/create-default-handler))
      {:middleware [session/wrap-session]}))

(defn -main
  "This is invoked at the start of the REPL. This will do all pre-processing required
  for this app, like creating DB tables etc."
  [& args]
  (println "Initializing application...")
  ;(dbutil/init-db-main nil)
  ;(httpkitserver/run-server #'app {:host "192.168.10.12" :port 3000})
  (jetty/run-jetty #'app {:host "192.168.10.12" :port 3000})
  )

(def app
  (ring/ring-handler
    (ring/router
      [["/loginsubmit" {:post 
                             {:summary "Login user"
                              :parameters {:body 
                                             {:inputEmail string?
                                              :inputPassword string?
                                              }
                                           }
                              :handler loginsubmit
                              }
      
                          }]
       ["/assets/*" (ring/create-resource-handler)]])
      (ring/create-default-handler)
      {:middleware [session/wrap-session]}))
