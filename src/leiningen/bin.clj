(ns leiningen.bin
  "Create a standalone executable for your project."
  (:require [clojure.string :refer [join]]
            [leiningen.jar :refer [get-jar-filename]]
            [leiningen.uberjar :refer [uberjar]]
            [me.raynes.fs :as fs]
            [clojure.java.io :as io])
  (:import java.io.FileOutputStream))

(defn- jvm-options [{:keys [jvm-opts name version] :or {jvm-opts []}}]
  (let [is-server (some #(= %1 "-server") jvm-opts)
         client-opt (if is-server "-server" "-client")]
    (join " " (distinct (conj jvm-opts client-opt (format "-D%s.version=%s" name version))))))

(defn jar-preamble [flags]
  (format (str ":;exec java %s -jar \"$0\" \"$@\"\n"
               "@java %s -jar \"%%~f0\" %%*\r\n"
               "@goto :eof\r\n")
          flags flags))

(defn boot-preamble [flags main]
  (format (str ":;exec java %s -Xbootclasspath/a:\"$0\" %s \"$@\"\n"
               "@java %s -Xbootclasspath/a:\"%%~f0\" %s %%*\r\n"
               "@goto :eof\r\n")
          flags main flags main))

(defn write-jar-preamble! [out flags]
  (.write out (.getBytes (jar-preamble flags))))

(defn write-boot-preamble! [out flags main]
  (.write out (.getBytes (boot-preamble flags main))))

(defn ^:private copy-bin [project binfile]
  (when-let [bin-path (get-in project [:bin :bin-path])]
    (let [bin-path (fs/expand-home bin-path)
          new-binfile (fs/file bin-path (fs/base-name binfile))]
      (println "Copying binary to" bin-path)
      (fs/chmod "+x" (fs/copy+ binfile new-binfile)))))

(defn bin
  "Create a standalone console executable for your project.

Add :main to your project.clj to specify the namespace that contains your
-main function."
  [project]
  (if (:main project)
    (let [opts (jvm-options project)
          target (fs/file (:target-path project))
          binfile (fs/file target
                           (or (get-in project [:bin :name])
                               (str (:name project) "-" (:version project))))
          jarfile (uberjar project)]
      (println "Creating standalone executable:" (str binfile))
      (io/make-parents binfile)
      (with-open [bin (FileOutputStream. binfile)]
        (if (get-in project [:bin :bootclasspath])
          (write-boot-preamble! bin opts (:main project))
          (write-jar-preamble! bin opts))
        (io/copy (fs/file jarfile) bin))
      (fs/chmod "+x" binfile)
      (copy-bin project binfile))
    (println "Cannot create bin without :main namespace in project.clj")))
