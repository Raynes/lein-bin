# lein-bin

A Leiningen plugin for producing standalone console executables that work on OS X, Linux, and Windows.

It basically just takes your uberjar and stuffs it in another file with some fancy magical execution stuff.

## Usage

This is a leiningen plugin. If you want it, do `lein plugin install lein-bin 0.1.0-alpha1`.

Your project needs to have a `:main` field specifying the namespace that contains your `-main` function. If you have that, just run `lein bin` and it'll produce a standalone executable for your project.

## License

Copyright (C) 2011 Anthony Grimes, Justin Balthrop

Distributed under the Eclipse Public License, the same as Clojure.
