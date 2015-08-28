(ns suspend.core-test
  (:require [clojure.test :refer :all]
            [suspend.core :refer :all]
            [com.stuartsierra.component :as component]))

(defrecord PlainComponent []
  component/Lifecycle
  (start [component] (assoc component :state :started))
  (stop  [component] (assoc component :state :stopped)))

(defrecord SuspendableComponent []
  component/Lifecycle
  (start [component] (assoc component :state :started))
  (stop  [component] (assoc component :state :stopped))
  Suspendable
  (suspend [component] (assoc component :state :suspended)))

(deftest test-suspend-or-stop
  (testing "stop"
    (let [component (->PlainComponent)]
      (is (= (-> component component/start suspend-or-stop :state) :stopped))))
  (testing "suspend"
    (let [component (->SuspendableComponent)]
      (is (= (-> component component/start suspend-or-stop :state) :suspended)))))

(deftest test-suspend-or-stop-system
  (let [system (suspend-or-stop-system
                (component/start
                 (component/system-map
                  :plain (->PlainComponent)
                  :suspendable (->SuspendableComponent))))]
    (is (= (-> system :plain :state) :stopped))
    (is (= (-> system :suspendable :state) :suspended))))
