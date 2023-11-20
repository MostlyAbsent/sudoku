(ns core
  (:require-macros
   [lib.helix-wrapper :as lh])
  (:require
   ["jotai" :as jotai]
   ["jotai/utils" :as jutils]
   ["react-dom/client" :as rdom]
   [helix.core :refer [$]]
   [helix.dom :as d]
   [sudoku]))

(defn log
  [s]
  (.log js/console s))

(def grid
  (jotai/atom
   (clj->js
    (into []
          (map-indexed
           (fn [idx itm] {idx itm})
           (reduce (fn [acc _] (conj acc 0)) [] (range 81)))))))

(def locked-cells
  (jotai/atom
   (clj->js {})))

(def empty-grid
 (reduce (fn [acc [idx x]]
                             (assoc acc idx x))
                           {}
                           (map-indexed
                            (fn [idx itm] [idx itm])
                            (reduce (fn [acc _] (conj acc 0)) [] (range 81)))))
(def split-grid
  (jutils/splitAtom grid))

(def selected
  (jotai/atom "0"))

(defn randomize-locked-cells []
  (reduce
   (fn [acc _]
     (loop [p (rand-int 80)
            v (inc (rand-int 9))]
       (if (not-any? true? (map #(= p (get acc %)) acc))
         (conj acc {p v})
         (recur (rand-int 80) (inc (rand-int 9))))))
   {}
   (range 20)))

(defn new-game-grid
  [lc]
  (->> lc
       (merge empty-grid)
       vec
       (sort #(compare (first %1) (first %2)))
       (map (fn [[p v]]
              {p v}))))

(defn calculate-grid-update
  [g idx v]
  (clj->js
   (into []
         (map (fn [c]
                (let [c-clj (js->clj c)
                      i (first (keys c-clj))]
                  (if (= idx i)
                    {idx v}
                    c-clj))) g))))

(lh/defnc num-button [{:keys [id]}]
  (let [[s _] (jotai/useAtom selected)
        [g set-g] (jotai/useAtom grid)]
    (d/div {:class-name "border border-black w-6 h-6 flex justify-center items-center"
            :on-click (fn [_] (set-g (calculate-grid-update g s id)))}
           (str id))))

(lh/defnc numpad [_]
  (d/div {:class-name "grid grid-rows-2 justify-center"}
         (d/div
          (d/div {:class-name "h-4"})
          (d/div {:class-name "grid grid-cols-3 place-items-center gap-4"}
                 (map #($ num-button {:id %
                                   :key %}) (range 1 10))) )))

(lh/defnc cell
  [{:keys [cell-atom]}]
  (let [[sel set-sel] (jotai/useAtom selected)
        [cellv _] (jotai/useAtom cell-atom)
        idx (-> cellv js->clj first first)
        v (-> cellv js->clj first second)]
   (d/div {:class-name (str "border border-black w-8 h-8 flex justify-center items-center"
                             (if (= sel idx)
                               " bg-emerald-400"))
            :on-click #(set-sel idx)
            :id idx}
           v)))

(lh/defnc controls
  []
  (let [[g set-g] (jotai/useAtom grid)
        [lc set-lc] (jotai/useAtom locked-cells)]
    (d/div {:class-name "grid grid-cols-2 max-w-[18rem]"}
           (d/div {:class-name "border border-black w-[9rem] h-6 flex justify-center items-center"
                   :on-click (fn []
                               (loop [lc (randomize-locked-cells)
                                      g (new-game-grid lc)]
                                 (if (sudoku/valid-game? (sudoku/make-grid g))
                                   (doall
                                    (set-g (clj->js g))
                                    (set-lc (clj->js lc)))
                                   (let [nlc (randomize-locked-cells)]
                                     (recur nlc (new-game-grid nlc))))))}
            "New Game")
     (d/div {:class-name "border border-black w-[9rem] h-6 flex justify-center items-center"
             :on-click #(log (sudoku/valid-puzzle? (sudoku/make-grid g)))}
            "Check Solution"))))

(lh/defnc sudoku
  []
  (set! (. js/document -title) "Sudoku!")
  (let [[g _] (jotai/useAtom split-grid)]
    (d/div
     ($ controls)
     (d/div
      {:class-name "grid grid-cols-3 my-4"}
      ($ numpad)
      (d/div
       {:class-name "grid grid-cols-9 w-[18rem]"}
       (map #($ cell {:cell-atom %
                      :key %}) g))
      ))))

(defonce root (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init
  []
  (.render root ($ sudoku)))
