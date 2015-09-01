(ns suspendable.core
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
    (fn [component]
      (let [key (-> component meta ::key)]
        (if-let [old-component (get old-system key)]
          (resume component old-component)
          (component/start component)))))))

(extend-protocol Suspendable
  Object
  (suspend [component] (component/stop component))
  (resume [component _] (component/start component))
  SystemMap
  (suspend [system] (suspend-system system))
  (resume [system old-system] (resume-system system old-system)))
