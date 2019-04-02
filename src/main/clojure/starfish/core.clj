(ns starfish.core
  (:import [sg.dex.starfish Asset Invokable Agent Job Listing Ocean Operation Purchase])
  (:import [sg.dex.starfish.util DID Hex Utils])
  (:import [sg.dex.starfish.impl.memory MemoryAsset ClojureOperation])
  (:import [sg.dex.starfish.impl.remote RemoteAgent Surfer])
  (:import [java.nio.charset StandardCharsets])
  (:import [clojure.lang IFn])
  (:require [clojure.walk :refer [keywordize-keys stringify-keys]])
  (:require [clojure.data.json :as json]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def BYTE-ARRAY-CLASS (Class/forName "[B"))

(def ^:dynamic *ocean* (Ocean/connect))

(declare content asset? get-asset get-agent)

(defn json-string 
  "Coerces the argument to a JSON string"
  (^String [json]
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
      (asset? data) (to-string (content data))
      :else (throw (IllegalArgumentException. (str "Can't convert to string: " (class data)))))))

(defn asset? 
  "Returns true if the argument is an Asset"
  ([a]
    (instance? Asset a)))

(defn agent? 
  "Returns true if the argument is an Agent"
  ([a]
    (instance? Agent a)))

(defn did? 
  "Returns true if the argument is a W3C DID"
  ([a]
    (instance? DID a)))

(defn create-operation 
  "Create an in-memory operation with the given parameter list and function."
  ([params ^IFn f]
    (let [params (mapv str params)
          paramspec (reduce #(assoc %1 %2 {"type" "asset"}) {} params)
          meta {"params" paramspec}]
      (ClojureOperation/create (json-string meta) f))))

(defn invoke 
  "Invoke an operation with the given parameters. Parameters may be either a positional list
   or a map of name / value pairs"
  (^Job [^Operation operation params]
    (cond
      (map? params) (.invoke operation ^java.util.Map (stringify-keys params))
      :else (throw (Error. "Not yet supported")))))

(defn invoke-result 
  "Invokes an operation and wait for the result" 
  (^Asset [^Operation operation params]
    (let [job (invoke operation params)]
      (.awaitResult job))))

(defn asset
  "Coerces input data to an asset.
   - Existing assets are unchanged
   - DID are resolved to appropriate assets if possible
   - Strings and numbers are converted to memory assets containing the string representation
   - Map data structures are converted to JSON strings"
  (^Asset [data]
    (cond
      (asset? data) data
      (string? data) (MemoryAsset/create ^String data)
      (number? data) (MemoryAsset/create (str data))
      (map? data) (json-string data)
      (did? data) (get-asset (get-agent ^DID data))
      :else (throw (Error. (str "Not yet supported: " (class data)))))))

(defn asset-id 
  ([^Asset a]
    (.getAssetID a)))

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
  (^Agent [host]
    (Surfer/getSurfer host)))

(defn get-asset
  ([^Agent agent ^String asset-id]
    (.getAsset agent asset-id)))

(defn did
  "Gets the DID for the given input
   - DID is returned for Agents or assets
   - Strings are intrepreted as DIDs if possible"
  ([a]
    (cond 
      (asset? a) (.getAssetDID ^Asset a)
      (agent? a) (.getDID ^Agent a)
      (string? a) (DID/parse ^String a)
      :else (throw (IllegalArgumentException. (str "Can't get DID: " (class a)))))))

(defn get-agent
  (^Agent [agent-did]
    (cond 
      (agent? agent-did) agent-did
      (did? agent-did) (.getAgent *ocean* ^DID agent-did)
      :else (throw (IllegalArgumentException. (str "Invalid did: " (class agent-did)))))))

(defn upload
  (^Asset [^Agent agent ^Asset asset]
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
