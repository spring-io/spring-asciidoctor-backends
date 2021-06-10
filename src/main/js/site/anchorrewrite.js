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
  "use strict";

  window.addEventListener("load", onChange);
  window.addEventListener("hashchange", onChange);

  function onChange() {
    const element = document.getElementById("anchor-rewrite");
    const anchor = window.location.hash.substr(1);
    if (element && anchor) {
      const rewites = JSON.parse(element.innerHTML);
      updateAnchor(anchor, rewites);
    }
  }

  function updateAnchor(anchor, rewrites) {
    const seen = [anchor];
    console.debug(anchor);
    while (rewrites[anchor]) {
      anchor = rewrites[anchor];
      if (seen.includes(anchor)) {
        console.error("Skipping circular anchor update");
        return;
      }
      seen.push(anchor);
    }
    window.location.hash = anchor;
  }
})();
