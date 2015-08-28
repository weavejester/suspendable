# Suspend

A very small library that adds a `suspend` method to [Component][].

[component]: https://github.com/stuartsierra/component

## Rationale

In the [reloaded workflow][], the system is stopped completely before
the codebase is refreshed. The problem with this approach is that it
doesn't allow for persistent connections to remain open across resets.

Ideally we want a way of signifying that a component prepare for a
reset, rather than stop entirely. This library introduces a new
method, `suspend`, that does exactly that.

[reloaded workflow]: http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded

## Installation

To install, add the following to your project `:dependencies`:

    [suspend "0.1.0-SNAPSHOT"]

## Usage

If you have a reset function in your user namespace, change it to use
`suspend-or-stop-system`:

```clojure
(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [suspend.core :refer [suspend-or-stop-system]]))

(defn reset []
  (alter-var-root #'system suspend-or-stop-system)
  (refresh :after 'user/go))
```

This will suspend any components that satisfy `Suspendable`, or stop
them as normal if not.

To write a component that can be suspended, you need to implement the
`suspend` method of the `Suspendable` protocol, and also ensure that
the `start` method can cope with suspended components.

## License

Copyright © 2015 James Reeves

Distributed under the MIT License.
