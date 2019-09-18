(ns myshoppingapp.dbutil
  (:require [clojure.java.jdbc :as jdbc]))

(def db-spec {:classname   "org.sqlite.JDBC", :subprotocol "sqlite", :subname "myshoppingapp.db", :auto-commit? true})

(def user-table-ddl
  (jdbc/create-table-ddl :USER
                         [[:EMAIL "TEXT" :primary :key]
                          [:PASSWORD "TEXT" :not :null]
                          [:FIRST_NAME "TEXT" :not :null]
                          [:LAST_NAME "TEXT"]]))

(def prod-category-table-ddl
  (jdbc/create-table-ddl :PRODUCT_CATEGORY
                         [[:CATEGORY_ID "INTEGER" :primary :key]
                          [:CATEGORY_NAME "TEXT" :not :null]]))

(def order-table-ddl
  (jdbc/create-table-ddl :ORDERS                       
                         [[:ORDER_ID "INTEGER" :primary :key]
                          [:CONTACT_NAME "TEXT" :not :null]
                          [:DELIVERY_ADDRESS "TEXT" :not :null]]))

;Some tables are directly created with sql, as they have FOREIGN_KEY, and clojure.java.jdbc library does not support FOREIGN_KEY creation with create-table-ddl 
;https://stackoverflow.com/questions/1884818/how-do-i-add-a-foreign-key-to-an-existing-sqlite-table
(defn init-db-part1 "initialize the DB part 1" [input]
  (try (jdbc/db-do-commands db-spec ["create table if not exists USER (EMAIL TEXT PRIMARY KEY, PASSWORD TEXT NOT NULL, NAME TEXT NOT NULL, TYPE TEXT NOT NULL)" "create table if not exists PRODUCT_CATEGORY (ID INTEGER PRIMARY KEY, NAME TEXT NOT NULL, DESC TEXT NOT NULL, PICTURE TEXT NOT NULL)" "create table IF NOT EXISTS PRODUCT (PRODUCT_ID INTEGER PRIMARY KEY, CATEGORY_ID INTEGER NOT NULL, NAME TEXT NOT NULL, DESCRIPTION TEXT, PRICE INTEGER NOT NULL, FOREIGN KEY (CATEGORY_ID) REFERENCES PRODUCT_CATEGORY(CATEGORY_ID))" "create table if not exists ORDERS (ORDER_ID INTEGER PRIMARY KEY, CONTACT_NAME TEXT NOT NULL, DELIVERY_ADDRESS TEXT NOT NULL)" "create table IF NOT EXISTS ORDER_DETAILS (ORDER_DETAILS_ID INTEGER PRIMARY KEY, ORDER_ID INTEGER NOT NULL, PRODUCT_ID INTEGER NOT NULL, QUANTITY INTEGER NOT NULL, PRICE INTEGER NOT NULL, FOREIGN KEY (ORDER_ID) REFERENCES ORDERS(ORDER_ID), FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCT(PRODUCT_ID))"])
                                     ;"create table PRODUCT (PRODUCT_ID INTEGER PRIMARY KEY, CATEGORY_ID INTEGER NOT NULL, NAME TEXT NOT NULL, DESCRIPTION TEXT, PRICE INTEGER NOT NULL, FOREIGN KEY (CATEGORY_ID) REFERENCES PRODUCT_CATEGORY(CATEGORY_ID))"  "create table ORDER_DETAILS (ORDER_DETAILS_ID INTEGER PRIMARY KEY, ORDER_ID INTEGER NOT NULL, PRODUCT_ID INTEGER NOT NULL, QUANTITY INTEGER NOT NULL, PRICE INTEGER NOT NULL, FOREIGN KEY (ORDER_ID) REFERENCES ORDER(ORDER_ID), FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCT(PRODUCT_ID)"])
  (catch Exception e
  (prn "Caught " (.getMessage e))))) 


(defn init-db-main "initialize the DB" [input]
(init-db-part1 nil))

(defn listProductCategories [input]
	(let [queryStr (str "SELECT * FROM product_category" )
		   result  (jdbc/query  db-spec [queryStr])];;output is a clojure.lang.LazySeq jdbc/query should only be used where return results are expected]
		(prn "queryStr is " queryStr)		
		(prn "result is " result)
		;;(result);;if result is nil, java.lang.NullPointerException: null is thrown		
		result))

(defn login [loginFormData]
	(let [queryStr (str "SELECT email, password, name, type FROM user WHERE email = '" (:inputEmail loginFormData) "' AND password = '" (:inputPassword loginFormData)  "'" )
		   result ( -> (jdbc/query  db-spec [queryStr]);;output is a clojure.lang.LazySeq jdbc/query should only be used where return results are expected
					 (first))]
		(prn "queryStr is " queryStr)		
		(prn "result is " result)
		;;(result);;if result is nil, java.lang.NullPointerException: null is thrown		
		result))

