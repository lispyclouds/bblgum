# bblgum

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://choosealicense.com/licenses/mit/)
[![bb compatible](https://raw.githubusercontent.com/babashka/babashka/master/logo/badge.svg)](https://babashka.org)

An _extremely tiny and simple_ wrapper around the awesome [gum](https://github.com/charmbracelet/gum) tool.

This is intended for [babashka](https://babashka.org/) and JVM clojure and provides an idiomatic and data driven wrapper around the CLI tool.

## Requirements
- Gum should be [installed](https://github.com/charmbracelet/gum#installation)
- Babashka or the Clojure JVM runtime, latest recommended

## Usage

Add this to your `bb.edn` or `deps.edn`:
```edn
{:deps {io.github.lispyclouds/bblgum {:git/sha "49728fd485114e92f4c571ef7665ed181de675ae"}}}
```

## Interaction

This follows the same [section](https://github.com/charmbracelet/gum#interaction) on the gum repo and all params should work verbatim.
Run `gum <cmd> --help` to discover all the params and args.

This lib only has _one_ public fn: `bblgum.core/gum`. This is possibly the tiniest clojure lib!

Convention:
- The main command should be passed as a keyword or string to `:cmd`. Required
- The --opts are to be passed as `:opts`. Use the full forms of the opts: `--spinner` not `-s`. Seqs can be passed to opts taking multiple params as well
- All positional args to be passed as `:args`. Pass boolean flags like `--password` as `{:password true}`.
- An input stream can be passed to `:in`. Useful for commands like [filter](https://github.com/charmbracelet/gum#filter)
- Corece the input by passing `:as :bool` or ignore it with `:ignored`. `:bool` is useful for commands like [confirm](https://github.com/charmbracelet/gum#confirm) and `:ignored` is useful for [pager](https://github.com/charmbracelet/gum#pager) Defaults to a seq of strings
- Override the default gum path of `gum` by passing it via `:gum-path`

The `gum` fn returns a map of exit status of calling gum and the result either as a seq of lines or coerced via `:as`.
Exceptions are not thrown unless calling gum itself does, the status code is intended for programming for failures.

```clojure
(require '[bblgum.core :as b])
```

### input

```clojure
(b/gum {:cmd :input :opts {:password true}})
```

### write

```clojure
(b/gum {:cmd :write})
```

### filter

```clojure
(b/gum {:cmd :filter :in (clojure.java.io/input-stream "flavours.txt")})

(b/gum {:cmd :filter :in (clojure.java.io/input-stream "flavours.txt") :opts {:no-limit true}})
```

### confirm

```clojure
(b/gum {:cmd :confirm :as :bool})
```

### file

```clojure
(b/gum {:cmd :file :args ["src"]})
```

### pager

```clojure
(b/gum {:cmd :pager :as :ignored :in (clojure.java.io/input-stream "README.md")})
```

### spin

```clojure
(b/gum {:cmd :spin :args ["sleep" "5"] :opts {:spinner "line" :title "Buying Bubble Gum..."}})
```

### table

```clojure
(b/gum {:cmd :table :in (clojure.java.io/input-stream "flavours.csv") :as :ignored})
```

All of the rest of the options and usecases should be supported. Please raise issues/PRs for any improvements. Much appreciated!

## License

Copyright © 2023- Rahul De.

Distributed under the MIT License. See LICENSE.
