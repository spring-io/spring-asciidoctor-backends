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

  const delay = require("lodash/delay");

  addCodeToolElements();

  function addCodeToolElements() {
    for (const preElement of document.querySelectorAll(".doc pre.highlight")) {
      const codeToolsElement = document.createElement("div");
      codeToolsElement.className = "codetools";
      if (addButtons(preElement, codeToolsElement)) {
        preElement.appendChild(codeToolsElement);
      }
    }

    function addButtons(preElement, codeToolsElement) {
      let numberOfButtons = 0;
      if (hasHideWhenFoldedSpans(preElement)) {
        addFoldUnfoldButton(preElement, codeToolsElement);
        numberOfButtons++;
      }
      if (window.navigator.clipboard) {
        addCopyButton(preElement, codeToolsElement);
        numberOfButtons++;
      }
      return numberOfButtons > 0;
    }

    function hasHideWhenFoldedSpans(preElement) {
      return !!preElement.querySelector("span.hide-when-folded");
    }

    function addFoldUnfoldButton(preElement, codeToolsElement) {
      const foldUnfoldButton = createButton();
      updateFoldUnfoldButton(foldUnfoldButton, true);
      foldUnfoldButton.addEventListener(
        "click",
        onFoldUnfoldButtonClick.bind(foldUnfoldButton, preElement)
      );
      codeToolsElement.appendChild(foldUnfoldButton);
    }

    function addCopyButton(preElement, codeToolsElement) {
      const copyButton = createButton("Copy to clipboard", "copy-button");
      copyButton.addEventListener(
        "click",
        onCopyButtonClick.bind(copyButton, preElement)
      );
      copyButton.addEventListener("mouseleave", clearClicked.bind(copyButton));
      copyButton.addEventListener("blur", clearClicked.bind(copyButton));
      const copiedPopup = document.createElement("span");
      copyButton.appendChild(copiedPopup);
      copiedPopup.className = "copied";
      codeToolsElement.appendChild(copyButton);
    }

    function createButton(label, className) {
      const buttonElement = document.createElement("button");
      buttonElement.className = className;
      buttonElement.title = label;
      buttonElement.type = "button";
      const labelElement = document.createElement("span");
      labelElement.appendChild(document.createTextNode(label));
      labelElement.className = "label";
      buttonElement.appendChild(labelElement);
      return buttonElement;
    }
  }

  function onCopyButtonClick(preElement) {
    const codeElement = preElement.querySelector("code");
    const copy = codeElement.cloneNode(true);
    for (const hideWhenFoldedElement of copy.querySelectorAll(
      ".hide-when-unfolded"
    )) {
      hideWhenFoldedElement.parentNode.removeChild(hideWhenFoldedElement);
    }
    const text = copy.innerText;
    if (text) {
      window.navigator.clipboard
        .writeText(text + "\n")
        .then(markClicked.bind(this));
    }
  }

  function markClicked() {
    this.classList.add("clicked");
  }

  function clearClicked() {
    this.classList.remove("clicked");
  }

  function onFoldUnfoldButtonClick(preElement) {
    const codeElement = preElement.querySelector("code");
    const unfolding = !codeElement.classList.contains("unfolded");
    console.log("I am " + unfolding);
    codeElement.classList.remove(unfolding ? "folding" : "unfolding");
    codeElement.classList.add(unfolding ? "unfolding" : "folding");
    delay(function () {
      codeElement.classList.remove(unfolding ? "unfolding" : "folding");
      codeElement.classList.toggle("unfolded");
    }, 1100);
    updateFoldUnfoldButton(this, !unfolding);
  }

  function updateFoldUnfoldButton(button, unfold) {
    const label = unfold ? "Expanded folded text" : "Collapse foldable text";
    button.classList.remove(unfold ? "fold-button" : "unfold-button");
    button.classList.add(unfold ? "unfold-button" : "fold-button");
    button.querySelector("span.label").innerText = label;
    button.title = label;
  }
})();
