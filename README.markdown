# lein-bin

[![Current Version](https://img.shields.io/clojars/v/lein-bin.svg)](https://clojars.org/lein-bin)
[![License](https://img.shields.io/badge/License-EPL%201.0-green.svg)](https://opensource.org/licenses/EPL-1.0)

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

## Advanced Usage
Under the hood this plugin adds a snippet of text to the beginning of your uber jar. Assuming you rewrite all the offsets in the zip file (or jar file in this case) meta data, the resulting jar file is still considered valid by the [zip file specification](https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT) and as a consequence by any sane zip implementation including programs like unzip and perhaps more significantly by java itself. 

So we add a snippet of text to the beginning of the uber jar, rewrite the zip meta data offsets and then make the uber jar executable. Now when you try to directly execute the uber jar, the operating system will try to run the prelude snippet of text we added to the file. If this prelude snippet is written in a way such that it is considered a valid shell/bat script on both windows and linux/osx, then we have just created ourselves a true executable jar file without the need to invoke `java -jar uber.jar`. 

So to get this working we need to write a "script" which works on both windows and *nix. This is a science unto itself, but suffice to say that it is possible. The default sample script inserted looks as follows: 

```
:;exec java %s -jar $0 "$@"
@echo off\r\njava %s -jar %%1 \"%%~f0\" %%*\r\ngoto :eof
```

where:

* on windows machines only the second line is executed and the first one is seen as a comment
* on *nix machines the first line is executed and since the `exec` command relinquishes control from the current process and replaces it with the `java -jar ...` one, the second line is never executed

For advanced usage or to take advantage of startup accelerators such as [drip](https://github.com/ninjudd/drip), you can include a custom preamble script in place of the above snippet by using the `:preamble-script` key like so: 

```clojure
:bin {:name "runme"
      :bin-path "~/bin"
      :preamble-script "custom-preample.txt"}
```

an example preamble for using drip if drip exists and otherwise fall back to java could look as follows: 

```bash
:; hash drip  >/dev/null 2>&1 # make sure the command call on the next line has an up to date worldview
:;if command -v drip >/dev/null 2>&1 ; then CMD=drip; else CMD=java; fi
:;$CMD -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:-OmitStackTraceInFastThrow -client -Xbootclasspath/a:"$0" myapp.core "$@"; exit $?
@echo off
java -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:-OmitStackTraceInFastThrow -client -Xbootclasspath/a:%1 myapp.core "%~f0" %*
goto :eof
```

where `myapp.core` is the name of the main class of your program. 

## License

Copyright (C) 2012 Anthony Grimes, Justin Balthrop

Copyright (C) 2013 Jason Whitlark

Distributed under the Eclipse Public License, the same as Clojure.
