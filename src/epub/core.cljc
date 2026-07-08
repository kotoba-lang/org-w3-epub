(ns epub.core
  "EPUB 3 (W3C Recommendation) content extraction from an already-unzipped
   entry table. Resolves META-INF/container.xml → OPF → spine, extracts
   dc:title and the text of each spine XHTML document (tags stripped).
   Pure cljc, zero dependencies — the caller unzips the .epub file (e.g.
   with org-pkware-zip) and passes the resulting entries here. Extracted
   from kotoba-lang/kasane (kasane.normalize/epub->doc, ADR-2606272100) as
   `org-w3-epub`."
  (:require [clojure.string :as str]))

(defn- bytes->str [bs] (apply str (map char bs)))

(defn- xml-texts
  "Extract text between <tag …>…</tag> elements (namespace-prefixed local
   name, e.g. \"dc:title\"). Portable across clj/cljs ([\\s\\S] instead of
   dotall flag)."
  [xml tag]
  (mapv second (re-seq (re-pattern (str "<" tag "[^>]*>([\\s\\S]*?)</" tag ">")) xml)))

(defn- strip-tags [s]
  (-> (str s) (str/replace #"<[^>]*>" " ") (str/replace #"\s+" " ") str/trim))

(defn parse
  "`entries` = seq of {:name :bytes} (an already-unzipped .epub). Returns
   {:title :spine-count :chapters [{:text \"...\"} ...]}."
  [entries]
  (let [by   (into {} (map (juxt :name identity) entries))
        gets (fn [n] (some-> (by n) :bytes bytes->str))
        opf-path (second (re-find #"full-path=\"([^\"]+)\"" (or (gets "META-INF/container.xml") "")))
        opf  (some-> opf-path gets)
        dir  (if (and opf-path (str/includes? opf-path "/"))
               (subs opf-path 0 (inc (str/last-index-of opf-path "/"))) "")
        man  (into {} (map (fn [[_ id href]] [id (str dir href)])
                           (re-seq #"<item\s+[^>]*id=\"([^\"]+)\"[^>]*href=\"([^\"]+)\"" (or opf ""))))
        spine (mapv second (re-seq #"<itemref\s+[^>]*idref=\"([^\"]+)\"" (or opf "")))
        docs  (keep #(gets (man %)) spine)]
    {:title (first (xml-texts (or opf "") "dc:title"))
     :spine-count (count spine)
     :entry-count (count entries)
     :chapters (mapv (fn [d] {:text (strip-tags d)}) docs)}))
