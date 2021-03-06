;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native.index
  (:refer-clojure :exclude [flush])
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv]
            [clojurewerkz.elastisch.arguments :as ar])
  (:import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse
           org.elasticsearch.action.admin.indices.create.CreateIndexResponse
           org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
           org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse
           org.elasticsearch.action.index.IndexResponse
           org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse
           org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse
           org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse
           org.elasticsearch.action.admin.indices.open.OpenIndexResponse
           org.elasticsearch.action.admin.indices.close.CloseIndexResponse
           org.elasticsearch.action.admin.indices.optimize.OptimizeResponse
           org.elasticsearch.action.admin.indices.flush.FlushResponse
           org.elasticsearch.action.admin.indices.refresh.RefreshResponse
           org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotResponse
           org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse
           org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse
           org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheResponse
           org.elasticsearch.action.admin.indices.status.IndicesStatusResponse
           org.elasticsearch.action.admin.indices.segments.IndicesSegmentResponse
           org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateResponse (org.elasticsearch.action.admin.indices.exists.types
                                                                                                TypesExistsResponse)))

;;
;; API
;;

(defn create
  "Creates an index.

   Accepted options are :mappings and :settings. Both accept maps with the same structure as in the REST API.

   Examples:

    (require '[clojurewerkz.elastisch.native.index :as idx])

    (idx/create \"myapp_development\")
    (idx/create \"myapp_development\" :settings {\"number_of_shards\" 1})

    (let [mapping-types {:person {:properties {:username   {:type \"string\" :store \"yes\"}
                                               :first-name {:type \"string\" :store \"yes\"}
                                               :last-name  {:type \"string\"}
                                               :age        {:type \"integer\"}
                                               :title      {:type \"string\" :analyzer \"snowball\"}
                                               :planet     {:type \"string\"}
                                               :biography  {:type \"string\" :analyzer \"snowball\" :term_vector \"with_positions_offsets\"}}}}]
      (idx/create \"myapp_development\" :mappings mapping-types))

   Related ElasticSearch API Reference section:
   http://www.elasticsearch.org/guide/reference/api/admin-indices-create-index.html"
  [^String index-name & args]
  (let [opts                        (ar/->opts args)
        {:keys [settings mappings]} opts
        ft                       (es/admin-index-create (cnv/->create-index-request index-name settings mappings))
        ^CreateIndexResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))


(defn exists?
  "Returns true if given index (or indices) exists"
  [index-name]
  (let [ft                        (es/admin-index-exists (cnv/->index-exists-request index-name))
        ^IndicesExistsResponse res (.actionGet ft)]
    (.isExists res)))


(defn type-exists?
  "Returns true if a type/types exists in an index/indices"
  [index-name type-name]
  (let [ft                        (es/admin-types-exists (cnv/->types-exists-request index-name type-name))
        ^TypesExistsResponse res (.actionGet ft)]
    (.isExists res)))


(defn delete
  "Deletes an existing index"
  ([]
     (let [ft                       (es/admin-index-delete (cnv/->delete-index-request))
           ^DeleteIndexResponse res (.actionGet ft)]
       {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))
  ([^String index-name]
     (let [ft                       (es/admin-index-delete (cnv/->delete-index-request index-name))
           ^DeleteIndexResponse res (.actionGet ft)]
       {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)})))

(defn get-mapping
  "The get mapping API allows to retrieve mapping definition of index or index/type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-mapping.html"
  ([^String index-name]
     (let [ft                       (es/admin-get-mappings (cnv/->get-mappings-request))
           ^GetMappingsResponse res (.actionGet ft)]
       (cnv/get-mappings-response->map res)))
  ([^String index-name ^String type-name]
     (let [ft                       (es/admin-get-mappings (cnv/->get-mappings-request index-name type-name))
           ^GetMappingsResponse res (.actionGet ft)]
       (cnv/get-mappings-response->map res))))

(defn update-mapping
  "The put mapping API allows to register or modify specific mapping definition for a specific type."
  [^String index-name ^String mapping-type & args]
  (let [opts                    (ar/->opts args)
        ft                      (es/admin-put-mapping (cnv/->put-mapping-request index-name mapping-type opts))
        ^PutMappingResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))

(defn delete-mapping
  "Allow to delete a mapping (type) along with its data."
  [^String index-name ^String mapping-type]
  (let [ft                       (es/admin-delete-mapping (cnv/->delete-mapping-request index-name mapping-type))
        ^PutMappingResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))

(defn update-settings
  "Updates index settings. No argument version updates index settings globally"
  ([index-name settings]
     (let [ft (es/admin-update-index-settings (cnv/->update-settings-request index-name settings))]
       (.actionGet ft)
       true)))


(defn open
  "Opens an index"
  [index-name]
  (let [ft                     (es/admin-open-index (cnv/->open-index-request index-name))
        ^OpenIndexResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))

(defn close
  "Closes an index"
  [index-name]
  (let [ft                     (es/admin-close-index (cnv/->close-index-request index-name))
        ^CloseIndexResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))

(defn optimize
  "Optimizes an index or multiple indices"
  [index-name & args]
  (let [opts                  (ar/->opts args)
        ft                    (es/admin-optimize-index (cnv/->optimize-index-request index-name opts))
        ^OptimizeResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn flush
  "Flushes an index or multiple indices"
  [index-name & args]
  (let [opts               (ar/->opts args)
        ft                 (es/admin-flush-index (cnv/->flush-index-request index-name opts))
        ^FlushResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn refresh
  "Refreshes an index or multiple indices"
  [index-name]
  (let [ft                 (es/admin-refresh-index (cnv/->refresh-index-request index-name))
        ^RefreshResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn snapshot
  "Performs a snapshot through the gateway for one or multiple indices"
  [index-name]
  (let [ft                           (es/admin-gateway-snapshot (cnv/->gateway-snapshot-request index-name))
        ^GatewaySnapshotResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn clear-cache
  "Clears caches index or multiple indices"
  [index-name & args]
  (let [opts                           (ar/->opts args)
        ft                             (es/admin-clear-cache (cnv/->clear-indices-cache-request index-name opts))
        ^ClearIndicesCacheResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn stats
  "Returns statistics about indexes.

   No argument version returns all stats.
   Options may be used to define what exactly will be contained in the response:

   :docs : the number of documents, deleted documents
   :store : the size of the index
   :indexing : indexing statistics
   :types : document type level stats
   :groups : search group stats to retrieve the stats for
   :get : get operation statistics, including missing stats
   :search : search statistics, including custom grouping using the groups parameter (search operations can be associated with one or more groups)
   :merge : merge operation stats
   :flush : flush operation stats
   :refresh : refresh operation stats"
  ([]
     (let [ft                        (es/admin-index-stats (cnv/->index-stats-request))
           ^IndicesStatsResponse res (.actionGet ft)]
       ;; TODO: convert stats into a map
       res))
  ([& args]
     (let [opts                      (ar/->opts args)
           ft                        (es/admin-index-stats (cnv/->index-stats-request opts))
           ^IndicesStatsResponse res (.actionGet ft)]
       ;; TODO: convert stats into a map
       res)))

(defn status
  "Returns status for one or more indices.

   Options may be used to define what exactly will be contained in the response:

   :recovery (boolean, default: false): should the status include recovery information?
   :snapshot (boolean, default: false): should the status include snapshot information?"
  [index-name & args]
  (let [opts                       (ar/->opts args)
        ft                         (es/admin-status (cnv/->indices-status-request index-name opts))
        ^IndicesStatusResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn segments
  "Returns segments information for one or more indices."
  [index-name]
  (let [ft                           (es/admin-index-segments (cnv/->indices-segments-request index-name))
        ^IndicesSegmentResponse res (.actionGet ft)]
    (merge (cnv/broadcast-operation-response->map res)
           (cnv/indices-segments-response->map res))))

(defn update-aliases
  "Performs a batch of alias operations. Takes a collection of actions in the form of

   { :add    { :index \"test1\" :alias \"alias1\" } }
   { :remove { :index \"test1\" :alias \"alias1\" } }"
  [ops & args]
  (let [opts                        (ar/->opts args)
        ft                          (es/admin-update-aliases (cnv/->indices-aliases-request ops opts))
        ^IndicesAliasesResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))

(defn create-template
  [^String template-name & args]
  (let [opts                          (ar/->opts args)
        ft                            (es/admin-put-index-template (cnv/->create-index-template-request template-name opts))
        ^PutIndexTemplateResponse res (.actionGet ft)]
    {:ok true :acknowledged (.isAcknowledged res)}))

(defn put-template
  [^String template-name & args]
  (let [opts                          (ar/->opts args)
        ft                            (es/admin-put-index-template (cnv/->put-index-template-request template-name opts))
        ^PutIndexTemplateResponse res (.actionGet ft)]
    {:ok true :acknowledged (.isAcknowledged res)}))

(defn delete-template
  [^String template-name]
  (let [ft                               (es/admin-delete-index-template (cnv/->delete-index-template-request template-name))
        ^DeleteIndexTemplateResponse res (.actionGet ft)]
    {:ok true :acknowledged (.isAcknowledged res)}))
