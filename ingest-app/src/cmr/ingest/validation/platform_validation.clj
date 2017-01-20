(ns cmr.ingest.validation.platform-validation
  "Provides functions to validate the platforms during collection update"
  (:require
   [clojure.set :as s]
   [cmr.ingest.services.humanizer-alias-cache :as humanizer-alias-cache]))

(defn deleted-platform-searches
  "Returns granule searches for deleted platforms. We should not delete platforms in a collection
  that are still referenced by existing granules. This function builds the search parameters
  for identifying such invalid deletions."
  [context concept-id concept prev-concept]
  (let [platform-aliases (get (humanizer-alias-cache/get-humanizer-alias-map context) "platform")
        current-platforms (map :ShortName (:Platforms concept))
        aliases (mapcat #(get platform-aliases %) current-platforms)
        deleted-platform-names (s/difference
                                 (set (map :ShortName (:Platforms prev-concept)))
                                 (set (concat current-platforms aliases)))]
    (for [name deleted-platform-names]
      {:params {"platform[]" name
                :collection-concept-id concept-id}
       :error-msg (format (str "Collection Platform [%s] is referenced by existing"
                               " granules, cannot be removed.") name)})))
