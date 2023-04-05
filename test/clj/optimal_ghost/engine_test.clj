(ns optimal-ghost.engine-test
  (:require
    [clojure.data.generators :as gen]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [optimal-ghost.engine :as engine])
  (:import [java.util Random]))


(def ^:private add-words #'engine/add-words)
(def ^:private calculate-best-move #'engine/calculate-best-move)

(def ^:private words
  ["abc"
   "abd"
   "abde"
   "abdef"
   "abdxy"
   "abdtu"
   "abdghijkl"
   "abdgklm"
   "abdgklx"
   "abdgknpqrst"
   "zkl"])


(defn- get-node [{:keys [children] :as node} [f & r :as _word]]
  (if (nil? f)
    node
    (if (not (children f))
      :invalid-word
      (recur (children f) r))))

(defn- get-status [node word]
  (:status (get-node node word)))

(defn- get-distance [node word]
  (:distance-to-resolution (get-node node word)))

(defn- get-best-moves [node word]
  (:best-moves (get-node node word)))



(deftest test-add-words
  (let [dict      (->> words (add-words {}) calculate-best-move)
        expected  (->> "trie.edn" io/resource slurp edn/read-string)]
    (testing ""
      (is (= expected dict)))))


(deftest test-calculate-best-move
  (let [dict     (->> words (add-words {}) calculate-best-move)
        status   (partial get-status dict)
        distance (partial get-distance dict)
        moves    (partial get-best-moves dict)]
    (testing "Nodes have the correct status"
      (testing ":unable-to-move"
        (is (= :unable-to-move (status "abc"))))
      (testing ":completes-word"
        (are [word] (= :completes-word (status word))
                    "abde"
                    "abdxy"
                    "abdtu"
                    "abdghijkl"
                    "abdgklm"
                    "abdgknpqrst"))
      (testing "unreachable"
        ;; Because a prefix is a word > 3 letters so the game never gets to it
        (is (nil? (status "abdef"))))
      (testing ":winning"
        (are [word] (= :winning (status word))
                    "a"
                    "abd"
                    "abdgh"
                    "abdghij"
                    "abdgk"
                    "abdgknp"
                    "abdgknpqr"))
      (testing ":losing"
        (are [word] (= :losing (status word))
                    ""
                    "ab"
                    "abdx"
                    "abdg"
                    "abdghi"
                    "abdghijk"
                    "abdgkl"
                    "abdgkn"
                    "abdgknpq"
                    "abdgknpqrs"))
      (testing "Distances from the end of the game"
        (are [word dist] (= dist (distance word))
                         "abc" 0
                         "abde" 0
                         "abdghijkl" 0
                         "abdghijk" 1
                         "abdghij" 2
                         "abdghi" 3
                         "abdgh" 4
                         "abdgklm" 0
                         "abdgkl" 1
                         "abdgknpqrst" 0
                         "abdgknpqrs" 1
                         "abdgknpqr" 2
                         "abdgknpq" 3
                         "abdgknp" 4
                         "abdgkn" 5
                         "abdgk" 2 ; Winner chooses shortest past
                         "abdg" 5
                         "abdxy" 0
                         "abdx" 1
                         "abdtu" 0
                         "abdt" 1
                         "abd" 2
                         "ab" 3
                         "a" 4
                         "" 5))
      (testing "Correct moves to play"
        (are [word dist] (= dist (moves word))
                         ""             #{\a}
                         "a"            #{\b}
                         "ab"           #{\d}
                         "abd"          #{\x \t \g} ; All the winning moves
                         "abde"         nil
                         "abdg"         #{\h} ; Longest path - not winner can force a quick win on the other option
                         "abdgh"        #{\i}
                         "abdghi"       #{\j}
                         "abdghij"      #{\k}
                         "abdghijk"     #{\l}
                         "abdgk"        #{\l \n} ; Both are winning moves
                         "abdgkl"       #{\m \x} ; Both losing moves are equal distance from the end
                         "abdgkn"       #{\p}
                         "abdgknp"      #{\q}
                         "abdgknpq"     #{\r}
                         "abdgknpqr"    #{\s}
                         "abdgknpqrs"   #{\t})))))

(deftest test-get-best-moves
  (let [dict (->> words (add-words {}) calculate-best-move)]
    (testing "Check the best move is returned"
      (binding [gen/*rnd* (Random. 123)]
        (are [word expected] (= expected (engine/get-move dict word))
          "Fred"    [:opponent-invalid-word (char 0)]
          "abd"     [:in-progress \x]
          "abd"     [:in-progress \x]
          "abd"     [:in-progress \g]
          "abd"     [:in-progress \t]
          "abd"     [:in-progress \x]
          "abd"     [:in-progress \x]
          "abdgkl"  [:computer-completes-word \x]
          "abdgkl"  [:computer-completes-word \m]
          "abdgkl"  [:computer-completes-word \x]
          "abc"     [:opponent-unable-to-move (char 0)]
          "abde"    [:opponent-completes-word (char 0)]
          "zk"      [:computer-unable-to-move \l])))))
