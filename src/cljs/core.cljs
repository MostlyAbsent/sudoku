(ns core
  (:require-macros
   [lib.helix-wrapper :as lh])
  (:require
   ["react-dom/client" :as rdom]
   [clojure.spec.alpha :as s]
   [helix.core :refer [$]]
   [helix.hooks :as hooks]
   [helix.dom :as d]))

(def grid
  (atom
   (into {}
         (for [m (range 9)
               n (range 9)]
           {[m n] 0}))))

(defn log
  [s]
  (.log js/console s))

(def charset "0123456789")

(s/def :sudoku/charset (fn [s] (not-any?
                                 false?
                                 (map #(contains? (set charset) %) s))))

(s/def :sudoku/length #(= (count %) 1))

(s/def :sudoku/empty #(= "" %))

(s/def :sudoku/valid? #(s/or
                        :sudoku/empty
                        (s/and :sudoku/charset :sudoku/length)))
(defn handle-on-change
  [e i f]
  (let [v (js/parseInt (.-value (.-target e)))
        m (first i)
        n (second i)]
    (swap! grid assoc [m n] v)
    (f v)))

(lh/defnc cell
  [{:keys [index]}]
  (let [[v set-v] (hooks/use-state 0)]
    (d/div {:class-name "border border-black"}
           (d/input {:default-value v
                     :type "text"
                     :index index
                     :class-name "w-8 h-8 text-center"
                     :on-change (fn [e] (handle-on-change e index set-v))
                     :on-click #() #_(.log js/console (second index))}))))

(lh/defnc sudoku
  []
  (set! (. js/document -title) "Sudoku!")
  (d/div
   {:class-name "grid grid-cols-3"}
   (d/div)
   (d/div
    {:class-name "grid grid-cols-9 w-[18rem] place-items-center justify-center"}
    (->> (range 9)
         (map (fn [m]
                (->> (range 9)
                     (map (fn [n]
                            ($ cell {:key [m n]
                                     :index [m n]}))))))))))

(defonce root (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init
  []
  (.render root ($ sudoku)))
