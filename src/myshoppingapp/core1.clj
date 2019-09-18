(ns myshoppingapp.core
  (:gen-class)
  (:require [myshoppingapp.dbutil :as dbutil]
            [calfpath.route :as r]
            ;[ring.adapter.jetty :as ringadapterjetty]
            [org.httpkit.server :as httpkitserver]
            [ring.middleware.resource :as ringmidwarersrc]
            [ring.middleware.file :as ringmidwarefile]
            [ring.middleware.content-type :as ringmidwarecontype]
            [ring.middleware.not-modified :as ringmidwarenotmod]
            [ring.util.response :as ringres]
            [ring.util.request :as ringreq]
            [ring.util.codec :as codec]
            [ring.middleware.head :as head]))

(defn defaultHandler [request]
  (println "defaultHandler: entered")
  {:status 404 :body "No such file"})

(defn test-resource-request
  "If request matches a static resource, returns it in a response map.
  Otherwise returns nil. See wrap-resource for the available options."
  {:added "1.2"}
  ([request root-path]
   (test-resource-request request root-path {}))
  ([request root-path options]
    (println "test-resource-request: ENTERED" )
   (if (#{:head :get} (:request-method request))
     (let [path (subs (codec/url-decode (ringreq/path-info request)) 1)]
       (-> (ringres/resource-response path (assoc options :root root-path))
           (head/head-response request))))))

(defn test-wrap-resource-prefer-resources [handler root-path options]
  (println "test-wrap-resource-prefer-resources: ENTERED!") 
  (fn
    ([request]
     (println "test-wrap-resource-prefer-resources: ENTERED111!") 
     (or (test-resource-request request root-path options)
         (handler request)))
    ([request respond raise]
     (if-let [response (test-resource-request request root-path options)]
       (respond response)
       (handler request respond raise)))))


(defn test-wrap-resource
  "Middleware that first checks to see whether the request map matches a static
  resource. If it does, the resource is returned in a response map, otherwise
  the request map is passed onto the handler. The root-path argument will be
  added to the beginning of the resource path.
  Accepts the following options:
  :loader          - resolve the resource using this class loader
  :allow-symlinks? - allow symlinks that lead to paths outside the root
                     classpath directories (defaults to false)
  :prefer-handler? - prioritize handler response over resources (defaults to
                     false)"
  ([handler root-path]
   (test-wrap-resource handler root-path {}))
  ([handler root-path options]
    (test-wrap-resource-prefer-resources handler root-path options)
))

(defn home [request]
  (ringmidwarersrc/wrap-resource defaultHandler "public")  
)

(defn wrap-static [classpath-folder]
  (-> (fn [_] {:status 400 :body "No such file"})      ; static files serving example
    ;; the following require Ring dependency in your project
    (ringmidwarersrc/wrap-resource classpath-folder)  ; render files from classpath
    ;(ringmidwarefile/wrap-file "/var/www/public") ; render files from filesystem
    (ringmidwarecontype/wrap-content-type)
    (ringmidwarenotmod/wrap-not-modified)))


(defn handle-request [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})


(defn app-routes
  "Return a vector of route specs."
  []
  [;; first route has a partial URI match,implied by a trailing '*'
   {:uri "/home" :method :get :handler (wrap-static "public")}
   {:uri "/users/:user-id*" :nested [{:uri "/jobs/"        :nested [{:method :get  :handler handle-request}
                                                                    {:method :post :handler handle-request}]}
                                     {:uri "/permissions/" :method :get :handler handle-request}]}
   {:uri "/orders/:order-id/confirm/" :method :post :handler handle-request}        ; :uri is lifted over :method
   {:uri "/health/"  :handler handle-request}
   {:uri "/static/*" :handler (-> (fn [_] {:status 400 :body "No such file"})      ; static files serving example
                                ;; the following require Ring dependency in your project
                                (ringmidwarersrc/wrap-resource "public")  ; render files from classpath
                                ;(ringmidwarefile/wrap-file "/var/www/public") ; render files from filesystem
                                (ringmidwarecontype/wrap-content-type)
                                (ringmidwarenotmod/wrap-not-modified)
                                )}])

(defn make-handler
  []
  (-> (app-routes)   ; return routes vector
    r/compile-routes ; turn every map into a route by populating matchers in them
    r/make-dispatcher))

(comment (defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"}) This function is commented!) 

(defn -main
  "This is invoked at the start of the REPL. This will do all pre-processing required
  for this app, like creating DB tables etc."
  [& args]
  (println "Initializing application...")
  (dbutil/init-db-main nil)
  (httpkitserver/run-server (make-handler) {:host "192.168.10.12" :port 3000})
  )
