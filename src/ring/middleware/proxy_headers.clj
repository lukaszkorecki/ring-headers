(ns ring.middleware.proxy-headers
  "Middleware for handling headers set by HTTP proxies."
  (:require [clojure.string :as str]))

(defn forwarded-remote-addr-request
  "Change the :remote-addr key of the request map to the last value present in
  the X-Forwarded-For header. See: wrap-forwarded-remote-addr."
  [request]
  (if-let [forwarded-for (get-in request [:headers "x-forwarded-for"])]
    (let [remote-addr (str/trim (re-find #"[^,]*$" forwarded-for))]
      (assoc request :remote-addr remote-addr))
    request))

(defn wrap-forwarded-remote-addr
  "Middleware that changes the :remote-addr of the request map to the
  last value present in the X-Forwarded-For header."
  [handler]
  (fn
    ([request]
     (handler (forwarded-remote-addr-request request)))
    ([request respond raise]
     (handler (forwarded-remote-addr-request request) respond raise))))

;; x-forwarded-proto header support
(def ^:private proto-lookup
  {"http" :http
   "https" :https
   "ws" :ws
   "wss" :wss})

(defn forwarded-scheme-request
  "Change the :scheme key of the request map to the last value present in
  the X-Forwarded-Proto header. See: wrap-forwarded-scheme."
  [request]
  (if-let [forwarded-proto (get-in request [:headers "x-forwarded-proto"])]
    (if-let [scheme (proto-lookup (str/lower-case (str/trim forwarded-proto)))]
      (assoc request :scheme scheme)
      request)
    request))

(defn wrap-forwarded-scheme
  "Middleware that changes the :scheme of the request map to the
  last value present in the X-Forwarded-Proto header."
  [handler]
  (fn
    ([request]
     (handler (forwarded-scheme-request request)))
    ([request respond raise]
     (handler (forwarded-scheme-request request) respond raise))))
