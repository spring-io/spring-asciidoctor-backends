"use strict";

const { src, dest, series, parallel, watch } = require("gulp");
const buffer = require("gulp-buffer");
const browserify = require("browserify");
const concat = require("gulp-concat");
const connect = require("gulp-connect");
const eslint = require("gulp-eslint7");
const header = require("gulp-header");
const log = require("gulplog");
const postcss = require("gulp-postcss");
const sourcemaps = require("gulp-sourcemaps");
const stylelint = require("gulp-stylelint");
const tap = require("gulp-tap");
const terser = require("gulp-terser");

const output =
  process.argv.indexOf("--output") != -1
    ? process.argv[process.argv.indexOf("--output") + 1]
    : "build/converted-asciidoc";

function info(cb) {
  log.info("Writing to " + output);
  cb();
}

function css() {
  return src("src/main/css/site.css")
    .pipe(
      stylelint({
        failAfterError: true,
        reporters: [{ formatter: "string", console: true }],
      })
    )
    .pipe(sourcemaps.init())
    .pipe(postcss())
    .pipe(
      /* MPL 2.0 requirement */
      header(
        "/*\n\nSource Available at https://github.com/spring-io/asciidoctor-spring-backend */\n\n\n"
      )
    )
    .pipe(sourcemaps.write("./"))
    .pipe(dest(output + "/css"))
    .pipe(connect.reload());
}

function img() {
  return src("src/main/img/*").pipe(dest(output + "/img"));
}

function jsSetup() {
  return jsFolder("setup");
}

function jsSite() {
  return jsFolder("site");
}

function jsFolder(name) {
  return src("src/main/js/" + name + "/*.js", { base: 'src/main/js' })
    .pipe(eslint())
    .pipe(eslint.format())
    .pipe(eslint.failAfterError())
    .pipe(tapBrowserify())
    .pipe(buffer())
    .pipe(initSourcemaps())
    .pipe(terser())
    .pipe(concat("js/" + name + ".js"))
    .pipe(sourcemaps.write("./"))
    .pipe(dest(output))
    .pipe(connect.reload());
}

function tapBrowserify() {
  return tap(function (file) {
    file.contents = browserify(file.path, {
      debug: true,
    })
      .plugin("browser-pack-flat/plugin")
      .bundle();
  });
}

function initSourcemaps() {
  return sourcemaps.init({
    loadMaps: true,
  });
}

function webServer(cb) {
  connect.server({
    root: output,
    livereload: true,
  });
  cb();
}

function watchFiles(cb) {
  watch("src/main/**", build);
  cb();
}

const build = series(info, css, img, jsSetup, jsSite);

exports.default = build;
exports.build = build;
exports.dev = series(build, parallel(webServer, watchFiles));
