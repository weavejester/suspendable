(ns suspendable.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [suspendable.core :as core]
            [com.stuartsierra.component :as component]))

(defrecord PlainComponent []
  component/Lifecycle
  (start [component] (assoc component :state :started))
  (stop  [component] (assoc component :state :stopped)))

(defrecord SuspendableComponent []
  component/Lifecycle
  (start [component] (assoc component :state :started))
  (stop  [component] (assoc component :state :stopped))
  core/Suspendable
  (suspend [component]   (assoc component :state :suspended))
  (resume  [component _] (assoc component :state :resumed)))

(defrecord StatefulComponent [state]
  component/Lifecycle
  (start [component] (reset! state :started) component)
  (stop [component] (reset! state :stopped) component))

(deftest test-default-suspend
  (is (= (-> (->PlainComponent) core/suspend :state) :stopped)))

(deftest test-default-resume
  (let [component (->PlainComponent)]
    (is (= (-> component (core/resume component) :state) :started))))

(deftest test-components-that-arent-components
  (let [system (component/start
                (component/system-map
                 :long 1
                 :string "string"
                 :false false))]
    (is (core/suspend-system system))
    (is (core/resume-system system system))))

(deftest test-suspend-system
  (let [system (core/suspend-system
                (component/start
                 (component/system-map
                  :plain (->PlainComponent)
                  :suspendable (->SuspendableComponent))))]
    (is (= (-> system :plain :state) :stopped))
    (is (= (-> system :suspendable :state) :suspended))))

(deftest test-resume-system
  (let [system (component/system-map
                :plain       (->PlainComponent)
                :suspendable (->SuspendableComponent)
                :stateful    (->StatefulComponent (atom nil)))]
    (testing "previous system"
      (let [system (core/resume-system system system)]
        (is (= (-> system :plain :state) :started))
        (is (= (-> system :suspendable :state) :resumed))))
    (testing "no previous system"
      (let [system (core/resume-system system nil)]
        (is (= (-> system :plain :state) :started))
        (is (= (-> system :suspendable :state) :started))))
    (testing "new component key"
      (let [system (core/resume-system system (dissoc system :suspendable))]
        (is (= (-> system :plain :state) :started))
        (is (= (-> system :suspendable :state) :started))))
    (testing "missing components are stopped"
      (let [system (component/start system)]
        (is (= (-> system :stateful :state deref) :started))
        (core/resume-system (dissoc system :stateful) system)
        (is (= (-> system :stateful :state deref) :stopped))))))
