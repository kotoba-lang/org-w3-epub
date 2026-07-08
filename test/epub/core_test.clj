(ns epub.core-test
  (:require [clojure.test :refer [deftest is]]
            [epub.core :as epub]))

(defn- e [name s] {:name name :bytes (mapv int (.getBytes ^String s "UTF-8"))})

(deftest epub-parse
  (let [container "<?xml version=\"1.0\"?><container><rootfiles><rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/></rootfiles></container>"
        opf "<package><metadata><dc:title>Test Book</dc:title></metadata><manifest><item id=\"c1\" href=\"ch1.xhtml\" media-type=\"application/xhtml+xml\"/></manifest><spine><itemref idref=\"c1\"/></spine></package>"
        xhtml "<html><body><h1>Chapter</h1><p>Hello epub world</p></body></html>"
        entries [(e "mimetype" "application/epub+zip")
                 (e "META-INF/container.xml" container)
                 (e "OEBPS/content.opf" opf)
                 (e "OEBPS/ch1.xhtml" xhtml)]
        p (epub/parse entries)]
    (is (= "Test Book" (:title p)))
    (is (= 1 (:spine-count p)))
    (is (= "Chapter Hello epub world" (-> p :chapters first :text)))))
