(ns starfish.core
  (:require [clojure.walk :refer [keywordize-keys stringify-keys]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [starfish.utils :refer [error TODO]]
            [clojure.string :as str])
  (:import [java.nio.charset
            StandardCharsets]
           [java.io InputStream File]
           [java.time
            Instant]
           [java.lang IllegalArgumentException]
           [clojure.lang
            IFn]
           [sg.dex.crypto
            Hash]
           [sg.dex.starfish.util
            DID Hex Utils RemoteAgentConfig ProvUtil DDOUtil JSON]
           [sg.dex.starfish
            Asset DataAsset Invokable Agent Job Listing Resolver Operation Purchase]
           [sg.dex.starfish.impl.memory
            MemoryAsset ClojureOperation MemoryAgent]
           [sg.dex.starfish.impl.file
            FileAsset]
           [sg.dex.starfish.impl.remote
            RemoteAgent RemoteAccount]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def BYTE-ARRAY-CLASS (Class/forName "[B"))

(def ^{:dynamic true :private true} *registry*
  (atom {}))

(def ^{:dynamic true :tag Resolver} *resolver*
  (sg.dex.starfish.impl.memory.LocalResolverImpl.))

(declare asset-content asset? get-asset)

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

(defn job?
  "Returns true if the argument is a Job"
  ([a]
   (instance? Job a)))

(defprotocol IDid
  (did [x]))

(extend-protocol IDid
  DID
  (did [x]
    x)

  String
  (did [x]
    (DID/parse x))

  Agent
  (did [x]
    (.getDID x))

  Asset
  (did [x]
    (.getDID x)))

;;===================================
;; Utility functions, coercion etc.

(defn- json-key-fn
  "Convert to string function - suitable for producing JSON keys from Clojure symbols and keywords"
  ^String [k]
  (cond
    (symbol? k) (name k)
    (keyword? k) (subs (str k) 1)                           ;; do NOT interpret / for namespaced keywords
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
     (asset? data) (.getContent ^Asset data)
     :else (error "Can't convert to bytes: " (class data)))))

(defn to-string
  "Coerces data to a string format."
  (^String [data]
   (cond
     (bytes? data) (String. ^bytes data StandardCharsets/UTF_8)
     (string? data) data
     (asset? data) (to-string (asset-content data))
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

(defn dido'
  "Gets the DID for the given input
   - DID is returned for Agents or Assets
   - Strings are intrepreted as DIDs if possible"
  (^DID [x]
   (cond
     (did? x) x
     (asset? x) (.getDID ^Asset x)
     (agent? x) (.getDID ^Agent x)
     (string? x) (DID/parse ^String x)
     :else (throw (IllegalArgumentException. (str "Can't get DID: " (class x)))))))

(defn dido ^DID [x]
  (try
    (dido' x)
    (catch Exception _
      nil)))

(defn didable?
  "Returns true if x can be sucessfully coerced to a DID, false otherwise."
  [x]
  (boolean (dido x)))

(defn random-did-string
  "Creates a random DEP-compliant DID as a string, of the format:
  did:xxx:a1019172af9ae4d6cb32b52193cae1e3d61c0bcf36f0ba1cd30bf82d6e446563"
  (^String []
   (DID/createRandomString)))

(defn random-did
  "Creates a random DEP-compliant DID of the format:
  did:xxx:a1019172af9ae4d6cb32b52193cae1e3d61c0bcf36f0ba1cd30bf82d6e446563"
  (^DID []
   (DID/createRandom)))

(defn did-scheme
  "Return the DID scheme."
  (^String [x]
   (when-let [d (dido x)]
     (.getScheme d))))

(defn did-method
  "Return the DID method."
  (^String [x]
   (when-let [d (dido x)]
     (.getMethod d))))

(defn did-id
  "Return the DID ID. In standard Starfish usage, this is the ID of the Agent."
  (^String [x]
   (when-let [d (dido x)]
     (.getID d))))

(defn did-path
  "Return the DID path. In standard Starfish usage, this is equivalent to the Asset ID."
  (^String [x]
   (when-let [d (dido x)]
     (.getPath d))))

(defn did-fragment
  "Return the DID fragment"
  (^String [x]
   (when-let [d (dido x)]
     (.getFragment d))))

;; ============================================================
;; DDO management

(defn ddo-string
  "Gets a DDO for the given DID as a JSON formatted String. Uses the default resolver if resolver is not specified."
  (^String [did]
   (ddo-string *resolver* did))
  (^String [^Resolver resolver did]
   (let [^DID did (dido did)]
     (.getDDOString resolver did))))

(defn ddo-map
  "Gets a DDO for the given DID as a map. Uses the default resolver if resolver is not specified."
  ([did]
   (ddo-map *resolver* did))
  ([resolver did]
   (if-let [ddos (ddo-string resolver did)]
     (read-json-string ddos))))

(defn create-ddo
  "Creates a default DDO as a String for the given host address"
  (^String [host]
   (DDOUtil/getDDO host)))

;; =================================================
;; Account

(defn remote-account
  ([token]
   (let [^java.util.Map credentials (doto (java.util.HashMap.)
                                      (.put "token" token))]
     (RemoteAccount/create (Utils/createRandomHexString 32) credentials)))
  ([username password]
   (let [^java.util.Map credentials (doto (java.util.HashMap.)
                                      (.put "username" username)
                                      (.put "password" password))]
     (RemoteAccount/create (Utils/createRandomHexString 32) credentials))))

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
               "operation" {"modes" ["sync" "async"]
                            "params" paramspec}}
         meta (merge meta (stringify-keys additional-metadata))]
     (ClojureOperation/create (json-string meta) (MemoryAgent/create) wrapped-fn))))

