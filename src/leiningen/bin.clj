(ns leiningen.bin
  "Create a standalone executable for your project."
  (:use [clojure.java.io :only [copy file]]
        [clojure.string :only [join]]
        [leiningen.jar :only [get-jar-filename]]
        [leiningen.uberjar :only [uberjar]])
  (:import java.io.FileOutputStream))

(defn- jvm-options [{:keys [jvm-opts name version] :or {jvm-opts []}}]
  (join " " (conj jvm-opts (format "-client -D%s.version=%s" name version))))

(defn jar-preamble [flags]
  (format (str ":;exec java %s -jar $0 \"$@\"\n"
               "@echo off\r\njava %s -jar %%1 \"%%~f0\" %%*\r\ngoto :eof\r\n")
          flags flags))

(defn boot-preamble [flags main]
  (format (str ":;exec java %s -Xbootclasspath/a:$0 %s \"$@\"\n"
               "@echo off\r\njava %s -Xbootclasspath/a:%%1 %s "
               "\"%%~f0\" %%*\r\ngoto :eof\r\n")
          flags main flags main))

(defn write-jar-preamble! [out flags]
  (.write out (.getBytes (jar-preamble flags))))

(defn write-boot-preamble! [out flags main]
  (.write out (.getBytes (boot-preamble flags main))))

(defn bin
  "Create a standalone console executable for your project.

Add :main to your project.clj to specify the namespace that contains your
-main function."
  [project]
  (if (:main project)
    (let [opts (jvm-options project)
          target (file (:target-path project))
          binfile (file target
                        (or (get-in project [:bin :name])
                            (str (:name project) "-" (:version project))))]
      (uberjar project)
      (println "Creating standalone executable:" (.getPath binfile))
      (with-open [bin (FileOutputStream. binfile)]
        (if (get-in project [:bin :bootclasspath])
          (write-boot-preamble! bin opts (:main project))
          (write-jar-preamble! bin opts))
        (copy (file (get-jar-filename project :uberjar)) bin))
      (.setExecutable binfile true))
    (println "Cannot create bin without :main namespace in project.clj")))
