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
    (println "categoryList !! "categoryList)
    (println "categoryListHtml final "(type categoryListHtml))

    
    (cond 
      (nil? user)
      {  :status 200
         :headers {"Content-Type" "text/html"}
         :body  (cp/render-resource "public/home3.mustache" {:login false :greetuser false :categoriesHtml 
          categoryListHtml})
         :session (assoc session :user user)
      }
      :else
      
      {  :status 200
         :headers {"Content-Type" "text/html"}
         :body  (cp/render-resource "public/home3.mustache" {:login true :greetuser true :user user :categoriesHtml 
          categoryListHtml})
         :session (assoc session :user user)
      }
      )))


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
                 user (get loginResult :name)
                 type (get loginResult :type)
                 session (get request :session)]
               {:status 200
               :headers {"Content-Type" "text/html"}
               :body  (cp/render-resource "public/home3.mustache" {:login true :greetuser true :user user :categoriesHtml 
               categoryListHtml})
               :session (assoc session :user user)
               }
             )))))


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
["/test" {:name ::test
                 :get test}]
       ;Ref - https://metosin.github.io/reitit/ring/static.html
["/assets/*" (ring/create-resource-handler)]])
      (ring/create-default-handler)
{:middleware [session/wrap-session parameters/parameters-middleware]}))

(defn -main
  "This is invoked at the start of the REPL. This will do all pre-processing required
  for this app, like creating DB tables etc."
  [& args]
  (println "Initializing application...")
  (dbutil/init-db-main nil)
  (jetty/run-jetty #'app {:host "192.168.10.12" :port 3000})
  )
