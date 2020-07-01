(ns suspendable.core
  "A protocol and methods for allowing connections and other stateful objects
  to persist in components across resets."
  (:require [com.stuartsierra.component :as component]
            [clojure.set :as set]))

(defprotocol Suspendable
  :extend-via-metadata true
  (suspend [component]
    "Suspend a component and return the suspended component. Expected to be
    called before a resume.")
  (resume [component old-component]
    "Given a new component and a suspended old component, return a component
    that incorporates the suspended data into the new component, if possible."))

(defn suspend-system
  "Suspend or stop every component in a system. Returns the suspended system."
  ([system]
   (suspend-system system (keys system)))
  ([system component-keys]
   (component/update-system-reverse system component-keys suspend)))

(defn- missing-keys [old-system new-system]
  (set/difference (set (keys old-system)) (set (keys new-system))))

(defn- stop-missing-components [old-system new-system]
  (component/update-system old-system (missing-keys old-system new-system) component/stop))

(defn- maybe-vary-meta [obj f & args]
  (if #?(:clj (instance? clojure.lang.IObj obj)
         :cljs (satisfies? IMeta obj))
    (apply vary-meta obj f args)
    obj))

(defn- assoc-component-key [system key]
  (update-in system [key] maybe-vary-meta assoc ::key key))

(defn- resume-system-component [old-system component]
  (let [key (-> component meta ::key)]
    (if-let [old-component (get old-system key)]
      (resume component old-component)
      (component/start component))))

(defn resume-system
  "Resume components in a new system using components from a previously
  suspended system. Components that are not suspendable or that are missing from
  the previous system are started instead. Returns the resumed system."
  ([system old-system]
   (resume-system system old-system (keys system)))
  ([system old-system component-keys]
   (let [old-system (stop-missing-components old-system system)]
     (component/update-system (reduce assoc-component-key system component-keys)
                              component-keys
                              (partial resume-system-component old-system)))))

(extend-protocol Suspendable
  #?(:clj java.lang.Object :cljs default)
  (suspend [component] (component/stop component))
  (resume [component _] (component/start component))
  com.stuartsierra.component.SystemMap
  (suspend [system] (suspend-system system))
  (resume [system old-system] (resume-system system old-system)))
