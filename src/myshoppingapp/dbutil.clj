(ns myshoppingapp.dbutil
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as cjio]
            [clojure.string :as cljstr])
  (:import bharati.binita.myshoppingapp.util.LuceneUtil))

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
  (try (jdbc/db-do-commands db-spec ["create table if not exists USER (EMAIL TEXT PRIMARY KEY, PASSWORD TEXT NOT NULL, NAME TEXT NOT NULL, TYPE TEXT NOT NULL)" "create table if not exists PRODUCT_CATEGORY (ID INTEGER PRIMARY KEY, NAME TEXT NOT NULL, DESC TEXT NOT NULL, PICTURE TEXT NOT NULL)" "create table IF NOT EXISTS PRODUCT (PRODUCT_ID INTEGER PRIMARY KEY, CATEGORY_ID INTEGER NOT NULL, NAME TEXT NOT NULL, SEARCH_TAG TEXT NOT NULL, DESCRIPTION TEXT NOT NULL, PRICE TEXT NOT NULL, PICTURES TEXT NOT NULL, RANK TEXT NOT NULL, FOREIGN KEY (CATEGORY_ID) REFERENCES PRODUCT_CATEGORY(CATEGORY_ID))" "create table if not exists ORDERS (ORDER_ID INTEGER PRIMARY KEY, CONTACT_NAME TEXT NOT NULL, DELIVERY_ADDRESS TEXT NOT NULL)" "create table IF NOT EXISTS ORDER_DETAILS (ORDER_DETAILS_ID INTEGER PRIMARY KEY, ORDER_ID INTEGER NOT NULL, PRODUCT_ID INTEGER NOT NULL, QUANTITY INTEGER NOT NULL, PRICE INTEGER NOT NULL, FOREIGN KEY (ORDER_ID) REFERENCES ORDERS(ORDER_ID), FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCT(PRODUCT_ID))" "create table if not exists auto_suggest_trie (ID INTEGER PRIMARY KEY, VALUE TEXT NOT NULL, WORDEND INTEGER)" "create index auto_suggest_trie_idx on auto_suggest_trie(VALUE, WORDEND)"])
                                     ;"create table PRODUCT (PRODUCT_ID INTEGER PRIMARY KEY, CATEGORY_ID INTEGER NOT NULL, NAME TEXT NOT NULL, DESCRIPTION TEXT, PRICE INTEGER NOT NULL, FOREIGN KEY (CATEGORY_ID) REFERENCES PRODUCT_CATEGORY(CATEGORY_ID))"  "create table ORDER_DETAILS (ORDER_DETAILS_ID INTEGER PRIMARY KEY, ORDER_ID INTEGER NOT NULL, PRODUCT_ID INTEGER NOT NULL, QUANTITY INTEGER NOT NULL, PRICE INTEGER NOT NULL, FOREIGN KEY (ORDER_ID) REFERENCES ORDER(ORDER_ID), FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCT(PRODUCT_ID)"])
  (catch Exception e
  (prn "Caught " (.getMessage e))))) 

;Execute a sql script that bulk creates DB entries
(defn exec-sql-file  
   [file]  
     (jdbc/db-do-commands db-spec  (read-string (slurp file))))

(defn stripSingleQuotesFromDBSqlStringValues [dbValuesVector positionVector]
  (loop [curIdx 0
         updatedDbValuesVector (vec dbValuesVector)]
    
    (println "stripSingleQuotesFromDBSqlStringValues: curIdx = "curIdx ", updatedDbValuesVector = "updatedDbValuesVector)
    (println "stripSingleQuotesFromDBSqlStringValues: curValToBeStripped = "(get dbValuesVector (get positionVector curIdx)))
    (cond 
      (< curIdx (- (count positionVector) 1));skipping this function for handling rank, will strip rank manually
      (do (let [curValToBeStripped (get dbValuesVector (get positionVector curIdx))
                curValQuoteStartIdx 0
                curValQuoteEndIdx (cljstr/index-of curValToBeStripped "'" 1); whats happening here, why this doesnt work only for rank like values ie "'5'" ?
                
                strippedCurVal (subs curValToBeStripped (+ 1 curValQuoteStartIdx) curValQuoteEndIdx)]
             (println "stripSingleQuotesFromDBSqlStringValues: curValToBeStripped = "curValToBeStripped ", curValQuoteStartIdx = "curValQuoteStartIdx ", curValQuoteEndIdx = "curValQuoteEndIdx)
             (println "stripSingleQuotesFromDBSqlStringValues: curValToBeStripped = "curValToBeStripped ", strippedCurVal = "strippedCurVal)
             (println "stripSingleQuotesFromDBSqlStringValues: (+ 1 curValQuoteStartIdx) = "(+ 1 curValQuoteStartIdx))
             (println "abt to recur, curIdx + 1 = "(+ 1 curIdx))
             (recur (+ 1 curIdx) (assoc updatedDbValuesVector (get positionVector curIdx) strippedCurVal))))
      :else 
      (do (println "stripSingleQuotesFromDBSqlStringValues: in else part updatedDbValuesVector = "updatedDbValuesVector)
        (println "coming out of loop, curIdx = "curIdx)
           updatedDbValuesVector))))

