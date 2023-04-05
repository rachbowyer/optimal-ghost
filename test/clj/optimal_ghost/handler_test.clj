(ns optimal-ghost.handler-test
  (:require
    [clojure.test :refer :all]
    [muuntaja.core :as m]
    [mount.core :as mount]
    [optimal-ghost.handler :refer :all]
    [optimal-ghost.middleware.formats :as formats]
    [ring.mock.request :refer :all]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'optimal-ghost.config/env
                 #'optimal-ghost.handler/app-routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))))
