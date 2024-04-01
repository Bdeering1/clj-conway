(ns clj-conway.core
  (:require [seesaw.core :refer [border-panel canvas frame invoke-later label select width height native! pack! show!]]
            [seesaw.graphics :refer [draw rect style]]
            [seesaw.config :refer [config!]]))

(def points (atom #{[0 1] [1 2] [2 0] [2 1] [2 2]}))

(defn draw-grid [c g cell-size points]
  (let [rows (quot (width c) cell-size)
        cols (quot (height c) cell-size)]
    (doseq [x (range rows)
            y (range cols)]
      (draw g
            (rect (* x cell-size) (* y cell-size) cell-size cell-size)
            (style :background (cond
                                 (contains? points [x y]) "#A03232"
                                 (even? (+ x y)) "#323232"))))))

(defn init-ui []
  (println "initializing UI...")
  (let [frame
       (frame :title "clj-conway"
              :size [500 :by 500]
              :on-close :exit
              :content (border-panel
                          :center (canvas :id :canvas
                                          :background "#121212")))]
    (native!)
    (invoke-later
      (-> frame
          ; pack!
          show!))
    frame))

(defn run-ui [root]
  (println "running UI...")
  (let [c (select root [:#canvas])]
    (config! c :paint #(draw-grid %1 %2 10 @points))))

(defn -main [& args]
  (-> (init-ui) run-ui))
