(ns lambdaisland.kramdown
  "JRuby-based wrapper to parse GFM-flavored Markdown with Kramdown extensions."
  (:import
   (org.jruby Ruby RubyProc)
   (org.jruby.javasupport JavaUtil)
   (org.jruby.runtime.builtin IRubyObject)))

(set! *warn-on-reflection* true)

(defonce ^Ruby ruby (memoize #(Ruby/newInstance)))

(defn- java->ruby [obj]
  (JavaUtil/convertJavaToRuby (ruby) obj))

(defn- ->IRubyObjectArray ^IRubyObject/1 [xs]
  (into-array IRubyObject (map java->ruby xs)))

(defn- rb-eval [& strs]
  (last
   (for [s strs]
     (.evalScriptlet (ruby) s))))

(defn- rb-call [^RubyProc this & args]
  (.call this
         (.getCurrentContext (ruby))
         (->IRubyObjectArray args)))

(def convert-rb
  (delay
    (rb-eval "require 'kramdown'"
             "-> str { Kramdown::Document.new(str, parse_block_html: true, input: 'GFM', syntax_highlighter: nil, hard_wrap: false).to_html }")))

(defn convert [s]
  (if s
    (str (rb-call @convert-rb s))
    ""))
