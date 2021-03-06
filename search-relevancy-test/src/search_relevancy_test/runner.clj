(ns search-relevancy-test.runner
  "Main entry point for executing tasks for the search-relevancy-test project."
  (:gen-class)
  (:require
   [cheshire.core :as json]
   [clj-http.client :as client]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [search-relevancy-test.anomaly-fetcher :as anomaly-fetcher]
   [search-relevancy-test.core :as core]
   [search-relevancy-test.ingest :as ingest]
   [search-relevancy-test.reporter :as reporter]
   [clojure.set :as set]))

(def tasks
  "List of available tasks"
  ["download-collections" "relevancy-tests"])

(defn- usage
 "Prints the list of available tasks."
 [& _]
 (println (str "Available tasks: " (string/join ", " tasks))))

(def base-search-path
  "http://localhost:3003/collections")

(defn- perform-search
  "Perform the search from the anomaly test by appending the search to the end
  of the base search path. Return results in JSON and parse."
  [anomaly-test]
  (let [response (client/get
                  (str base-search-path (:search anomaly-test))
                  {:headers {"Accept" "application/json"}})]
    (json/parse-string (:body response) true)))

(defn- perform-search-test
  "Perform the anomaly test. Perform the search and compare the order of the
  results to the order specified in the test. Print messages to the REPL."
  [anomaly-test]
  (let [search-results (perform-search anomaly-test)
        test-concept-ids (string/split (:concept-ids anomaly-test) #",")
        all-result-ids (map :id (:entry (:feed search-results)))
        result-ids (filter #(contains? (set test-concept-ids) %) all-result-ids)]
    (reporter/analyze-search-results anomaly-test test-concept-ids result-ids)))

(defn string-key-to-int-sort
  "Sorts by comparing as integers."
  [v1 v2]
  (< (Integer/parseInt (key v1))
     (Integer/parseInt (key v2))))

(defn- perform-tests
  "Read the anomaly test CSV and perform each test"
  []
  (doseq [tests-by-anomaly (sort string-key-to-int-sort
                                 (group-by :anomaly (core/read-anomaly-test-csv)))
          :let [test-count (count (val tests-by-anomaly))]]
    (println (format "Anomaly %s %s"
                     (key tests-by-anomaly)
                     (if (> test-count 1) (format "(%s tests)" test-count) "")))
    (doseq [individual-test (val tests-by-anomaly)]
      (perform-search-test individual-test))))

(defn relevancy-test
  "Reset the system, ingest all of the test data, and perform the searches from
  the anomaly testing CSV"
  []
  (let [test-files (core/test-files)]
    (println "Creating providers")
    (ingest/create-providers test-files)
    (println "Ingesting community usage metrics and test collections")
    (ingest/ingest-community-usage-metrics) ;; Needs to happen before ingest
    (ingest/ingest-test-files test-files)
    (println "Running tests")
    (perform-tests)))

(defn -main
  "Runs search relevancy tasks."
  [task-name & args]
  (case task-name
        "download-collections" (anomaly-fetcher/download-and-save-all-collections)
        "relevancy-tests" (relevancy-test)
        usage)
  (shutdown-agents))

(comment
 (relevancy-test))
