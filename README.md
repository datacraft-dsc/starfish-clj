# starfish-clj

[![Actions Status](https://github.com/DEX-Company/starfish-clj/workflows/CI/badge.svg)](https://github.com/DEX-Company/starfish-clj/actions) [![Clojars Project](https://img.shields.io/clojars/v/sg.dex/starfish-clj.svg)](https://clojars.org/sg.dex/starfish-clj)

Toolkit for Decentralised Data Ecosystem development in Clojure

## Table of Contents

* [Overview](#overview)
* [Installation](#installation)
* [Configuration](#configuration)
* [Example](#example)
* [Testing](#testing)
* [License](#license)

# Overview

Starfish-clj is an open source developer toolkit for the data economy. It allows developers, data scientists and enterprises to create, interact, integrate and manage decentralised data supply lines through standardised and simple-to-use APIs.

Based on an underlying data ecosystem standard, Starfish provides high-level APIs for common tasks within the data economy, for example, registering/publishing an asset, for subsequent use in a data supply line. In this case, an asset can be any data set, model or data service. The high-level API also allows developers to invoke operation on an asset, e.g. computing a predictive model or anonymising sensitive personal information, among other capabilities. 

Starfish works with blockchain networks, such as Ocean Protocol, and common web services through agents, allowing unprecedented flexibility in asset discovery and data supply line management. 

# Installation

Add a dependency in your build tool for [![Clojars Project](https://img.shields.io/clojars/v/sg.dex/starfish-clj.svg)](https://clojars.org/sg.dex/starfish-clj)

# Configuration

Here's an [example of a configuration file](https://github.com/DEX-Company/starfish-clj/blob/master/src/test/resources/squid_test.properties).

# Example 

Starfish-clj is a thin wrapper on top of Starfish-java. 
Here's an example of creating and managing assets using starfish-clj
```clj
  ;;define a memory asset
  (def as1 (memory-asset             ;; type of asset to construct
             {:name "My Asset"}      ;; metadata
             "This is a test")       ;; content (as a String))
    )
    
  ;; display the metadata
  
  (s/metadata as1)
  ;;{:dateCreated "2019-07-24T08:02:52.738504Z", :size "14", :name "My Asset", :type "dataset", :contentType "application/octet-stream", :contentHash "93b90fab55adf4e98787d33a38e71106e8c016f1a124dfc784f3cca4d938b1af"}

  ;; validate the content hash
  (digest "This is a test")
  ;;"93b90fab55adf4e98787d33a38e71106e8c016f1a124dfc784f3cca4d938b1af"

  ;; Print the content
  (to-string (content as1))
  ;;"This is a test"

  ;; ======================================================================================
  ;; USING REMOTE AGENTS
  ;; Agents are remote services providing asset and capabilities to the Ocean ecosystem
  (def my-agent (let [did (random-did)
                      ddostring (create-ddo "http://52.187.164.74:8080/")]
                  (remote-agent did ddostring "Aladdin" "OpenSesame")))
  

  ;; agents have a DID
  (str (did my-agent))
  ;;"did:op:d394d2e1a211ba61f6f7543bd36df59994a5cb99e7d863405117b4c42c5cb2e9"

  ;; Get an asset
  (def as2 (get-asset my-agent "10bc529b730b9372689af7c8848256c75b61e1c25addc0dc100059dcceb05d03"))

  ;; assets also have a DID, starting with the DID of the agent
  (str (did as2))
  ;;"did:op:d394d2e1a211ba61f6f7543bd36df59994a5cb99e7d863405117b4c42c5cb2e9/10bc529b730b9372689af7c8848256c75b61e1c25addc0dc100059dcceb05d03"
 
  ;; print the content of asset data, which happens to be JSON
  (json/read-str (s/to-string (content as2)))
  ;;{"age_derived" "12", "ownerManual" "", "age_score" "2"}


```

# Testing

- `lein test` runs the unit tests
- `lein test :integration` runs the integration test

# License

```
Copyright 2018-2019 DEX Pte. Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
