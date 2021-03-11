"use strict";

module.exports = {
  plugins: {
    "postcss-import": {},
    cssnano: {
      preset: ["default", {
        normalizeUrl: false,
      }]
    },
    "autoprefixer": {}
  },
};
