(ns leiningen.bin
  "Create a standalone executable for your project."
  (:use [clojure.java.io :only [copy file]]
        [clojure.string :only [join]]
        [leiningen.jar :only [get-default-uberjar-name]]
        [leiningen.uberjar :only [uberjar]])
  (:import java.io.FileOutputStream))

(defn ^{:help-arglists '([])} bin
  "Create a standalone console executable for your project.

Add :main to your project.clj to specify the namespace that contains your
-main function."
  [project]
  (if (:main project)
    (let [opts (join " " (:jvm-opts project ""))
          target (file (:target-dir project))
          binfile (file target (:name project))]
      (uberjar project)
      (println "Creating standalone executable:" (.getPath binfile))
      (with-open [bin (FileOutputStream. binfile)]
        (.write bin (.getBytes (format ":;exec java %s -jar $0 \"$@\"\n" opts)))
        (.write bin (.getBytes (format "@echo off\r\njava %s -jar %%1 \"%%~f0\" %%*\r\ngoto :eof\r\n" opts)))
        (copy (file target (get-default-uberjar-name project)) bin))
      (.setExecutable binfile true))
    (println "Cannot create bin without :main namespace in project.clj")))