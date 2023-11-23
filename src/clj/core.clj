(ns core
  (:require
   [clojure.java.io :as io]
   [muuntaja.core :as m]
   [reitit.ring :as ring]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [ring.adapter.jetty :as ring-jetty]))

(defn index
  []
  (slurp (io/file "./resources/public/index.html")))

(def app
  (ring/ring-handler
   (ring/router
    ["/"
     ["assets/*" (ring/create-file-handler {:root "./resources/public/assets/"})]
     ["" (fn [_] {:body (index) :status 200})]]
    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}})
   (ring/create-default-handler
    {:not-found (constantly {:status 200
                             :body (index)})})))

(defn start
  []
  (ring-jetty/run-jetty #'app {:port 3000
                               :join? false}))

(defn -main
  []
  (start))
