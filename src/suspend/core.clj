(ns suspend.core
  (:require [com.stuartsierra.component :as component])
  (:import [com.stuartsierra.component SystemMap]))

(defprotocol Suspendable
  (suspend [component])
  (resume [component old-component]))

(defn suspend-system
  ([system]
   (suspend-system system (keys system)))
  ([system component-keys]
   (component/update-system-reverse system component-keys suspend)))

(defn resume-system
  ([system old-system]
   (resume-system system old-system (keys system)))
  ([system old-system component-keys]
   (component/update-system
    (reduce (fn [s k] (update-in s [k] vary-meta assoc ::key k)) system component-keys)
    component-keys
    (fn [c] (resume c (get old-system (::key (meta c))))))))

(extend-protocol Suspendable
  Object
  (suspend [component] (component/stop component))
  (resume [component _] (component/start component))
  SystemMap
  (suspend [system] (suspend-system system))
  (resume [system old-system] (resume-system system old-system)))
