(ns clj-conway.core
  (:require [seesaw.core :refer [border-panel canvas frame invoke-later listen select timer width height native! repaint! show!]]
            [seesaw.graphics :refer [draw rect style]]
            [seesaw.config :refer [config!]]))

(defn neighbours [[x y]]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :when (not (and (= dx 0) (= dy 0)))]
    [(+ x dx) (+ y dy)]))

(defn alive? [adj alive?]
  (or (= adj 3)
      (and (= adj 2) alive?)))

(defn transform [pts]
  (->>
    (mapcat neighbours pts)
    (frequencies)
    (filter #(alive? (second %) (contains? pts (first %))))
    (map first)
    (set)
    ))

(defn get-pt [e cell-size]
  [(int (/ (.getX e) cell-size))
   (int (/ (.getY e) cell-size))])

(def points (atom #{[1 1] [2 1] [3 1] [1 2] [1 3] [2 3] [3 2] [3 3]}))

(defn draw-grid [c g cell-size points]
  (let [rows (quot (width c) cell-size)
        cols (quot (height c) cell-size)
        w cell-size
        h cell-size]
    (doseq [x (range rows)
            y (range cols)]
      (draw g
            (rect (* x cell-size) (* y cell-size) w h)
            (style :background (cond
                                 (contains? points [x y]) "#A03232"
                                 (even? (+ x y)) "#323232"))))))

(defn init-ui []
  (let [frame
       (frame :title "clj-conway"
              :size [800 :by 600]
              :on-close :exit
              :content
                (border-panel
                  :center (canvas :id :canvas
                                  :background "#121212")))]
    (native!)
    (invoke-later
      (-> frame
          show!))
    frame))

(defn run-ui [root]
  (let [cell-size 20
        ms_inc 400
        c (select root [:#canvas])
        t (timer (fn [_]
                    (swap! points transform)
                    (repaint! c))
                  :start? false
                  :initial-delay ms_inc
                  :delay ms_inc)]
    (listen c
      :mouse-clicked
        (fn [e]
          (let [pt (get-pt e cell-size)]
            (if (contains? @points pt)
              (swap! points disj pt)
              (swap! points conj pt))
            (repaint! c))))
    (listen root
      :key-pressed
        (fn [e]
          (when (= (.getKeyCode e) 32)
            (if (.isRunning t)
              (.stop t)
              (.start t)))))
    (config! c :paint #(draw-grid %1 %2 cell-size @points))))

(defn -main []
  (-> (init-ui) run-ui))
