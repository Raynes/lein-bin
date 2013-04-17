# lein-bin

A Leiningen plugin for producing standalone console executables that
work on OS X, Linux, and Windows.

It basically just takes your uberjar and stuffs it in another file
with some fancy magical execution stuff.

## Usage

This is a leiningen plugin. If you're using lein 1, run `lein plugin
install lein-bin <current-version>` to install it.  If you're using
lein 2, add the plugin to your default profile in
`~/.lein/profiles.clj`.

Your project needs to have a `:main` field specifying the namespace
that contains your `-main` function.  If you have that, just run `lein
bin` and it'll produce a standalone executable for your project. Note
that your main namespace currently needs to be AOT compiled (it just
needs to have `:gen-class` specified in its `ns` declaration).

You can also supply a `:bin` key like so:

        :bin {:name "runme"
              :bin-path "~/bin"
              :bootclasspath true}

  * `:name`: Name the file something other than `project-version`
  * `:bin-path`: If specified, also copy the file into `bin-path`, which is presumably on your $PATH.
  * `:bootclasspath`: Supply the uberjar to java via `-Xbootclasspath/a` instead of `-jar`.  Sometimes this can speed up execution, but may not work with all classloaders.

## License

Copyright (C) 2012 Anthony Grimes, Justin Balthrop

Copyright (C) 2013 Jason Whitlark

Distributed under the Eclipse Public License, the same as Clojure.
