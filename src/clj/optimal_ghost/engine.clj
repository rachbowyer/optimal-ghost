(ns optimal-ghost.engine
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [mount.core :as mount]
    [medley.core :as medley]))

(defn- load-words []
  (-> "WORD.LST" io/resource slurp (str/split #"\n")))

(defn- add-word [tree path-length [f & r]]
  (let [child (when (some? f)
                (add-word (get-in tree [:children f]) (inc path-length)  r))]
    (cond-> (assoc (or tree {}) :path-length path-length)
            child
            (assoc-in [:children f] child)

            (nil? f)
            (assoc :word? true))))

(defn- add-words [tree words]
  (reduce (fn [tree word] (add-word tree 0 word))
          tree words))

(defn- calculate-best-move
  "Calculate the best move for the given tree.
   :children - a map of edges indexed by letter to child trees
   :path-length - the number of letters in the path from the root to this node
   :word? - true if this node completes a word
   :status - :winning if the player can force a win, :losing if the player cannot
             force a win, and :lost if the position is already lost
  :best-moves - the best moves to take from this position
                if winning any moves that force a win
                if losing all moves that delay the loss as long as possible
  :distance-to-resolution - the number of moves to win/lose from this position
                            if winning then based on the shortest path to a win
                            with the opponent trying to delay the loss
                            if losing then based on the longest path to a loss
                            with the opponent trying to win as soon as possible"
  [{:keys [children path-length word?] :as tree}]
  {:pre [(map? tree)]}
  (if (or (empty? children)
          (and (>= path-length 4) word?))
    (assoc tree :distance-to-resolution 0 :status :lost)
    (let [child-results
          (medley/map-vals calculate-best-move children)

          {winning-moves true losing-moves false}
          (group-by (fn [[_k {:keys [status path-length word?] :as v}]]
                      (and (or (>= path-length 4) (not word?))
                           (= status :losing)))
                    child-results)

          status
          (if (seq winning-moves) :winning :losing)

          get-dist-to-res
          (comp (partial * -1) :distance-to-resolution val)

          best-moves
          (if (= status :winning)
            (keys winning-moves)
            (->> losing-moves
                 (sort-by get-dist-to-res)
                 (partition-by get-dist-to-res)
                 first
                 (map key)))

          distance-to-resolution
          (cond
            (= status :winning)
            (inc (apply min (map :distance-to-resolution (vals winning-moves))))

            (and (= status :losing) (seq losing-moves))
            (inc (apply max (map :distance-to-resolution (vals losing-moves))))

            :else
            0)]
      (cond-> (assoc tree :status status
                          :best-moves best-moves
                          :distance-to-resolution distance-to-resolution)

              (seq child-results)
              (assoc :children child-results)))))

(defn get-move
  [{:keys [status children best-moves] :as _tree} [f & r :as _word]]
  (cond
    (nil? f)
    (if (= status :lost)
      [:opponent-completes-word (char 0)]
      (let [move (rand-nth best-moves)]
        (if (= (-> move children :status) :lost)
          [:computer-completes-word move]
          [:in-progress move])))

    (not (children f))
    [:opponent-invalid-word (char 0)]

    :else
    (recur (children f) r)))

;(defn get-status [{:keys [status children]} [f & r :as _word]]
;  (if (nil? f)
;    status
;    (if (not (children f))
;      :invalid-word
;      (recur (children f) r))))

(mount/defstate dict
  :start (->> (load-words)
              (add-words {})
              calculate-best-move))
