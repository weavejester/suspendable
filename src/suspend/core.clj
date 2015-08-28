(ns suspend.core
  (:require [com.stuartsierra.component :as component]))

(defprotocol Suspendable
  (suspend [component]))

(defn suspend-or-stop [component]
  (if (satisfies? Suspendable component)
    (suspend component)
    (component/stop component)))

(defn suspend-or-stop-system
  ([system]
   (suspend-or-stop-system system (keys system)))
  ([system component-keys]
   (component/update-system-reverse system component-keys #'suspend-or-stop)))
