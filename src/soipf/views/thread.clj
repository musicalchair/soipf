(ns soipf.views.thread
  (:refer-clojure :exclude [get swap!])
  (:require [noir.validation :as vali])
  (:use noir.core
        noir.request
        noir.session
        [noir.response :only [redirect status json]]
        hiccup.form-helpers
        hiccup.page-helpers
        soipf.views.common
        [soipf.format :only [date-str]]
        [soipf.models.thread :only [create-thread! add-reply! get-thread-listing retrieve-thread]]
        [soipf.models.user :only [logged-in?]]))

(defpartial new-thread [{:keys [title body]}]
  (form-to {:class "form-horizontal"} [:post "/thread"]
           [:legend "New Thread"]
           [:div {:class (error-class :title)}
            [:label {:for "title"} "Title"]
            [:div.controls
             (text-field {:class "input-xlarge"} :title title)
             (error-help :title)]]

           [:div {:class (error-class :body)}
            [:label {:for "body"} "Body"]
            [:div.controls
             [:textarea#body.input-xlarge
              {:name "body" :rows 6 }
              body]
             (error-help :body)]]
           [:div.form-actions
            [:button.click-once {:type "submit"} "Create Thread"]]))

(defpartial display-post [post]
  [:div.post
   [:div.metadata
    [:span.author (get-in post [:author :login])]
    " at "
    (date-str (post :created-at))]
   (post :content)])

(defpartial list-thread [thread]
  [:div.item (link-to (url-for show-thread {:id (thread :_id)})
                      (thread :title))
   [:div.info "by " (get-in thread [:author :login])]])

(defpage "/" []
  (let [threads (get-thread-listing)]
    (layout
     [:div.row
      (link-to {:class "btn btn-primary"} "/thread" "New Thread")]
     (map list-thread threads))))

(pre-route "/thread*" {}
           (when-not (logged-in?)
             (put! :redirected-from ((ring-request) :uri))
             (redirect "/login")))

(defpage "/thread" {:as t}
  (layout (new-thread t)))

(defpage [:post "/thread"] {:keys [title body] :as t}
  (if-let [thread (create-thread! (assoc t :author (logged-in?)))]
    (redirect (url-for show-thread {:id (:_id thread)}))
    (render "/thread" t)))

(defpartial reply-form [id body]
  (form-to {:class "form-horizontal well"} [:post (url-for reply-to-thread {:id id})]
           [:div {:class (error-class :body)}
            [:div.controls
             [:textarea#body.input-xlarge
              {:name "body" :rows 6 :cols 50}
              body]
             (error-help :body)]]
           [:div.form-actions
            [:button.click-once {:type "submit"} "Add Reply"]]))

(defpartial display-thread [thread]
  [:div.thread
   [:div.heading
    (link-to (url-for show-thread {:id (thread :_id)}) (thread :title))]
   [:div.content
    (map display-post (thread :posts))]
   (reply-form (thread :_id) "")])

(defpage show-thread "/thread/:id" {:keys [id body]}
  (if-let [{:keys [_id title author created-at posts] :as thread} (retrieve-thread id)]
    (layout (display-thread thread))
    (status 404 (layout [:h1 "Thread not found"]))))

(defpage reply-to-thread [:post "/thread/:id"] {:keys [id body] :as args}
  (if-let [{:keys [_id title posts]} (retrieve-thread id -5)]
    (do (add-reply! (assoc args :author (logged-in?)))
        (render show-thread {:id id}))
    (redirect "/")))
