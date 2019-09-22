(ns starfish.core
  (:require [clojure.walk :refer [keywordize-keys stringify-keys]]
            [clojure.data.json :as json]
            [starfish.utils :refer [error TODO]])
  (:import [java.nio.charset
            StandardCharsets]
           [java.io InputStream]
           [java.time
            Instant]
           [clojure.lang
            IFn]
           [sg.dex.crypto
            Hash]
           [sg.dex.starfish.util
            DID Hex Utils RemoteAgentConfig ProvUtil DDOUtil JSON]
           [sg.dex.starfish
            Asset DataAsset Invokable Agent Job Listing Ocean Operation Purchase]
           [sg.dex.starfish.impl.memory
            MemoryAsset ClojureOperation MemoryAgent]
           [sg.dex.starfish.impl.remote
            RemoteAgent RemoteAccount]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def BYTE-ARRAY-CLASS (Class/forName "[B"))

(def ^{:dynamic true :tag Ocean}  *ocean* (Ocean/connect))

(declare content asset? get-asset get-agent)

;;===================================
;; Starfish predicates

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

;;===================================
;; Utility functions, coercion etc.

(defn- json-key-fn
  "Convert to string function - suitable for producing JSON keys from Clojure symbols and keywords"
  ^String [k]
  (cond
    (symbol? k) (name k)
    (keyword? k) (subs (str k) 1) ;; do NOT interpret / for namespaced keywords
    :else (str k)))

(defn json-string
  "Coerces the argument to a valid JSON string.
   Optional pprint parameter may be used to pretty-print the JSON (default false)"
  (^String [json]
   (json-string json false))
  (^String [json pprint?]
   (let [write-json (if pprint?
                      #(with-out-str (json/pprint %))
                      #(json/write-str % :key-fn json-key-fn))]
     (cond
       (string? json) (json/write-str json)
       (map? json) (write-json json)
       (vector? json) (write-json json)
       (number? json) (write-json json)
       (boolean? json) (write-json json)
       (instance? java.util.Map json) (write-json (into {} json))
       (nil? json) (write-json json)
       :else (error "Can't convert to JSON: " (class json))))))

(defn json-string-pprint
  "Coerces the argument to a pretty-printed JSON string"
  (^String [json]
   (json-string json true)))

(defn read-json-string
  "Parses JSON string to a Clojure Map. Converts map keys to Clojure keywords."
  ([json-str]
   (json/read-str json-str :key-fn keyword)))

(defn to-bytes
  "Coerces the data to a byte array.
    - byte arrays are returned unchanged
    - Strings converted to UTF-8 byte representation
    - Assets have their raw byte content returned"
  (^{:tag bytes} [data]
   (cond
     (bytes? data) ^bytes data
     (string? data) (.getBytes ^String data StandardCharsets/UTF_8)
     (asset? data) (.getBytes ^String data StandardCharsets/UTF_8)
     :else (error "Can't convert to bytes: " (class data)))))

(defn to-string
  "Coerces data to a string format."
  (^String [data]
   (cond
     (bytes? data) (String. ^bytes data StandardCharsets/UTF_8)
     (string? data) data
     (asset? data) (to-string (content data))
     :else (error "Can't convert to string: " (class data)))))

(defn hex->bytes
  "Convert hex string to bytes"
  (^bytes [^String h]
   (Hex/toBytes h)))

(defn bytes->hex
  "Convert bytes to hex string"
  (^String [^bytes b]
   (Hex/toString b)))

(defn int->hex
  "Convert int to hex string"
  (^String [^long i]
   (Hex/toString i)))

(defn hex->int
  "Convert hex string to int"
  (^long [^String h]
   (Long/parseLong h 16)))

(defn random-hex-string
  "Creates a random hex string of the specified length in bytes"
  [len]
  (Utils/createRandomHexString len))

;; =================================================
;; Identity

(defn did
  "Gets the DID for the given input
   - DID is returned for Agents or Assets
   - Strings are intrepreted as DIDs if possible"
  (^DID [a]
   (cond
     (did? a)    a
     (asset? a)  (.getAssetDID ^Asset a)
     (agent? a)  (.getDID ^Agent a)
     (string? a) (DID/parse ^String a)
     :else (throw (IllegalArgumentException. (str "Can't get DID: " (class a)))))))

(defn random-did-string
  "Creates a random Ocean-compliant DID as a string, of the format:
  did:ocn:a1019172af9ae4d6cb32b52193cae1e3d61c0bcf36f0ba1cd30bf82d6e446563"
  (^String []
   (DID/createRandomString)))

(defn random-did
  "Creates a random Ocean-compliant DID of the format:
  did:ocn:a1019172af9ae4d6cb32b52193cae1e3d61c0bcf36f0ba1cd30bf82d6e446563"
  (^DID []
   (DID/createRandom)))

(defn valid-did?
  "Is this a valid DID?"
  [a]
  (or (did? a)
      (try
        (did a)
        true
        (catch Exception _
          false))))

(defn did-scheme
  "Return the DID scheme"
  (^String [a]
    (.getScheme (did a))))

(defn did-method
  "Return the DID method"
  (^String [a]
   (.getMethod (did a))))

(defn did-id
  "Return the DID ID"
  (^String [a]
   (.getID (did a))))

(defn did-path
  "Return the DID path"
  (^String [a]
   (.getPath (did a))))

(defn did-fragment
  "Return the DID fragment"
  (^String [a]
    (.getFragment (did a))))

(defn asset-id
  "Gets the Asset ID for an asset.

   The asset ID is meaningful mainly  in the context of an agent that has the asset registered. It is
   preferable to use (did asset) for the asset DID if the intent is to obtain a full reference to the asset
   that includes the agent location."
  ([^Asset a]
   (.getAssetID a)))

;; ============================================================
;; DDO management

(defn install-ddo 
  "Installs a DDO locally for an agent.

   DDO may be either a String or a Map, it will be coerced into a JSON String for installation."
  [did-value ddo]
  (let [resolver (Ocean/connect)
        ^String ddo-string (cond
                             (string? ddo) ddo 
                             (map? ddo) (json-string-pprint ddo)
                             :else (error "ddo value must be a String or Map"))
        did (did did-value)]
    (.installLocalDDO resolver did ddo-string)))

(defn ddo
  "Gets a DDO for the given DID as a String. Uses the default resolver if resolver is not specified."
  (^String [did-value]
   (ddo (Ocean/connect) did-value))
  (^String [^Ocean resolver did-value]
   (let [^DID d (did did-value)
         ddo-value (.getDDO resolver d)]
     (when ddo-value (json-string ddo-value)))))

(defn create-ddo
  "Creates a default DDO as a String for the given host address"
  (^String [host]
   (DDOUtil/getDDO host)))


;; =================================================
;; Account

(defn remote-account
  [username password]
  (RemoteAccount/create (Utils/createRandomHexString 32)
                        {"username" username
                         "password" password}))

;; =================================================
;; Operations

(defn create-operation
  "Create an in-memory operation with the given parameter list and function.

   The function provided should accept a map of inputs where each entry maps a keyword to either:
     a) A Starfish Asset
     b) A object representation of a JSON value as per read-json-string

   The function should return a similar map of outputs where each entry maps a keyword to either:
     a) A Starfish Asset
     b) A object representation of a JSON value as per read-json-string"
  ([params ^IFn f]
   (create-operation params f nil))
  ([params ^IFn f additional-metadata]
   (let [wrapped-fn (fn [params]
                      (f params))
         params (mapv name params)
         paramspec (reduce #(assoc %1 %2 {"type" "asset"}) {} params)
         meta {"name" "Unnamed Operation"
               "type" "operation"
               "dateCreated" (str (Instant/now))
               "params" paramspec}
         meta (merge meta (stringify-keys additional-metadata))]
     (ClojureOperation/create (json-string meta) (MemoryAgent/create) wrapped-fn ))))