(defn invokable-metadata
  "Returns an Operation Metadata map.

   `obj` *must* be a Var, and its value *must* be a function.

   Params are extracted from `obj` metadata, but you can pass an option map
   with `params` and `results` to be used instead.

   DEP 8 - Asset Metadata
   https://github.com/DEX-Company/DEPs/tree/master/8"
  [obj & [{:keys [params results]}]]
  (let [metadata (meta obj)

        params (or params (reduce
                            (fn [params arg]
                              (let [arg (name arg)]
                                (assoc params arg {"type" "json"})))
                            {}
                            ;; Take the first; ignore other arities.
                            (first (:arglists metadata))))]
    {:name (or (:doc metadata) "Unnamed Operation")
     :type "operation"
     :dateCreated (str (Instant/now))
     :additionalInfo {:function (-> obj symbol str)}
     :operation (let [m {"modes" ["sync" "async"]
                         "params" (stringify-keys params)}]
                  (if results
                    (merge m {"results" (stringify-keys results)})
                    m))}))

(defn in-memory-operation
  "Make an in-memory operation from the metadata map."
  [metadata]
  (let [f (-> (get-in metadata [:additionalInfo :function]) symbol resolve)]
    (ClojureOperation/create (json-string metadata) (MemoryAgent/create) f)))

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
  ([^Operation operation params]
   (let [job (invoke operation params)
         resp (.getResult job)]
     (into {} (keywordize-keys resp))))
  ([^Operation operation params timeout]
   (let [job (invoke operation params)
         resp (.getResult job (long timeout))]
     (into {} (keywordize-keys resp)))))

(defn invoke-sync
  "Invokes an operation synchronously, waiting to return the result."
  ([^Operation operation params]
   ;;convert from java Hashmap to Clojure map
   (let [params (format-params operation params)]
     (keywordize-keys (.invokeResult operation (stringify-keys params))))))

(defn job-status
  "Gets the status of a Job instance as a keyword. 

   Possible return values are defined by DEP6."
  ([^Job job]
   (keyword (.getStatus job))))

(defn job-id
  "Gets the ID of a Job instance."
  ([^Job job]
   (.getJobID job)))

(defn get-result
  "Gets the results of a job, as a map of keywords to assets / values.

   Blocks until results are ready"
  [^Job job]
  (let [res (.getResult job)]
    (keywordize-keys res)))

(defn poll-result
  "Pools the results of a job, returning a map of keywords to assets / values if succeeded.

   Returns null if results are not yet available"
  [^Job job]
  (let [res (.pollResult job)]
    (keywordize-keys res)))

;; ==============================================================
;; Asset functionality

(defn asset-id'
  "Gets the Asset ID for an asset or DID as a String.

   The asset ID is meaningful mainly  in the context of an Agent that has the Asset registered. It is
   preferable to use (did asset) for the asset DID if the intent is to obtain a full reference to the asset
   that includes the agent location."
  (^String [a]
   (cond
     (asset? a) (.getAssetID ^Asset a)
     (did? a) (or (did-path ^DID a) (error "DID does not contain an Asset ID in DID path"))
     (string? a) (asset-id' (dido' a))
     (nil? a) (error "Can't get Asset ID of null value")
     :else (error "Can't get asset ID of type " (class a)))))

