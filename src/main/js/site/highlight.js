/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function () {
  'use strict';

  const highlightJs = require('highlight.js/lib/core');

  highlightJs.registerLanguage('asciidoc', require('highlight.js/lib/languages/asciidoc'));
  highlightJs.registerLanguage('bash', require('highlight.js/lib/languages/bash'));
  highlightJs.registerLanguage('css', require('highlight.js/lib/languages/css'));
  highlightJs.registerLanguage('diff', require('highlight.js/lib/languages/diff'));
  highlightJs.registerLanguage('dockerfile', require('highlight.js/lib/languages/dockerfile'));
  highlightJs.registerLanguage('dos', require('highlight.js/lib/languages/dos'));
  highlightJs.registerLanguage('gradle', require('highlight.js/lib/languages/gradle'));
  highlightJs.registerLanguage('groovy', require('highlight.js/lib/languages/groovy'));
  highlightJs.registerLanguage('http', require('highlight.js/lib/languages/http'));
  highlightJs.registerLanguage('java', require('highlight.js/lib/languages/java'));
  highlightJs.registerLanguage('javascript',require('highlight.js/lib/languages/javascript'));
  highlightJs.registerLanguage('json', require('highlight.js/lib/languages/json'));
  highlightJs.registerLanguage('kotlin', require('highlight.js/lib/languages/kotlin'));
  highlightJs.registerLanguage('markdown', require('highlight.js/lib/languages/markdown'));
  highlightJs.registerLanguage('nix', require('highlight.js/lib/languages/nix'));
  highlightJs.registerLanguage('powershell', require('highlight.js/lib/languages/powershell'));
  highlightJs.registerLanguage('properties', require('highlight.js/lib/languages/properties'));
  highlightJs.registerLanguage('ruby', require('highlight.js/lib/languages/ruby'));
  highlightJs.registerLanguage('scala', require('highlight.js/lib/languages/scala'));
  highlightJs.registerLanguage('shell', require('highlight.js/lib/languages/shell'));
  highlightJs.registerLanguage('sql', require('highlight.js/lib/languages/sql'));
  highlightJs.registerLanguage('swift', require('highlight.js/lib/languages/swift'));
  highlightJs.registerLanguage('xml', require('highlight.js/lib/languages/xml'));
  highlightJs.registerLanguage('yaml', require('highlight.js/lib/languages/yaml'));

  highlightJs.configure({ignoreUnescapedHTML: true});

  for(const codeElement of document.querySelectorAll('pre.highlight > code')) {
    highlightJs.highlightBlock(codeElement);
  }

})();
