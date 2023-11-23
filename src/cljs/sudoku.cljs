;; Originally from  Shermin Pei (github ship561)
;; https://gist.github.com/ship561
;; https://gist.github.com/ship561/3755868
;;
;; Minor edits due to changes to the early version of clojure used in the
;; original, additionally removed the main functions that dealt a file
;; base input I have no need for. And ported to cljs.
;;
(ns sudoku
  (:require
   [clojure.set :as sets]
   [cljs.pprint :as pp]))

(defn log
  [s]
  (.log js/console s))

;;;labels and definitions for the columns and rows
(def rowlabel [:A :B :C :D :E :F :G :H :I])
(def collabel [:a :b :c :d :e :f :g :h :i])
(def valid-numbers #{1 2 3 4 5 6 7 8 9})

(def rowkeys
  (for [r rowlabel
        c collabel]
    [r c]))

(def colkeys
  (for [c collabel
        r rowlabel]
    [r c]))

(def boxkeys
  (for [rk (partition-all 3 rowlabel)
        ck (partition-all 3 collabel)]
    (for [r rk
          c ck]
      [r c])))

(defn make-grid
  "Builds a map of the starting input puzzle from an input java array
  formatted to reflect the grid pattern"
  [ingrid]
  (let [g (->> ingrid
               js->clj
               (partition 9))
        grid (into {}
                   (map (fn [rk r]
                          [rk (into {}
                                    (map (fn [ck v]
                                           [ck (first (vals v))])
                                         collabel r))]) rowlabel g))]
    grid))

(defn get-peers
  "Gets the related rows, columns, and box of the input square coordinates
   sq given as [:row :col]"

  [sq]
  (let [[r c] sq
        row-check (filter #(= r (first %)) rowkeys)
        col-check (filter #(= c (second %)) colkeys)
        box-check (first (filter #(some (fn [x] (= x sq)) %) boxkeys))]
    [row-check col-check box-check]))

(defn valid-sq?
  "Ensures a valid square. sq is given as [:row :col]. Returns a vector of valid guesses."

  [sq grid]
  (let [checker (fn [check]
                  (= (sets/intersection
                      (set (map #(get-in grid %) check))
                      valid-numbers)
                     valid-numbers))]
    (vec (map checker (get-peers sq)))))

(defn valid-game-sq?
  [sq grid]
  (let [checker (fn [check]
                  (->> (map #(get-in grid %) check)
                       frequencies
                       (map #(cond
                               (= (key %) 0) true
                               (> (val %) 1) false
                               :else true))))]
    (map checker (get-peers sq))))

(defn valid-game?
  [grid]
  (every? true?
          (for [r rowlabel
                c collabel]
            (every? true? (flatten (valid-game-sq? [r c] grid))))))

(defn valid-puzzle?
  "Ensure that all squares are valid in the puzzle. Returns a boolean."

  [grid]
  (every? true?
          ;;scan all squares in the puzzle
          (for [r rowlabel
                c collabel]
            ;;make sure each square is valid compared to peers
            (every? true? (valid-sq? [r c] grid)))))
