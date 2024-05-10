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
    (set)))

(defn get-pt [e cell-size]
  [(int (/ (.getX e) cell-size))
   (int (/ (.getY e) cell-size))])

(def achorn (atom #{[62 27] [58 27] [61 27] [60 26] [63 27] [57 27] [58 25]}))
(def achimsp16
  (atom #{[8 7] [7 12] [10 5] [13 15] [14 17] [15 16] [10 8] [16 7] [15 9] [18 10] [9 6]
          [6 13] [18 9] [16 8] [11 6] [8 15] [13 17] [17 10] [11 7] [9 7] [11 5] [9 13]
          [13 16] [14 14] [7 14] [6 12] [16 15] [16 10] [8 14] [8 12] [15 15] [17 8]}))
(def gosper_glider
  (atom #{[16 6] [12 6] [23 5] [19 6] [13 8] [22 5] [18 7] [2 5] [15 3] [12 5] [15 9]
          [37 4] [24 6] [36 4] [26 7] [26 2] [24 2] [18 6] [26 1] [17 4] [23 4] [12 7]
          [36 3] [22 3] [3 6] [37 3] [26 6] [14 3] [2 6] [13 4] [23 3] [22 4] [3 5]
          [17 8] [14 9] [18 5]}))
(def lightspeed_crawler
  (atom #{[8 8] [48 9] [27 8] [8 7] [8 9] [53 7] [19 9] [18 7] [32 8] [35 8] [24 9] [43 7]
          [14 6] [52 8] [44 9] [37 8] [9 6] [43 9] [38 9] [29 7] [4 7] [4 10] [19 7] [4 9]
          [18 9] [40 8] [34 9] [33 9] [5 7] [54 7] [4 8] [20 8] [29 9] [55 8] [47 8] [5 8]
          [28 7] [15 8] [6 8] [5 9] [23 9] [42 8] [34 7] [38 7] [22 8] [39 9] [24 7] [49 9]
          [45 8] [23 7] [44 7] [25 8] [49 7] [33 7] [28 9] [30 8] [9 10] [39 7] [14 7]
          [14 10] [48 7] [17 8] [53 9] [54 9] [14 9] [50 8]}))
(def weekender
  (atom #{[7 11] [2 8] [10 15] [7 4] [8 3] [5 4] [7 8] [9 16] [10 17] [9 3] [3 13] [5 13]
          [5 14] [10 2] [5 7] [11 3] [8 16] [2 12] [2 11] [5 6] [6 8] [6 11] [5 5] [7 9]
          [2 7] [3 6] [5 15] [7 10] [10 4] [6 9] [12 3] [6 10] [7 15] [12 16] [5 12] [11 16]}))

(def points (atom #{}))

(defn draw-grid [c g cell-size offset-x offset-y points]
  (let [rows (quot (width c) cell-size)
        cols (quot (height c) cell-size)
        w cell-size
        h cell-size]
    (doseq [x (range rows)
            y (range cols)]
      (draw g
            (rect (* x cell-size) (* y cell-size) w h)
            (style :background (cond
                                 (contains? points [(+ x offset-x) (+ y offset-y)]) "#A03232"
                                 (even? (+ x y)) "#212121"))))))

(defn init-ui []
  (let [frame
       (frame :title "clj-conway"
              :size [1200 :by 900]
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

(defmacro when! [c pred & actions]
  `(when ~pred
     ~@actions
     (repaint! ~c)))

(defn run-ui [root]
  (let [cell-size 15
        ms_inc 180
        c (select root [:#canvas])
        t (timer (fn [_]
                    (swap! points transform)
                    (repaint! c))
                  :start? false
                  :initial-delay ms_inc
                  :delay ms_inc)
        offset-x (atom 0)
        offset-y (atom 0)
        zoom (atom 1)]
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
          (when (= (.getKeyCode e) 32) ; space
            (if (.isRunning t)
              (.stop t)
              (.start t)))
          (when! c (= (.getKeyCode e) 37) ; left
            (reset! offset-x (- @offset-x 5)))
          (when! c (= (.getKeyCode e) 38) ; up
            (reset! offset-y (- @offset-y 5)))
          (when! c (= (.getKeyCode e) 39) ; right
            (reset! offset-x (+ @offset-x 5)))
          (when! c (= (.getKeyCode e) 40) ; down
            (reset! offset-y (+ @offset-y 5)))
          (when! c (and (= (.getKeyCode e) 45) (> @zoom 0.4)) ; minus
            (reset! zoom (- @zoom 0.1)))
          (when! c (and (= (.getKeyCode e) 61) (< @zoom 5)) ; equals
            (reset! zoom (+ @zoom 0.1)))
          (when! c (= (.getKeyCode e) 48) ; 0
            (reset! points #{}))
          (when! c (= (.getKeyCode e) 49) ; 1
            (reset! points @gosper_glider))
          (when! c (= (.getKeyCode e) 50) ; 2
            (reset! points @achimsp16))
          (when! c (= (.getKeyCode e) 51) ; 3
            (reset! points @achorn))
          (when! c (= (.getKeyCode e) 52) ; 4
            (reset! points @weekender))
          (when! c (= (.getKeyCode e) 53) ; 5
            (reset! points @lightspeed_crawler))
          (when! c (= (.getKeyCode e) 67) ; c
            (reset! offset-x 0)
            (reset! offset-y 0))
          (when! c (= (.getKeyCode e) 82) ; r
            (reset! points #{})
            (reset! offset-x 0)
            (reset! offset-y 0)
            (reset! zoom 1))
          (when! c (= (.getKeyCode e) 88) ; x
            (if (< (.getDelay t) ms_inc)
              (.setDelay t ms_inc)
              (.setDelay t (float (/ ms_inc 2)))))
          (when (= (.getKeyCode e) 80) ; p
            (println @points))
          ))
    (config! c :paint #(draw-grid %1 %2 (float (* cell-size @zoom)) @offset-x @offset-y @points))))

(defn -main []
  (-> (init-ui) run-ui))
