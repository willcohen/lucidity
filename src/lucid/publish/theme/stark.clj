(ns lucid.publish.theme.stark
  (:require [lucid.publish.engine :as engine]
            [hiccup.compiler :as compiler]
            [hiccup.core :as html]
            [clojure.string :as string]))

(def settings
  {:engine    "winterfell"
   :resource  "theme/stark"
   :copy      ["assets"]
   :render    {:article       "render-article"
               :navigation    "render-navigation"
               :top-level     "render-top-level"}
   :defaults  {:icon             "stark"
               :template         "article.html"
               :icon             "favicon"
               :tracking-enabled "false"}
   :manifest  ["article.html"
               "assets/favicon.ico"
               "assets/js/highlight.min.js"
               "assets/js/gumshoe.min.js"
               "assets/js/smooth-scroll.min.js"
               "assets/css/api.css"
               "assets/css/code.css"
               "assets/css/highlight.css"
               "assets/css/page.css"]})

(def engine (engine/engine (:engine settings)))

(defn render-top-level [interim name]
  (let [files (-> interim
                  :project
                  :publish
                  :files
                  (dissoc "index")
                  (sort))]
    (->> files
         (map (fn [[key {title :title}]]
                [:li [:a {:href (str key ".html")} title]]))
         (concat [:ul [:li [:a {:href "index.html"} "home"]]])
         vec
         html/html)))

(defn render-article [interim name]
  (->> (get-in interim [:articles name :elements])
       (map (:page-element engine))
       (mapcat (fn [ele] (#'compiler/compile-seq [ele])))
       (string/join)))

(defn render-navigation [interim name]
  (let [elems (get-in interim [:articles name :elements])
        telems (filter #(-> % :type #{:chapter :section :subsection}) elems)]
    (->> telems
         (map (:nav-element engine))
         (mapcat (fn [ele] (#'compiler/compile-seq [ele])))
         string/join)))
