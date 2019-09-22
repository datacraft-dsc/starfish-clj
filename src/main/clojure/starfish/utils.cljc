(ns starfish.utils)

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defmacro error
  "Throws an error with the provided message(s). This is a macro in order to try and ensure the 
   stack trace reports the error at the correct source line number."
  ([& messages]
    `(throw (java.lang.Error. (str ~@messages)))))

(defmacro error?
  "Returns true if executing body throws an error, false otherwise."
  ([& body]
    `(try 
       ~@body
       false
       (catch Throwable t# 
         true)))) 

(defmacro TODO
  "Throws a TODO error. This ia a useful macro as it is easy to search for in source code, while
   also throwing an error at runtime if encountered."
  ([]
    `(TODO "Not yet implemented"))
  ([message]
    `(throw (java.lang.UnsupportedOperationException (str "TODO: " ~message)))))
