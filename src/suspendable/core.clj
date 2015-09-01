(ns suspendable.core
  (:require [com.stuartsierra.component :as component]
            [clojure.set :as set]))

(defprotocol Suspendable
  (suspend [component])
  (resume [component old-component]))

(defn suspend-system
  ([system]
   (suspend-system system (keys system)))
  ([system component-keys]
   (component/update-system-reverse system component-keys suspend)))

(defn- missing-keys [old-system new-system]
  (set/difference (set (keys old-system)) (set (keys new-system))))

(defn- stop-missing-components [old-system new-system]
  (component/update-system old-system (missing-keys old-system new-system) component/stop))

(defn- assoc-component-key [system key]
  (update-in system [key] vary-meta assoc ::key key))

(defn- resume-system-component [old-system component]
  (let [key (-> component meta ::key)]
    (if-let [old-component (get old-system key)]
      (resume component old-component)
      (component/start component))))

(defn resume-system
  ([system old-system]
   (resume-system system old-system (keys system)))
  ([system old-system component-keys]
   (let [old-system (stop-missing-components old-system system)]
     (component/update-system (reduce assoc-component-key system component-keys)
                              component-keys
                              (partial resume-system-component old-system)))))

(extend-protocol Suspendable
  java.lang.Object
  (suspend [component] (component/stop component))
  (resume [component _] (component/start component))
  com.stuartsierra.component.SystemMap
  (suspend [system] (suspend-system system))
  (resume [system old-system] (resume-system system old-system)))
