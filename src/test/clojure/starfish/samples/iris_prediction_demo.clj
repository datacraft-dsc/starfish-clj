(ns starfish.samples.iris-prediction-demo
  (:use [starfish.core :refer :all]
        [clojure.walk :refer [keywordize-keys stringify-keys]]
        )
  (:require [clojure.pprint :refer [pprint]]
            [clojure.data.json :as json :exclude [pprint]]))

(def ddo-surfer-koi
  {"service" [{"type" "Ocean.Meta.v1", "serviceEndpoint" "http://localhost:8080/api/v1/meta"}
              {"type" "Ocean.Storage.v1", "serviceEndpoint" "http://localhost:8080/api/v1/assets"}
              {"type" "Ocean.Invoke.v1", "serviceEndpoint" "http://localhost:3000/api/v1"}
              {"type" "Ocean.Auth.v1", "serviceEndpoint" "http://localhost:8080/api/v1/auth"}
              {"type" "Ocean.Market.v1", "serviceEndpoint" "http://localhost:8080/api/v1/market"}]})

;;wrapping in a fn to not execute it
(comment
  ;;the iris dataset path
  (def iris-dataset "https://gist.githubusercontent.com/curran/a08a1080b88344b0c8a7/raw/d546eaee765268bf2f487608c537c05e22e4b221/iris.csv")

  ;;create a remote agent with the  right DDO and auth.
  (def rema
    (->> (remote-account "Aladdin" "OpenSesame")
         (remote-agent (random-did))))

  ;;create the iris asset locally
  (def iris-mem-asset (resolve-asset (memory-asset {"description" "iris dataset"} (slurp iris-dataset))))
  ;; register it
  (def iris-asset (->> iris-mem-asset (register rema)))
  ;;upload the contents
  (upload rema iris-mem-asset)


  ;;create the prediction operation, given the asset or operation ID.
  (def prediction-op (get-asset rema "ed6c4f7f5d7a361fc87d68f8ec7a06168460b8e7b720b97315696ace74bdd871"))

  ;;invoke the operation, giving iris dataset as the input to the operation
  (def pred-resp (invoke-sync prediction-op {"dataset" iris-asset}))

  ;;check what is returned by invoke, it is a map with parameter names and values.
  (keys pred-resp)
  ;;("predictions")

  ;;the operation returns its predictions
  (def predictions-asset (get pred-resp "predictions"))
  (def prediction-data (-> predictions-asset
                           ;;get the content from the asset
                           asset-content
                           ;;convert to a string
                           to-string
                           ;;split by newline to view each prediction
                           (clojure.string/split #"\n")))

  ;;check the first row
  (first prediction-data)
  ;;"sepal_length,sepal_width,petal_length,petal_width,species,predclass"

  ;;note that the predicted class "predclass" is added as the last column
  ;;view the first 5 rows
  (take 5 prediction-data)
  ;;("sepal_length,sepal_width,petal_length,petal_width,species,predclass" "5.1,3.5,1.4,0.2,setosa,setosa" "4.9,3.0,1.4,0.2,setosa,setosa" "4.7,3.2,1.3,0.2,setosa,setosa" "4.6,3.1,1.5,0.2,setosa,setosa")
  ;;note that the predicted class in all cases is 'setosa'


  ;;view the metadata with added provenance
  (def pred-meta (asset-metadata predictions-asset))

  ;;view the provenance section
  (-> pred-meta :provenance)
  ;;{"agent" {"opf:did:op:06dedd87b7e9189013919d435553678d779b5d643c0d0587b66c784b51b219c8" {"prov:type" {"$" "opf:ACCOUNT", "type" "xsd:string"}}}, "wasGeneratedBy" {"_:8577167c-56b1-4fcd-b3a1-465bced633b2" {"prov:entity" "opf:this", "prov:activity" "0a3466b1-b106-4002-987d-227472b086e0"}}, "activity" {"opf:0a3466b1-b106-4002-987d-227472b086e0" {"prov:type" {"$" "opf:OPERATION", "type" "xsd:string"}, "opf:resultParamName" {"$" "predictions", "type" "xsd:string"}, "opf:params" {"$" "{\":dataset\":{\":did\":\"18dbcbe85ac83574e87b54909599c70a8ad89808d28c9e143080ae32b625f21b\"}}", "type" "xsd:string"}}}, "prefix" {"opf" "http://oceanprotocol.com/schemas", "xsd" "http://www.w3.org/2001/XMLSchema#", "prov" "http://www.w3.org/ns/prov#"}, "wasAssociatedWith" {"_:fdec4f90-997d-407e-b0d9-f8cd8d8c17a1" {"prov:agent" "did:op:06dedd87b7e9189013919d435553678d779b5d643c0d0587b66c784b51b219c8", "prov:activity" "0a3466b1-b106-4002-987d-227472b086e0"}}, "wasDerivedFrom" {"_:8fa37851-873e-40f4-90e8-ab649ccc847c" {"prov:generatedEntity" "opfthis", "prov:usedEntity" "18dbcbe85ac83574e87b54909599c70a8ad89808d28c9e143080ae32b625f21b"}}, "entity" {"opf:this" {"prov:type" {"$" "opf:asset", "type" "xsd:string"}}, "opf:18dbcbe85ac83574e87b54909599c70a8ad89808d28c9e143080ae32b625f21b" {"prov:type" {"$" "opf:asset", "type" "xsd:string"}}}}

  ;;view the activity section
  (get-in (-> pred-meta :provenance) ["activity"])
  ;;{"opf:0a3466b1-b106-4002-987d-227472b086e0"
  ;;{"prov:type" {"$" "opf:OPERATION", "type" "xsd:string"},
  ;; "opf:resultParamName" {"$" "predictions", "type" "xsd:string"},
  ;; "opf:params" {"$" "{\":dataset\":{\":did\":\"18dbcbe85ac83574e87b54909599c70a8ad89808d28c9e143080ae32b625f21b\"}}", "type" "xsd:string"}}}

  ;;note that it is an OPERATION that generates it,
  ;;and the parameters record the did of the iris-dataset (which is the input)



  )
