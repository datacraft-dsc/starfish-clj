(ns starfish.core
  (:import [sg.dex.starfish Asset Invokable Agent Job Listing Ocean Operation Purchase])
  (:import [sg.dex.starfish.util DID Hex Utils])
  (:import [sg.dex.starfish.impl.memory MemoryAsset])
  (:import [sg.dex.starfish.impl.remote RemoteAgent Surfer])
  (:import [java.nio.charset StandardCharsets])
  (:require [clojure.walk :refer [keywordize-keys stringify-keys]])
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

(defn create-operation 
  "Create an in-memory operation with the given parameter spec and function."
  ([params f]
    ))

(defn memory-asset
  "Create an in-memory asset with the given metadata and data"
  (^Asset [meta data]
    (let [meta-str (json-string meta)
          byte-data (to-bytes data)]
      (MemoryAsset/create meta-str byte-data))))

(defn remote-agent
  "Gets a remote agent with the provided DID"
  ([did]
    (RemoteAgent/create *ocean* did)))

(defn surfer
  "Gets a surfer remote agent for the given Host string in the form 'http://www.mysurfer.com:8080'"
  ([host]
    (Surfer/getSurfer host)))

(defn get-asset
  ([^Agent agent ^String asset-id]
    (.getAsset agent asset-id)))

(defn upload
  ([^Agent agent ^Asset asset]
    (.uploadAsset agent asset)))

(defn metadata
  "Gets the metadata for an asset as a Clojure map"
  ([^Asset asset]
    (let [md (.getMetadata asset)]
      (keywordize-keys (into {} md)))))

(defn content
  "Gets ths content for a given asset"
  ([^Asset asset]
    (let []
      (.getContent asset))))
