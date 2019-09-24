(ns myshoppingapp.core
  (:gen-class)
  (:require [myshoppingapp.dbutil :as dbutil]
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
            [ring.middleware.cookies :as cookies]
            [ring.middleware.json :as json]
            [clostache.parser :as cp]
            ))
;https://github.com/ring-clojure/ring/wiki/Sessions
;https://github.com/metosin/reitit/issues/205
(defn home [request]
  (println "home handler : entered with "request)
  (let [session (get request :session)
        user (:user session "None")]
    (println "doneeeeeeeeee!!")
    {    :status 200
         :headers {"Content-Type" "text/html"}
         :body (io/input-stream (io/resource "public/home1.html"))
         :session (assoc session :user user)
    }
  )
  
)

(defn constructCategoryListHtml [input]
  (println "constructCategoryListHtml: entered with "input)
  (let [categoryHtmlFixedPart1 "<div class='col-lg-4 col-md-6 mb-4'>
                                   <div class='card h-100'>
			                                <div class='card-body'>
                                         <a href='#'><img class='card-img-top' src='assets/images/"
        categoryHtmlFixedPart2 "' alt=''>
                                  </a>			  
			                            <h4 class='card-title'>
                                       <a href='#'>"
        categoryHtmlFixedPart3 "</a>
                                   </h4>
                                   <p class='card-text'"
        categoryHtmlFixedPart4 "</p>  
                            </div>
                          </div>
                        </div>"]
    (str categoryHtmlFixedPart1 (get input :picture) categoryHtmlFixedPart2 (get input :name) categoryHtmlFixedPart3 (get input :desc) categoryHtmlFixedPart4 )))


(defn home2 [request]
  (println "home2 handler : entered with "request)
  (let [session (get request :session)
        user (:user session nil)
        categoryList (dbutil/listProductCategories nil)
        categoryListHtml (reduce str (map constructCategoryListHtml categoryList))
        
        ]
    (println "USER from session =  !! "user)
    (println "categoryList !! "categoryList)
    (println "categoryListHtml finallllll "(type categoryListHtml))
    {  :status 200
         :headers {"Content-Type" "text/html"}
         :body  (cp/render-resource "public/home3.mustache" {:categoriesHtml categoryListHtml})
     }
    
   
    ))


(defn test [request]
  (println "test handler : entered with "request)
   {    :status 200
         :headers {"Content-Type" "text/html"}
         :body (io/input-stream (io/resource "public/test.html"))
         
    })

(defn login [request]
  (println "login handler : entered with "request)
   {     :status 200
         :headers {"Content-Type" "text/html"}
         :body (io/input-stream (io/resource "public/login.html"))
         
    })

(defn loginsubmit [request]
  (let [params (get request :params)
       inputEmail (get params "inputEmail")
       inputPassword (get params "inputPassword")
       loginResult   (dbutil/login {:inputEmail inputEmail :inputPassword inputPassword})
       ]
       (println "params is "params "inputEmail is "inputEmail "inputPassword is "inputPassword)
       (println "loginResult is "loginResult)
       (cond 
         (nil? loginResult)
         {:status 200
         :headers {"Content-Type" "text/html"}
         :body "Incorrect login credentials :( !"       
         }
         :else
         (do 
            (let [categoryList (dbutil/listProductCategories nil)
                 categoryListHtml (reduce str (map constructCategoryListHtml categoryList))
                 userName (get loginResult :name)
                 userEmail (get loginResult :email)
                 userType (get loginResult :type)
                 session (get request :session)
                 cookieVal (str "email=" userEmail ";name=" userName ";type=" userType)]
               (println "cookieVal = "cookieVal)
               {:status 200
               :headers {"Content-Type" "text/html"}
               :body  (cp/render-resource "public/home3.mustache" {:login true :greetuser true :user userName :categoriesHtml 
               categoryListHtml})
               :session (assoc session :user userName)
               ;Set cookies for UI. Above session key is for server.
               ;:cookies {"userCreds" {:value (str (:email loginResult) ";" (:first_name loginResult) ";" (:last_name loginResult)) :path "/"  } }
               :cookies {"userCreds" {:value  cookieVal :path "/"  } }
               }
             )))))

(defn loginsubmit2 [request]
  (let [params (get request :params)
       inputEmail (get params "inputEmail")
       inputPassword (get params "inputPassword")
       loginResult   (dbutil/login {:inputEmail inputEmail :inputPassword inputPassword})
       ]
       (println "params is "params "inputEmail is "inputEmail "inputPassword is "inputPassword)
       (println "loginResult is "loginResult)
       (cond 
         (nil? loginResult)
         {:status 200
         :headers {"Content-Type" "application/json"}
         :body {:msg "LogIn failed. Incorrect credentials"}       
         }
         :else
         (do (let [userName (get loginResult :name)
                  userEmail (get loginResult :email)
                  userType (get loginResult :type)
                  session (get request :session)]
                 {:status 200
                 :headers {"Content-Type" "application/json"}
                 :body {:status "success" :msg "Logged In, yayyyy"}     
                 :session (assoc session :user userName)
                 ;Set cookies for UI. Above session key is for server.               
                 :cookies {"userCreds" {:value (str "email=" userEmail ";name=" userName ";type=" userType) :path "/"  } }
                 })))))

(defn loginsubmit3 [request]
  (let [params (get request :body)
       inputEmail (get params "inputEmail")
       inputPassword (get params "inputPassword")
       loginResult   (dbutil/login {:inputEmail inputEmail :inputPassword inputPassword})
       ]
       (println "params is "params "inputEmail is "inputEmail "inputPassword is "inputPassword)
       (println "loginResult is "loginResult)
       (cond 
         (nil? loginResult)
         {:status 200
         :headers {"Content-Type" "application/json"}
         :body {:status "ERROR" :msg "LogIn failed. Incorrect credentials."}       
         }
         :else
         (do (let [userName (get loginResult :name)
                  userEmail (get loginResult :email)
                  userType (get loginResult :type)
                  session (get request :session)
                  cookieVal (str "email=" userEmail ";name=" userName ";type=" userType)]
                 (println "cookie val = "cookieVal)
                 {:status 200
                 :headers {"Content-Type" "application/json"}
                 :body {:status "SUCCESS" :msg "Logged In successfully." :userName userName :userType userType}     
                 :session (assoc session :user userName)
                 ;Set cookies for UI. Above session key is only for server.               
                 :cookies {"userCreds" {:value cookieVal :path "/"  } }
                 })))))


(def app
  (ring/ring-handler
    (ring/router
      [["/home" {:name ::home
                 :get home2
                 :post home}]
["/login" {:name ::login
                 :get login}]
["/loginsubmit" {:name ::loginsubmit
                 :post loginsubmit}]
;loginsubmit2 is with request content-type : application/x-www-form-urlencoded  
["/loginsubmit2" {:name ::loginsubmit2
                 :post loginsubmit2}]
;loginsubmit3 is with request content-type : application/json  
["/loginsubmit3" {:name ::loginsubmit3
                 :post loginsubmit3}]
["/test" {:name ::test
                 :get test}]
       ;Ref - https://metosin.github.io/reitit/ring/static.html
["/assets/*" (ring/create-resource-handler)]])
      (ring/create-default-handler)
{:middleware [
              ;Ref - Ring session handler
              session/wrap-session 
              ;Ref - Request Parameter handling in reitit - https://metosin.github.io/reitit/ring/default_middleware.html
              ;Looks like in Reitit, default request parameter handling type is application/x-www-form-urlencoded            
              parameters/parameters-middleware 
              ;If you want to handle application/json request params, you could use https://github.com/ring-clojure/ring-json's wrap-json-body/wrap-json-params
              json/wrap-json-body
              ;Ref - JSON response middleware for ring - https://github.com/ring-clojure/ring-json
              json/wrap-json-response
              cookies/wrap-cookies]}))

(defn -main
  "This is invoked at the start of the REPL. This will do all pre-processing required
  for this app, like creating DB tables etc."
  [& args]
  (println "Initializing application...")
  (dbutil/init-db-main nil)
  (jetty/run-jetty #'app {:host "192.168.10.12" :port 3000})
  )
