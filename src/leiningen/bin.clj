(ns leiningen.bin
  "Create a standalone executable for your project."
  (:use [clojure.java.io :only [copy file]]
        [leiningen.jar :only [get-default-uberjar-name]]
        [leiningen.uberjar :only [uberjar]])
  (:import java.io.FileOutputStream))

(defn ^{:help-arglists '([])} bin
  "Create a standalone console executable for your project.

Add :main to your project.clj to specify the namespace that contains your
-main function."
  [project]
  (if (:main project)
    (let [binfile (file (:name project))]
      (uberjar project)
      (println "Creating standalone executable:" (.getPath binfile))
      (with-open [bin (FileOutputStream. binfile)]
        (let [opts (:jvm-opts project "")]
          (.write bin (.getBytes (format ":;exec java %s -jar $0 \"$@\"\n" opts)))
          (.write bin (.getBytes (format "@echo off\r\njava %s -jar %%1 \"%%~f0\" %%*\r\ngoto :eof\r\n" opts))))
        (copy (file (str (get-default-uberjar-name project))) bin))
      (.setExecutable binfile true))
    (println "Cannot create bin without :main namespace in project.clj")))