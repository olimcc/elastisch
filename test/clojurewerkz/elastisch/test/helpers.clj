;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.test.helpers
  (:require [clojurewerkz.elastisch.native :as es]))

(defn ci?
  "Returns true if tests are running in the CI environment
   (on travis-ci.org)"
  []
  (System/getenv "CI"))

(defn infer-cluster-name
  "Returns current cluster name set via the ES_CLUSTER_NAME env variable"
  []
  (get (System/getenv) "ES_CLUSTER_NAME" "elasticsearch"))

(defn connect-native-client
  ([]
     (connect-native-client (infer-cluster-name)))
  ([cluster-name]
     (es/connect! [["127.0.0.1" 9300]]
                  {"cluster.name" cluster-name })))

(defn maybe-connect-native-client
  []
  (when (not (es/connected?))
    (connect-native-client)))
