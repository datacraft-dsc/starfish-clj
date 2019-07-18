# starfish-clj

[![Build Status](https://travis-ci.com/DEX-Company/starfish-clj.svg?token=g26KMSqk9yTWH8J1QLSN&branch=master)](https://travis-ci.com/DEX-Company/starfish-clj) [![Clojars Project](https://img.shields.io/clojars/v/sg.dex/starfish-clj.svg)](https://clojars.org/sg.dex/starfish-clj)

Toolkit for Decentralised Data Ecosystem development Clojure

## Table of Contents

* [Overview](#overview)
* [Installation](#installation)
* [Configuration](#configuration)
* [Documentation](#documentation)
* [Testing](#testing)
* [License](#license)

# Overview

Starfish-clj is an open-sourced developer toolkit for the data economy. It allows developers, data scientists and enterprises to create, interact, integrate and manage a data supply line through standardised and simple-to-use APIs.

Based on an underlying data ecosystem standard, Starfish provides high-level APIs for common tasks within the data economy, for example, registering/publishing an asset, for subsequent use in a data supply line. In this case, an asset can be any data set, model or data service. The high-level API also allows developers to invoke operation on an asset, e.g. computing a predictive model or anonymising sensitive personal information, among other capabilities. 

Starfish works with blockchain networks, such as Ocean Protocol, and common web services through agents, allowing unprecedented flexibility in asset discovery and data supply line management. 

# Installation

Add a dependency in your build tool for [![Clojars Project](https://img.shields.io/clojars/v/sg.dex/starfish-clj.svg)](https://clojars.org/sg.dex/starfish-clj)

# Configuration

Here's an [example of a configuration file](https://github.com/DEX-Company/starfish-clj/blob/master/src/test/resources/squid_test.properties).

# Documentation 

TBD

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
