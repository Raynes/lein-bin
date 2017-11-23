(defproject lein-bin "0.3.5"
  :description "A leiningen plugin for generating standalone console executables for your project."
  :url "https://github.com/Raynes/lein-bin"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[me.raynes/fs "1.4.0"]
                 [clj-zip-meta/clj-zip-meta "0.1.1" :exclusions [org.clojure/clojure]]]
                 ;;[org.clojure/core.incubator "0.1.4"]]
  :eval-in-leiningen true)
