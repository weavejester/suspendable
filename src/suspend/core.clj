(ns suspend.core
  (:require [com.stuartsierra.component :as component]))

(defprotocol Suspendable
  (suspend [component])
  (resume [component old-component]))

(defn suspend-or-stop [component]
  (if (satisfies? Suspendable component)
    (suspend component)
    (component/stop component)))

(defn suspend-or-stop-system
  ([system]
   (suspend-or-stop-system system (keys system)))
  ([system component-keys]
   (component/update-system-reverse system component-keys suspend-or-stop)))

(defn resume-or-start [component old-component]
  (if (and (satisfies? Suspendable component) old-component)
    (resume component old-component)
    (component/start component)))

(defn- assoc-key-metadata [system]
  (reduce (fn [s k] (update-in s [k] vary-meta assoc ::key k)) system (keys system)))

(defn resume-or-start-system
  ([system old-system]
   (resume-or-start-system system old-system (keys system)))
  ([system old-system component-keys]
   (component/update-system (assoc-key-metadata system)
                            component-keys
                            #(resume-or-start % (get old-system (::key (meta %)))))))
