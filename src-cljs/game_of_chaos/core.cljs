(ns game-of-chaos.core
  (:require [clojure.browser.repl :as repl]
            [jayq.core :as jq
             :refer [$ append ajax inner html $deferred when done resolve pipe on]]))

($ (fn [] (repl/connect "http://localhost:9000/repl")))

(defn plot [context x y]
  (.fillRect context (int x) (int y) 1 1))

(def rules
  [(fn [x y]
     [0 (* 0.16 y)])
   (fn [x y]
     [(+ (* 0.85 x) (* 0.04 y)) (+ (* -0.04 x) (* 0.85 y) 1.6)])
   (fn [x y]
     [(+ (* 0.2 x) (* -0.26 y)) (+ (* 0.23 x) (* 0.22 y) 1.6)])
   (fn [x y]
     [(+ (* -0.15 x) (* 0.28 y)) (+ (* 0.26 x) (* 0.24 y) 0.44)])])

(defn run [iterations]
  (let [canvas (.get ($ "#c1") 0)
        ctx (.getContext canvas "2d")
        width (.-width canvas)
        height (.-height canvas)
        center [(quot width 2) (quot height 2)]
        [cx cy] center]
    (loop [i 1
           x 0
           y 0]
      (when (< i iterations)
        (let [px (+ cx (* x 100))
              py (+ height (* y -80))]
          (plot ctx px py))
        (let [rule (get rules (rand-int 4))
              [x y] (rule x y)]
          (recur (inc i) x y))))))

(defn inc-iteration-counter [n]
  (let [counter ($ "#iterations")
        counted (js/parseInt (.text counter))
        new-count (+ counted n)]
    (.text counter new-count)))

(def state (atom :running))

(defn start []
  (let [btn ($ "#start")
        run-again (fn run-again []
                    (when (= @state :running)
                      (run 10000)
                      (inc-iteration-counter 10000)
                      (js/setTimeout run-again 200)))]
    (.text btn "Stop")
    (run-again)))

(defn stop []
  (let [btn ($ "#start")]
    (reset! state :stopped)
    (.text btn "Start")))

; TODO: figure out why the event is not triggering
($ (on ($ :body) :click "#start" nil
      (fn [e]
        (jq/prevent e)
        (.log js/console "hi")
        (if (= (.text ($ "#start")) "Start")
          (start)
          (stop)))))
