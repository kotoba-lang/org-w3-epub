(ns epub.real-file-test
  "epub.core/parse against a real pandoc-generated .epub (`pandoc book.md -o
   book.epub`, 3-document spine: title page + 2 chapters). The existing
   epub-parse test only ever exercised a hand-built 4-entry vector; this is
   the first real EPUB 3 package (real META-INF/container.xml, real OPF
   with pandoc's actual namespace/attribute conventions, real XHTML content
   documents) this parser has ever seen. This test does its own unzip
   (java.util.zip, JVM-only) since epub.core is a zero-dep pure parser that
   takes already-unzipped {:name :bytes} entries by design (the caller is
   expected to supply a zip reader, e.g. org-pkware-zip, at the call site)."
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [epub.core :as epub])
  (:import [java.util.zip ZipInputStream]))

(defn- unzip-resource [path]
  (with-open [zis (ZipInputStream. (io/input-stream (io/resource path)))]
    (loop [acc []]
      (if-let [ent (.getNextEntry zis)]
        (let [baos (java.io.ByteArrayOutputStream.)]
          (io/copy zis baos)
          (recur (conj acc {:name (.getName ent)
                             :bytes (mapv #(bit-and (int %) 0xff) (.toByteArray baos))})))
        acc))))

(deftest real-pandoc-epub
  (let [entries (unzip-resource "epub/fixtures/pandoc_book.epub")
        p (epub/parse entries)]
    (testing "container.xml -> OPF -> dc:title resolution against pandoc's real package"
      (is (= "My Real Book" (:title p))))
    (testing "spine order: title page + 2 chapters, matching the OPF's real <spine>"
      (is (= 3 (:spine-count p)))
      (is (= 10 (:entry-count p))))
    (testing "tag-stripped chapter text extracted from pandoc's real XHTML content documents"
      (is (= "ch001.xhtml Chapter One Hello epub world from a real pandoc export."
             (:text (second (:chapters p)))))
      (is (= "ch002.xhtml Chapter Two Second chapter content here."
             (:text (nth (:chapters p) 2)))))))
