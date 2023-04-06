# optimal-ghost

My implementation of ITA's Optimal Ghost. To read me about it please click
[here](https://www.bowyer.info/ita-optimal-ghost).


## To run it in a REPL

Needs leiningen, shadow-cljs and tailwind

Then

    npx tailwindcss -i ./resources/css/input.css -o ./resources/public/css/output.css --watch

to run tailwind in watch mode

    shadow-cljs watch app

to run shadow-cljs in watch mode


To start a web server for the application, run:

    lein run 


In the REPL, run:

    (start)


## To build the app and run it

    lein clean
    shadow-cljs compile app
    npx tailwindcss -i ./resources/css/input.css -o ./resources/public/css/output.css
    lein uberjar
    java -jar target/default+uberjar/optimal-ghost.jar


## To run the tests

    lein test


## The URLs

To access the API
http://localhost:3000/api/api-docs/index.html#/

To access the app
http://localhost:3000/


Or to access a prebuilt version of the app
http://optimal-ghost.bowyer.info/


## Copyright Information

Copyright © 2023
