(ns core
  (:require-macros
   [lib.helix-wrapper :as lh])
  (:require
   ["jotai" :as jotai]
   ["react-dom/client" :as rdom]
   [clojure.spec.alpha :as s]
   [helix.core :refer [$ <>]]
   [helix.dom :as d]))

(defn log
  [s]
  (.log js/console s))

(def grid
  (into {}
        (for [m (range 9)
              n (range 9)]
          {(str m " " n) (jotai/atom  0)})))

(def selected
  (jotai/atom "0 0"))

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

(lh/defnc control [{:keys [id]}]
  (let [[s _] (jotai/useAtom selected)
        [_ set-v] (jotai/useAtom (get grid s))]
    (d/div {:class-name "border border-black w-6 h-6 flex justify-center items-center"
            :on-click #(set-v id)}
           (str id))))

(lh/defnc controls [_]
  (let [[sel _] (jotai/useAtom selected)]
      (d/div {:class-name "grid grid-rows-2"}
             (d/div {:on-click #(log sel)} "selected")
             (d/div {:class-name "grid grid-cols-9 place-items-center"}
                    (map #($ control {:id %
                                      :key %}) (range 1 10))))))

(lh/defnc cell
  [{:keys [id]}]
  (let [[v _] (jotai/useAtom (get grid id))
        [s set-s] (jotai/useAtom selected)]
    (d/div {:class-name (str "border border-black w-8 h-8 flex justify-center items-center"
                             (if (= s id)
                               " bg-emerald-400"))
            :on-click #(set-s id)
            :id id}
           v)))

(lh/defnc sudoku
  []
  (set! (. js/document -title) "Sudoku!")
  (d/div
   {:class-name "grid grid-cols-3"}
   ($ controls)
   (d/div
    {:class-name "grid grid-cols-9 w-[18rem] place-items-center justify-center"}
    (map #($ cell {:id (first %)
                   :key (first %)} %) grid))))

(defonce root (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init
  []
  (.render root ($ sudoku)))
