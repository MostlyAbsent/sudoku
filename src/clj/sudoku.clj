;; Originally from  Shermin Pei (github ship561)
;; https://gist.github.com/ship561
;; https://gist.github.com/ship561/3755868
;;
;; Minor edits due to changes to the early version of clojure used in the
;; original, additionally removed the main functions that dealt a file
;; base input I have no need for.

(ns sudoku
  (:require
   [clojure.string :as str]
   [clojure.set :as sets]))

;;;example input for testing
(def grid01
  (list "003020600"
        "900305001"
        "001806400"
        "008102900"
        "700000008"
        "006708200"
        "002609500"
        "800203009"
        "005010300"))
;;;example solution to input
(def grid01-sol
  (list "483921657"
        "967345821"
        "251876493"
        "548132976"
        "729564138"
        "136798245"
        "372689514"
        "814253769"
        "695417382"))
;;example input hard
(def grid50
  (list "300200000"
        "000107000"
        "706030500"
        "070009080"
        "900020004"
        "010800050"
        "009040301"
        "000702000"
        "000008006"))
;;example hardest sudoku in world?
(def hardestsudokuinworld
  (list "850002400"
        "720000009"
        "004000000"
        "000107002"
        "305000900"
        "040000000"
        "000080070"
        "017000000"
        "000036040"))

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
  "Builds a map of the starting input puzzle from an input string
  formatted to reflect the grid pattern"

  [ingrid]
  (let [grid (into {}
                   (map (fn [rk l]
                          [rk
                           (into {} (map (fn [ck v]
                                           [ck (Integer/parseInt v)])
                                         collabel (-> l
                                                      (str/split #""))))])
                        rowlabel ingrid))]
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
                  (= (sets/intersection (set (map #(get-in grid %) check)) valid-numbers)
                     valid-numbers))]
    (vec (map checker (get-peers sq)))))

(defn valid-puzzle?
  "Ensure that all squares are valid in the puzzle. Returns a boolean"

  [grid]
  (every? true?
          ;;scan all squares in the puzzle
          (for [r rowlabel
                c collabel]
            ;;make sure each square is valid compared to peers
            (every? true? (valid-sq? [r c] grid)))))

(defn valid-guess-set
  "Creates a set of valid values a square can be. Returns a set of values."

  [sq grid]
  (let [get-nums (fn [pos] (set (map #(get-in grid %) pos)))]
    (sets/difference valid-numbers ;finds valid numbers by removing already used numbers
                     (apply sets/union ;union all used numbers
                            (map get-nums (get-peers sq)))))) ;get the current numbers used by peers
(defn solver
  "Solves a sudoku puzzle input as grid. The loop is only allowed to
   proceed a certain number of times so that infinite loops cannot
   occur. So far 17 is the min number of known starting squares to
   solve a valid sudoku. Solver works by branching guesses until a
   solution is found. When solution is found, complete=true."

  [grid]
  (loop [i (range 64)
         cur-m grid
         prev-m []
         complete false]
    (let [guess (for [r rowlabel ;creates set of guesses for each square
                      c collabel]
                  (let [v (get-in cur-m [r c])]
                    (if (= 0 v)
                      [[r c] (valid-guess-set [r c] cur-m)]
                      [[r c] (set [v])])))
          {easy true hard false} (group-by #(= 1 (-> % second count)) guess) ;separates known from unknown
          hard (remove nil? (sort-by #(count (second %)) hard))]
      (if (and (seq i)
               (not complete))
        (do ;(prn complete)
            ;(prn "i" i)
            ;(when (= 0 (first i)) (prn "current map=" cur-m))
            ;(prn "easy" easy)
            ;(prn "hard" hard)
          (if (= cur-m prev-m)
            (do ;(prn "calling self")
              (first (remove (fn [grid] ;removes any invalid puzzles
                               (or (nil? grid)
                                   (not (valid-puzzle? grid))))
                             (flatten (map (fn [[k cur-guess]]
                                             ;;branches to make guesses when guesses are available
                                             ;;if no guesses can be made, returns nil as that puzzle
                                             ;;is unsolvable
                                             (when-not (empty? cur-guess)
                                               (map #(solver (assoc-in cur-m k %)) cur-guess)))
                                           (take 1 hard))))))
            (do ;(prn "recur")
              (recur (rest i)
                     (reduce (fn [m [k v]] ;fills in values known from deduction
                               (assoc-in m k (first v)))
                             cur-m easy)
                     cur-m
                     (valid-puzzle? cur-m)))))
        cur-m))))
