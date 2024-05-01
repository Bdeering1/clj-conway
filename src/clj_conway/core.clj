(ns clj-conway.core
  (:gen-class)
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

(def achimsp16
  (atom #{[8 7] [7 12] [10 5] [13 15] [14 17] [15 16] [10 8] [16 7] [15 9] [18 10] [9 6]
          [6 13] [18 9] [16 8] [11 6] [8 15] [13 17] [17 10] [11 7] [9 7] [11 5] [9 13]
          [13 16] [14 14] [7 14] [6 12] [16 15] [16 10] [8 14] [8 12] [15 15] [17 8]}))
(def gosper_glider
  (atom #{[16 6] [12 6] [23 5] [19 6] [13 8] [22 5] [18 7] [2 5] [15 3] [12 5] [15 9]
          [37 4] [24 6] [36 4] [26 7] [26 2] [24 2] [18 6] [26 1] [17 4] [23 4] [12 7]
          [36 3] [22 3] [3 6] [37 3] [26 6] [14 3] [2 6] [13 4] [23 3] [22 4] [3 5]
          [17 8] [14 9] [18 5]}))

(def points (atom #{}))

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
                                 (even? (+ x y)) "#212121"))))))

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
  (let [cell-size 18
        ms_inc 200
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
            (when (.isRunning t)
              (.stop t))
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
              (.start t)))
          (when (= (.getKeyCode e) 48)
            (reset! points #{})
            (repaint! c))
          (when (= (.getKeyCode e) 49)
            (reset! points @gosper_glider)
            (repaint! c))
          (when (= (.getKeyCode e) 50)
            (reset! points @achimsp16)
            (repaint! c))
          (when (= (.getKeyCode e) 80)
            (println @points))
          ))
    (config! c :paint #(draw-grid %1 %2 cell-size @points))))

(defn -main []
  (-> (init-ui) run-ui))
