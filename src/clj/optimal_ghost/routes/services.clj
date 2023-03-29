(ns optimal-ghost.routes.services
  (:require
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [optimal-ghost.engine :as engine]
    [optimal-ghost.middleware.formats :as formats]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]))

(def version (-> "version.txt" io/resource slurp str/trim))

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 coercion/coerce-exceptions-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "Optimal Ghost"
                         :description "Backend for the optimal ghost game"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

   ["/get-move"
    {:post {:summary    "Gets the next move"
            :parameters {:body {:word string?}}
            :responses  {200 {:body {:status keyword? :move char?}}}
            :handler    (fn [{{{:keys [word]} :body} :parameters}]
                          (log/info "Get move" word)
                          (let [[status move] (engine/get-move engine/dict word)]
                            (log/info "Get move response" status move)
                            {:status 200
                             :body   {:status status
                                      :move   move}}))}}]

   ["/get-version"
    {:get {:summary    "Gets the version number"
            :responses  {200 {:body {:version string?}}}
            :handler    (fn [_]
                          {:status 200
                           :body   {:version version}})}}]])


