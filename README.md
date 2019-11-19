# myshoppingapp

An e-commerce web site developed using Clojure[[Clojure](https://clojure.org/ "Clojure Homepage")]/ReactJs[[ReactJs](https://reactjs.org/ "React Homepage")]/Bootstrap[[Bootstrap4](https://getbootstrap.com/docs/4.0/getting-started/introduction/ "Bootstrap4 Homepage")]/Lucene[[Lucene](https://lucene.apache.org/ "Lucene Homepage")].

## Build
### React JSX build
npm should be used to build the React JSX (extension `.jsx`) files. Once the `npm run build` generates the desired `.js` files, those need to be copied into the project's `resources/public/js` folder.The raw JSX files have been placed under `npm-build` directory of this project.

### Clojure backend build
Leiningen[[Leiningen](https://leiningen.org/ "Leiningen Homepage")] has been used. Nothing specific needs to be done to build the backend.As part of installation of this project, the backend build will also happen.

## Installation
cd to the project directory and run `lein run`. This will compile and start the server process at port 3000.

## Screen shots
Please take a look at the projects overview page available @ [Project overview](https://binitabharati.wordpress.com/2019/11/19/e-commerce-web-site-with-clojure-reactjs-bootstrap-lucene/ "Wordpress project homepage")

## License

Copyright © 2019 Binita Bharati

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