(defn- format-params
  "Format parameters into a parameter map of string->asset according to the requirements of the operation."
  (^java.util.Map [operation params]
   (cond
     (map? params) params
     (asset? params) {:input params}
     :else (throw (IllegalArgumentException. (str "Params type not supported: " (class params)))))))

(defn invoke
  "Invoke an operation with the given parameters. Parameters may be either a positional list
   or a map of name / value pairs"
  (^Job [^Operation operation params]
   (let [params (format-params operation params)]
     (.invoke operation ^java.util.Map (stringify-keys params)))))

(defn invoke-result
  "Invoke an operation and wait for the result.

   An optional timeout may be provided."
  (^Asset [^Operation operation params]
   (let [job (invoke operation params)
         resp (.getResult job)]
     resp))
  (^Asset [^Operation operation params timeout]
   (let [job (invoke operation params)
         resp (.getResult job (long timeout))]
     resp)))


(defn invoke-sync
  "Invokes an operation synchronously, waiting to return the result."
  ([^Operation operation params]
    ;;convert from java Hashmap to Clojure map
   (into {} (.invokeResult operation params))))

(defn job-status 
  "Gets the status of a Job instance. Possible return values are defined by DEP6."
  ([^Job job]
    (throw (Error. "Not yet supported, requires squid-java interface extension")))) 

