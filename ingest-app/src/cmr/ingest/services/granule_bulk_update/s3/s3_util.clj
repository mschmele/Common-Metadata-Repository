(ns cmr.ingest.services.granule-bulk-update.s3.s3-util
  "Contains functions to facilitate S3 url granule bulk update."
  (:require
   [clojure.string :as string]
   [cmr.common.services.errors :as errors]))

(defn validate-url
  "Validate the given S3 url for granule bulk update. It can be multiple urls
  separated by comma, and each url must be started with s3:// (case insensitive).
  Returns the parsed urls in a list."
  [input-url]
  (let [urls (map string/trim (string/split input-url #","))]
    (doseq [url urls]
      (when-not (or (string/starts-with? url "s3://")
                    (string/starts-with? url "S3://"))
        (errors/throw-service-errors
         :invalid-data
         [(str "Invalid URL value, each S3 url must start with s://, but was " url)])))
    urls))
