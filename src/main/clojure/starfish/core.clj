(ns starfish.core
  (:import [sg.dex.starfish Asset Invokable Job Listing Ocean Operation Purchase])
  (:import [sg.dex.starfish.impl.memory MemoryAsset])
  (:import [java.nio.charset StandardCharsets])
  (:require [clojure.data.json :as json]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def BYTE-ARRAY-CLASS (Class/forName "[B"))

(def ^:dynamic *ocean* (Ocean/connect))

(declare content asset?)

(defn json-string 
  "Coerces the argument to a JSON string"
  (^{:tag String} [json]
    (cond
      (string? json) json
      (map? json) (json/write-str json)
      (vector? json) (json/write-str json)
      :else (throw (IllegalArgumentException. (str "Can't convert to JSON: " (class json)))))))

(defn to-bytes 
  "Coerces the data to a byte array."
  (^{:tag bytes} [data]
    (cond
      (instance? BYTE-ARRAY-CLASS data) ^bytes data 
      (string? data) (.getBytes ^String data StandardCharsets/UTF_8) 
      :else (throw (IllegalArgumentException. (str "Can't convert to bytes: " (class data)))))))

(defn to-string
  "Coerces data to a string format."
  (^String [data]
    (cond
      (instance? BYTE-ARRAY-CLASS data) (String. ^bytes data StandardCharsets/UTF_8)
      (string? data) data
      (asset? data) (to-string (content data)))))



(defn asset? 
  "Returns true if the argument is an Asset"
  ([a]
    (instance? Asset a)))

(defn memory-asset
  "Create an in-memory asset with the given metadata and data"
  ([meta data]
    (let [meta-str (json-string meta)
          byte-data (to-bytes data)]
      (MemoryAsset/create meta-str byte-data))))

(defn content
  "Gets ths content for a given asset"
  ([^Asset asset]
    (let []
      (.getContent asset))))
