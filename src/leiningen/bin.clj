(ns leiningen.bin
  "Create a standalone executable for your project."
  (:use [clojure.java.io :only [copy file]]
        [clojure.string :only [join]]
        [leiningen.jar :only [get-jar-filename]]
        [leiningen.uberjar :only [uberjar]])
  (:import java.io.FileOutputStream))

(defn- jvm-options [{:keys [jvm-opts name version] :or {jvm-opts []}}]
  (join " " (conj jvm-opts (format "-D%s.version=%s" name version))))

(defn bin
  "Create a standalone console executable for your project.

Add :main to your project.clj to specify the namespace that contains your
-main function."
  [project]
  (if (:main project)
    (let [opts (jvm-options project)
          target (file (:target-path project))
          binfile (file target (:or (:executable-name project)
                                    (str (:name project) "-" (:version project))))]
      (uberjar project)
      (println "Creating standalone executable:" (.getPath binfile))
      (with-open [bin (FileOutputStream. binfile)]
        (.write bin (.getBytes (format ":;exec java %s -jar $0 \"$@\"\n" opts)))
        (.write bin (.getBytes (format "@echo off\r\njava %s -jar %%1 \"%%~f0\" %%*\r\ngoto :eof\r\n" opts)))
        (copy (file (get-jar-filename project :uberjar)) bin))
      (.setExecutable binfile true))
    (println "Cannot create bin without :main namespace in project.clj")))
