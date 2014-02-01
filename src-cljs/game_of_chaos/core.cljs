(ns game-of-chaos.core
  (:require ;[clojure.browser.repl :as repl]
            [jayq.core :as jq
             :refer [$ append ajax inner html $deferred when done resolve pipe on]]))

;($ (fn [] (repl/connect "http://localhost:9000/repl")))
;($ (fn [] (repl/connect "http://betalabs:9000/repl")))

(defn plot [context x y]
  (.fillRect context (int x) (int y) 1 1))

(defn get-param [k i]
  (let [id (str "#" k i)
        v (.val ($ id))]
    (if (empty? v)
      0
      (cljs.reader/read-string v))))

(defn get-params [i]
  (let [ks ["a" "b" "c" "d" "e" "f" "p"]]
    (map #(get-param % i) ks)))

(defn rule [i]
  (let [[a b c d e f p] (get-params i)]
    (fn [x y]
      [(+ (* a x) (* b y) e) (+ (* c x) (* d y) f)])))

(defn rules []
  (vec (map rule (range 4))))

(defn rand-rule-id [ps]
  (let [n (rand)]
    (loop [i 0
           cp 0.0]
      (let [new-cp (+ cp (get ps i))]
        (if (>= new-cp n)
          i
          (recur (inc i) new-cp))))))

(defn run [iterations]
  (let [rules (rules)
        ps (vec (map (comp last get-params) (range 4)))
        canvas (.get ($ "#canvas") 0)
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
        (let [rule (get rules (rand-rule-id ps))
              [x y] (rule x y)]
          (recur (inc i) x y))))))

(defn inc-iteration-counter [n]
  (let [counter ($ "#iterations")
        counted (js/parseInt (.text counter))
        new-count (+ counted n)]
    (.text counter new-count)))

(def state (atom :running))

(defn start []
  (reset! state :running)
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

(defn clear []
  (let [canvas (.get ($ "#canvas") 0)
        ctx (.getContext canvas "2d")
        width (.-width canvas)
        height (.-height canvas)]
    (.clearRect ctx 0 0 width height)))

($ #(on ($ :body) :click "#start" {}
      (fn [e]
        (jq/prevent e)
        (if (= (.text ($ "#start")) "Start")
          (start)
          (stop)))))

($ #(on ($ :body) :click "#clear" {}
      (fn [e]
        (jq/prevent e)
        (clear))))

