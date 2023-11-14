(ns core
  (:require-macros
   [lib.helix-wrapper :as lh])
  (:require
   ["jotai" :as jotai]
   ["react-dom/client" :as rdom]
   [helix.core :refer [$]]
   [helix.dom :as d]))

(defn log
  [s]
  (.log js/console s))

(def grid
  (reduce (fn [acc _] (conj acc (jotai/atom 0))) [] (range 81)))

(def selected
  (jotai/atom 0))

(lh/defnc control [{:keys [id]}]
  (let [[s _] (jotai/useAtom selected)
        [_ set-v] (jotai/useAtom (get grid s))]
    (d/div {:class-name "border border-black w-6 h-6 flex justify-center items-center"
            :on-click #(set-v id)}
           (str id))))


(lh/defnc controls [_]
  (let [[sel _] (jotai/useAtom selected)]
    (d/div {:class-name "grid grid-rows-2 justify-center"}
           (d/div
            (d/div {:class-name "border border-black w-[9rem] h-6 flex justify-center items-center"
                    :on-click #(log sel)}
                   "Check Solution")
            (d/div {:class-name "h-4"})
            (d/div {:class-name "grid grid-cols-3 place-items-center gap-4"}
                   (map #($ control {:id %
                                     :key %}) (range 1 10)))))))

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
   {:class-name "grid grid-cols-3 my-4"}
   ($ controls)
   (d/div
    {:class-name "grid grid-cols-9 w-[18rem] place-items-center justify-center"}
    (map #($ cell {:id (.indexOf grid %)
                   :key (.indexOf grid %)}) grid))))

(defonce root (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init
  []
  (.render root ($ sudoku)))
