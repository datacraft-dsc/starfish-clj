# starfish-clj

[![Build Status](https://travis-ci.com/DEX-Company/starfish-clj.svg?token=g26KMSqk9yTWH8J1QLSN&branch=master)](https://travis-ci.com/DEX-Company/starfish-clj) [![Clojars Project](https://img.shields.io/clojars/v/sg.dex/starfish-clj.svg)](https://clojars.org/sg.dex/starfish-clj)

Ocean protocol developer toolkit for Clojure


## Running tests

- `lein test` runs the unit tests
- `lein test :integration` runs the integration test, which needs
  - Surfer to be up and running 
  - the host/port for Surfer should be changed in a config file (TBD)