;; ==============================================================
;; Asset functionality

(defn asset
  "Coerces input data to an asset.
   - Existing assets are unchanged
   - DIDs are resolved to appropriate assets if possible
   - Strings and numbers are converted to memory assets containing the string representation
   - Map and Vector data structures are converted to JSON strings"
  (^Asset [data]
   (cond
     (asset? data) data
     (did? data) (get-asset (get-agent ^DID data))
     (string? data) (MemoryAsset/createFromString ^String data)
     (number? data) (MemoryAsset/createFromString (str data))
     (map? data) (json-string data)
     (vector? data) (json-string data)
     :else (TODO "Not yet supported: " (class data)))))

(defn memory-asset
  "Create an in-memory asset with the given metadata and raw data.

   If no metadata is supplied, default metadata is generated."
  (^Asset [data]
   (let [byte-data (to-bytes data)]
     (MemoryAsset/create byte-data)))
  (^Asset [meta data]
   (let [^java.util.Map meta-map (stringify-keys meta)
         byte-data (to-bytes data)]
     (MemoryAsset/create byte-data meta-map ))))

;; =======================================================
;; Agent functionality

(defn remote-agent
  "Gets a remote agent with the provided DID"
  ([did ddo username password]
   (RemoteAgentConfig/getRemoteAgent ddo did username password)))

(defn get-asset
  "Gets an asset from a remote agent, given as Asset ID as a string."

  ([^Agent agent ^String asset-id]
   (.getAsset agent asset-id)))

(defn get-agent
  "Gets a Ocean agent for the given DID"
  (^Agent [agent-did]
   (cond
     (agent? agent-did) agent-did
     (did? agent-did) (.getAgent *ocean* ^DID agent-did)
     :else (throw (IllegalArgumentException. (str "Invalid did: " (class agent-did)))))))

(defn digest
  "Computes the sha3_256 String hash of the byte representation of some data and returns this as a hex string.

  Handles
   - byte arrays - hashed as-is
   - Strings - converted to UTF-8 representation
   - Assets - compute the hash of asset metadata
  "
  (^String [data]
   (let [bytes (to-bytes data)]
     (Hash/sha3_256String bytes))))


(defn upload
  "Uploads any asset to an Agent. Returns an Asset referring to the uploaded Asset." 
  (^Asset [^Agent agent ^Asset asset]
   (.uploadAsset agent asset)))

(defn register
  "Registers an Asset with an Agent"
  (^Asset [^Agent agent ^Asset asset]
   (.registerAsset agent asset)))

(defn metadata
  "Gets the metadata for an asset as a Clojure map"
  ([^Asset asset]
   (let [md (.getMetadata asset)]
     (keywordize-keys (into {} md)))))

(defn content
  "Gets the content for a given asset as raw byte data"
  (^bytes [^Asset asset]
   (let []
     (.getContent asset))))

(defn content-stream
  "Gets the content for a given asset as an input stream."
  (^java.io.InputStream [^Asset asset]
   (let []
     (.getContentStream ^DataAsset asset))))

(defn publish-prov-metadata
  "Creates provenance metadata. If the first argument is a map with raw metadata, it adds a provenance
  section"
  ([^String activityId ^String agentId]
   (publish-prov-metadata {} activityId agentId))
  ([metadata ^String activityId ^String agentId]
   (merge metadata {"provenance" (ProvUtil/createPublishProvenance activityId agentId)})))

(defn invoke-prov-metadata
  ([^String activityId ^String agentId asset-dependencies ^String params ^String result-param-name]
   (invoke-prov-metadata {} activityId agentId asset-dependencies params result-param-name))
  ([metadata ^String activityId ^String agentId asset-dependencies ^String params ^String result-param-name]
   (merge metadata {"provenance" (ProvUtil/createInvokeProvenance activityId agentId
                                                                  asset-dependencies
                                                                  params result-param-name)})))

