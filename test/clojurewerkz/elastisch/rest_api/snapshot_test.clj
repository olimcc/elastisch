;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.snapshot-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.admin :as admin]
            [clojurewerkz.elastisch.rest.response :refer [acknowledged? accepted?]]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-tweets-index)

(defn ^String tmp-dir
  []
  (System/getProperty "java.io.tmpdir"))

(deftest ^{:rest true} test-snapshotting
  (let [repo "backup1"
        p    (tmp-dir)
        s    "snapshot1"
        r1   (admin/register-snapshot-repository repo
                                                 :type "fs"
                                                 :settings {:location p
                                                            :compress true})
        _  (admin/delete-snapshot repo s)
        r2 (admin/take-snapshot repo s :wait-for-completion? true)
        r3 (admin/delete-snapshot repo s)]
    (is (acknowledged? r1))
    (is (accepted? r2))
    (is (acknowledged? r3))))
