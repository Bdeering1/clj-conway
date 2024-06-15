(defproject clj-conway "0.1.1"
  :description "Conway's Game of Life implemented in Clojure"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [seesaw "1.5.0"]]
  :main clj-conway.core
  :aot [clj-conway.core])