(defn asset-id [x]
  (try
    (asset-id' x)
    (catch Error _
      nil)))

(defn asset-metadata
  "Gets the metadata for an Asset as a map."
  ([^Asset asset]
   (keywordize-keys (into {} (.getMetadata asset)))))

(defn asset-metadata-string
  "Gets the metadata for an Asset as a String. This is guaranteed to match the
   precise metadata used for the calculation of the Asset ID."
  (^String [^Asset asset]
   (.getMetadataString asset)))

(defn asset-content
  "Gets the content for a given Asset as raw byte data"
  (^bytes [^Asset asset]
   (.getContent asset)))

(defn asset-content-stream
  "Gets the content for a given data asset as an input stream."
  (^java.io.InputStream [^DataAsset asset]
   (.getContentStream asset)))

(defn memory-asset
  "Create an in-memory asset with the given metadata and data.

   If no metadata is supplied, default metadata is generated."
  (^Asset [data]
   (MemoryAsset/create (to-bytes data)))
  (^Asset [meta data]
   (let [byte-data (to-bytes data)]
     (if (string? meta)
       (MemoryAsset/create byte-data ^String meta)
       (let [^java.util.Map meta-map (stringify-keys meta)]
         (MemoryAsset/create byte-data meta-map))))))

(defn file-asset
  "Create a file asset with the given metadata and source file.

   If no metadata is supplied, default metadata is generated."
  (^Asset [file]
   (let [^File file (io/file file)]
     (FileAsset/create file)))
  (^Asset [meta file]
   (let [^File file (io/file file)]
     (if (string? meta)
       (FileAsset/create file ^String meta)
       (let [^java.util.Map meta-map (stringify-keys meta)]
         (FileAsset/create file meta-map))))))

(defn get-asset
  "Gets Asset from an Agent.

   `id` can be either a String or DID; it will be coarced to DID in case it's a String."
  [agent id]
  (.getAsset ^Agent agent ^String (asset-id id)))

;; =======================================================
;; Agent functionality

(defn remote-agent
  "Gets a remote agent with the provided DID"
  ([did account]
   (remote-agent *resolver* did account))
  ([resolver did account]
   (RemoteAgent/create resolver (dido did) ^RemoteAccount account)))

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
  "Uploads any asset to an Agent. Registers the asset with the Agent if required.

   Returns an Asset instance referring to the uploaded remote Asset."
  (^Asset [^Agent agent ^Asset asset]
   (.uploadAsset agent asset)))

(defn register
  "Registers an Asset with an Agent. Registration stores the metadata of the asset with the Agent, 
   but does not upload any data.

   Returns an asset associated with the agent if successful."
  (^Asset [^Agent agent ^Asset asset]
   (.registerAsset agent asset)))

(defn publish-prov-metadata
  "Creates provenance metadata. If the first argument is a map with raw metadata, it adds a provenance
  section"
  ([^String activityId ^String agentId]
   (publish-prov-metadata {} activityId agentId))
  ([asset-metadata ^String activityId ^String agentId]
   (merge asset-metadata {"provenance" (ProvUtil/createPublishProvenance activityId agentId)})))

(defn invoke-prov-metadata
  ([^String activityId ^String agentId asset-dependencies ^String params ^String result-param-name]
   (invoke-prov-metadata {} activityId agentId asset-dependencies params result-param-name))
  ([asset-metadata ^String activityId ^String agentId asset-dependencies ^String params ^String result-param-name]
   (merge asset-metadata {"provenance" (ProvUtil/createInvokeProvenance activityId agentId
                                                                        asset-dependencies
                                                                        params result-param-name)})))

(defn install
  "Configures local resolver and registry."
  [did ddo make]
  (.registerDID *resolver* (dido did) (json/write-str ddo))
  (swap! *registry* #(assoc % (did-id did) make))
  nil)

(defn get-agent
  (^Agent [did]
   (get-agent *resolver* *registry* did))
  (^Agent [resolver registry did]
   (when-let [ddo-str (ddo-string resolver did)]
     (let [ddo (json/read-str ddo-str :key-fn str)
           make (@registry (did-id did))]
       (make resolver (dido did) ddo)))))