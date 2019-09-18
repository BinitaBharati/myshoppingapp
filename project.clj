(defproject myshoppingapp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.xerial/sqlite-jdbc "3.25.2"]
                 ;[metosin/reitit "0.3.9"]
                 [ring "1.7.1"]
                 ;[ring/ring-core "1.7.1"]
                 [http-kit "2.3.0"]
                 ;[calfpath "0.7.2"]
                 [metosin/reitit "0.3.9"]
                 ;[javax.servlet/javax.servlet-api "4.0.1"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 ]
  :main ^:skip-aot myshoppingapp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :repl-options {:host "192.168.10.12" :port 42676 :init-ns myshoppingapp.core }
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010"]
  
  )