(defn handleEachProduct [product]
  (println "handling "product)
                 (let [productInsert? (cljstr/includes? (cljstr/lower-case product) "insert or ignore into product (")]
                 (println "productInsert? = "productInsert?)
                 (if productInsert?
                  (do (let [valueStartIdx (cljstr/index-of (cljstr/lower-case product) "values (")
                           valueEndIdx (cljstr/index-of (cljstr/lower-case product) ")" valueStartIdx)
                           productValuesStr (subs (cljstr/lower-case product) (+ valueStartIdx 8) valueEndIdx)
                           productVector (cljstr/split productValuesStr #",")
                           updatedProductVector (stripSingleQuotesFromDBSqlStringValues productVector [2 3 4 5 6 7])  
                           ]
                          (println "productValuesStr = "productValuesStr)
                          (println "updatedProductVector = "updatedProductVector)
                          (println "updatedProductVector of 6 = "(get updatedProductVector 6))
                          {"searchTag" (get updatedProductVector 3) "description" (get updatedProductVector 4) "productId" (get updatedProductVector 0) "name" (get updatedProductVector 2) "price" (get updatedProductVector 5) "pictures" (get updatedProductVector 6) "rank" (subs (get updatedProductVector 7) 2 3)}
                       )))))


;Extract PRODUCT entries from file and convert it into a datastructure suitable for consumption by Lucene Indexer.
(defn constructDataStructureForJavaCall1  
   [file]  
     (println "constructDataStructureForJavaCall1: entered")
     (let [productVector (read-string (slurp file))
           luceneIdxWriterVector []]
       (println "constructDataStructureForJavaCall: productVector1111111111 = "(type productVector))
       (mapv handleEachProduct productVector)))

;Extract PRODUCT entries from file and convert it into a datastructure suitable for consumption by Lucene Indexer.
(defn index-products  
   [file]  
     (println "index-products: entered")
     (let [javaStruc (constructDataStructureForJavaCall1 file)
           finalJavaStruc (remove nil? javaStruc)]
       (println "finalJavaStruc = "finalJavaStruc)
       (LuceneUtil/createProductIndex finalJavaStruc "/home/vagrant/shared1/myshoppingapp/lucene/index")))


;Extract PRODUCT entries from file and convert it into a datastructure suitable for consumption by Lucene Indexer.
(defn constructDataStructureForJavaCall  
   [file]  
     (println "index-products1111: entered")   
        (let [test []])
        (loop [productVector (read-string (slurp file))
              luceneIdxWriterVector [] ]
          (println "luceneIdxWriterVector = "luceneIdxWriterVector)
          (cond 
            (> (count productVector) 0) 
             (do (let [eachProduct (first productVector)
                      eachProductLuceneEntry (handleEachProduct eachProduct)]
                    (println "eachProductLuceneEntry = "eachProductLuceneEntry)
                    (if eachProductLuceneEntry           
                        (do 
                          (println "About to conj as  eachProductLuceneEntry is not nil or false")
                          (recur  (next productVector) (conj luceneIdxWriterVector eachProductLuceneEntry)))
                        ;else recur without adding to luceneIdxWriterVector
                        (recur   (next productVector) luceneIdxWriterVector)))) 
           :else 
           (do 
             (println "in else part now as all productVector entries have been consumed.Time to return the luceneIdxWriterVector")
             luceneIdxWriterVector))))

(defn index-products1 [file]
  (let [luceneIndexerInput (constructDataStructureForJavaCall file)]
       (println "luceneIndexerInput = "luceneIndexerInput)
       (LuceneUtil/createProductIndex luceneIndexerInput "/home/vagrant/shared1/myshoppingapp/lucene/index")
    
    ))


(defn init-db-main "initialize the DB" [input]
 ;create schema
(init-db-part1 nil)
;populate schema
(exec-sql-file "db-mgmnt/populateDB.sql")
;create Lucene index
(let [out (index-products "db-mgmnt/populateDB.sql")]
 (println "out = "out))
)

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

(defn autosuggest [searchString]
	(let [queryStr (str "SELECT value from auto_suggest_trie where value like '" searchString "%' and WORDEND=1")
		   result ( -> (jdbc/query  db-spec [queryStr]);;output is a clojure.lang.LazySeq jdbc/query should only be used where return results are expected
					 )]
		(prn "queryStr is " queryStr)		
		(prn "result is " result)
    (cond 
      (nil? result)
      ()
      :else
      (do (map (fn [arg1] (get arg1 :value)) result)))))


