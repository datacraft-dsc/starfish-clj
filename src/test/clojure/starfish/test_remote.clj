(ns starfish.test-remote
  (:require [clojure.test :refer :all]
            [starfish.core :as sf])
  (:import (sg.dex.starfish.impl.remote RemoteAgent)
           (sg.dex.starfish.impl.memory LocalResolverImpl)))

(deftest remote-agent-test
  (testing "New Remote Agent"
    (binding [sf/*resolver* (LocalResolverImpl.)]
      (testing "Missing DID"
        (let [ex (try
                   (sf/remote-agent nil)
                   (catch Exception ex
                     ex))]
          (is (= "Can't get DID: " (ex-message ex)))
          (is (instance? IllegalArgumentException ex))))

      (testing "Invalid DID"
        (let [ex (try
                   (sf/remote-agent {:did "foo"})
                   (catch Exception ex
                     ex))]
          (is (= "Parse failure on invalid DID [foo]" (ex-message ex)))
          (is (instance? IllegalArgumentException ex))))

      (testing "Minimal"
        (let [did (sf/random-did-string)
              remote-agent (sf/remote-agent {:did did})]
          (is (instance? RemoteAgent remote-agent))
          (is (= did (str (sf/did remote-agent))))
          (is (= (sf/did did) (sf/did remote-agent)))
          (is (= nil (sf/ddo did)))
          (is (= nil (sf/ddo-string did)))))

      (testing "Minimal with Account"
        (is (= {"username" "foo"
                "password" "bar"}
               (-> (sf/remote-agent {:did (sf/random-did)
                                     :account (sf/remote-account "foo" "bar")})
                   (.getAccount)
                   (.getUserDataMap))))

        (is (= {"username" "foo"
                "password" "bar"}
               (-> (sf/remote-agent {:did (sf/random-did)
                                     :username "foo"
                                     :password "bar"})
                   (.getAccount)
                   (.getUserDataMap))))))))
