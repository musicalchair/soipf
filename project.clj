(defproject soipf "0.0.1-SNAPSHOT"
            :description "A simple forum application"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [org.clojure/tools.logging "0.2.4"]
                           [ring-anti-forgery "0.1.1"]
                           [congomongo "0.1.11-SNAPSHOT"]
                           [clj-time "0.3.4"]
                           [org.pegdown/pegdown "1.1.0"]
                           [org.clojars.hozumi/mongodb-session "1.0.1"
                            :exclusions [org.clojure/clojure
                                         ring/ring-core
                                         congomongo]]
                           [log4j/log4j "1.2.17"]]
            :profiles {:dev
                       {:dependencies
                        [[midje "1.4.0" :exclusions [org.clojure/clojure]]]
                        :plugins
                        [[lein-midje "2.0.0-SNAPSHOT"]]}}
            :main soipf.server
            :checksum :warn
            :uberjar-exclusions [#"META-INF/ECLIPSEF.SF"])
