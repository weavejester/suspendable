# Suspendable

[![Build Status](https://travis-ci.org/weavejester/suspendable.svg?branch=master)](https://travis-ci.org/weavejester/suspendable)

A very small library that adds `suspend` and `resume` methods to
[Component][].

[component]: https://github.com/stuartsierra/component

## Rationale

In the [reloaded workflow][], each component is stopped completely
before the codebase is refreshed. One problem with this approach is
that it doesn't allow for connections held by components to persist
during development.

Ideally we want a way of carrying open connections across a refresh of
the codebase. This library introduces a new protocol, `Suspendable`,
that adds two new methods, `suspend` and `resume`, for facilitating a
"soft reset" of a system.

[reloaded workflow]: http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded

## Installation

To install, add the following to your project `:dependencies`:

    [suspendable "0.1.1"]

## Usage

Essentially we want to replace uses of `go` with `resume`, and `stop`
with `suspend`, in functions that are used to handle a reset.

```clojure
(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [suspendable.core :refer [Suspendable suspend resume]]
            [your.app.system :refer [create-your-system]))

(defn suspend []
  (alter-var-root #'system suspend))

(defn resume []
  (alter-var-root #'system (partial resume (create-your-system))))

(defn reset []
  (suspend)
  (refresh :after 'user/resume))
```

If a component does not explicitly satisfy `Suspendable`, it will be
stopped and started as normal.

To write a component that can be suspended, you need to implement the
`Suspendable` protocol. For example, assume you already have a
component that maintains some connection on a port. When we suspend
the component, we want to keep the connection open, and when we resume
the component, we want to reuse the same connection, but only if the
port is the same.

```clojure
(defrecord Foo [port]
  component/Lifecycle
  (start [component]
    (if (:connection component)
      component
      (assoc component :connection (open-connection port))))
  (stop [component]
    (if-let [connection (:connection component)]
      (do (close-connection connection)
          (dissoc component :connection))
      component))
  Suspendable
  (suspend [component]
    component)
  (resume [component old-component]
    (if (= (:port component) (:port old-component))
      (assoc component :connection (:connection old-component))
      (do (component/stop old-component)
          (component/start component)))
```
      
In the above case, `suspend` does nothing special, but in other
components it could be used to delay incoming data until the component
is resumed.

The `resume` method will only resume if the configuration for the
connection (in this example, just the port) is the same. Otherwise we
stop the old component and start the new component.

## License

Copyright © 2016 James Reeves

Distributed under the MIT License.
