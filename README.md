# kotoba-lang/org-w3-epub

Zero-dep portable `.cljc` EPUB 3 (W3C Recommendation) content extractor.
Resolves `META-INF/container.xml` → OPF → spine, extracts `dc:title` and
spine XHTML text. Operates on an already-unzipped entry table (`{:name
:bytes}` seq) — the caller unzips the `.epub` file first, e.g. with
`org-pkware-zip`. Extracted from `kotoba-lang/kasane`
(kasane.normalize/epub->doc, ADR-2606272100).

## Usage

```clojure
(require '[epub.core :as epub])

(epub/parse entries)  ; entries = seq of {:name "path" :bytes [...]}
;; => {:title "..." :spine-count N :entry-count N :chapters [{:text "..."}]}
```

## Test

```sh
clojure -M:test
```
