(defproject suspendable "0.1.1"
  :description "A library that adds suspend and resume methods to Component"
  :url "https://github.com/weavejester/suspendable"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.stuartsierra/component "0.4.0"]]
  :profiles {:provided {:dependencies [[org.clojure/clojurescript "1.10.597"]]}}
  :aliases {"test-cljs" ["with-profile" "provided" "run" "-m" "cljs.main" "-re" "node" "-e"
                         "(require '[clojure.test :as test])
                          (require '[suspendable.core-test])
                          (test/run-tests 'suspendable.core-test)"]})
