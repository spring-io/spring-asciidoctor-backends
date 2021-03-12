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

  const debounce = require("lodash/debounce");
  const throttle = require("lodash/throttle");

  const debugMode = false;

  let tocElement;
  let toggleTocElement;
  let contentElement;

  let headingElements;
  let hrefToTocAnchorElement;
  let headingElementToTocElement;

  let lastActiveTocElement = null;
  let disableOnScroll = false;

  window.addEventListener("load", onLoad);

  function onLoad() {
    tocElement = document.querySelector("#toc");
    toggleTocElement = document.querySelector("#toggle-toc");
    contentElement = document.querySelector("#content");
    if (!tocElement || !contentElement) {
      return;
    }
    headingElements = findHeadingElements();
    hrefToTocAnchorElement = buildHrefToTocAnchorElement();
    headingElementToTocElement = buildHeadingElementToTocElement();
    onLocationHashChange();
    window.addEventListener("hashchange", onLocationHashChange);
    window.addEventListener("scroll", onScroll);
    window.addEventListener("scroll", onEndScroll);
    window.addEventListener("resize", onResize);
    tocElement.addEventListener("click", onTocElementClick);
    toggleTocElement.addEventListener("click", onToggleTocClick);
  }

  function findHeadingElements() {
    const maxHeadingLevel = getMaxHeadingLevel();
    const headingSelectors = [];
    for (
      let headingLevel = 0;
      headingLevel <= maxHeadingLevel;
      headingLevel++
    ) {
      headingSelectors.push("h" + (headingLevel + 1) + "[id]");
    }
    return contentElement.querySelectorAll(headingSelectors);

    function getMaxHeadingLevel() {
      let maxHeadingLevel = 1;
      for (const listElement of tocElement.querySelectorAll("ul", "ol")) {
        maxHeadingLevel = Math.max(
          maxHeadingLevel,
          getHeadingLevel(listElement)
        );
      }
      return maxHeadingLevel;
    }

    function getHeadingLevel(element) {
      let headingLevel = 0;
      while (element && element !== tocElement) {
        headingLevel +=
          element.nodeName === "UL" || element.nodeName === "OL" ? 1 : 0;
        element = element.parentElement;
      }
      return element ? headingLevel : -1;
    }
  }

  function buildHrefToTocAnchorElement() {
    const result = new Map();
    for (const tocAnchorElement of tocElement.querySelectorAll("li > a")) {
      const href = tocAnchorElement.getAttribute("href");
      if (href) {
        result.set(href, tocAnchorElement);
      }
    }
    return result;
  }

  function buildHeadingElementToTocElement() {
    const result = new Map();
    for (const headingElement of headingElements) {
      const href = getChildAnchorHref(headingElement);
      if (href) {
        const tocAnchorElement = hrefToTocAnchorElement.get(href);
        if (tocAnchorElement) {
          const tocElement = tocAnchorElement.parentElement;
          result.set(headingElement, tocElement);
        }
      }
    }
    return result;
  }



  function onLocationHashChange() {
    updateFixedPositionClass();
    const tocAnchorElement = hrefToTocAnchorElement.get(window.location.hash);
    const headingElement = getHeadingElementByHref(window.location.hash);
    if (tocAnchorElement && isInViewport(headingElement)) {
      disableOnScroll = true;
      debug("activating window location hash");
      activateTocElement(tocAnchorElement.parentElement);
    }
    onEndScroll();
  }

  const onScroll = throttle(
    function () {
      updateFixedPositionClass();
      if (!disableOnScroll) {
        activateTopHeadingTocElement();
      }
    },
    50,
    { leading: true }
  );

  const onEndScroll = debounce(function () {
    debug("scrolling ended");
    updateFixedPositionClass();
    disableOnScroll = false;
    if (lastActiveTocElement) {
      const href = getChildAnchorHref(lastActiveTocElement);
      const headingElement = getHeadingElementByHref(href);
      if (!isInViewport(headingElement)) {
        activateTopHeadingTocElement();
      }
    } else {
      activateTopHeadingTocElement();
    }
  }, 50);

  const onResize = throttle(
    function () {
      updateFixedPositionClass();
    },
    50,
    { leading: true }
  );

  function onTocElementClick(event) {
    if (event.target.nodeName === "A") {
      const parentElement = event.target.parentElement;
      if (parentElement && parentElement.id === "back-to-index") {
        return;
      }
      disableOnScroll = true;
      debug("activating clicked toc element");
      activateTocElement(event.target.parentElement);
    }
  }

  function onToggleTocClick(event) {
    event.stopPropagation();
    const showing = document.body.classList.toggle("show-toc");
    if (showing) {
      document.documentElement.addEventListener("click", onToggleTocClick);
    } else {
      document.documentElement.removeEventListener("click", onToggleTocClick);
    }
  }

  function updateFixedPositionClass() {
    const computedStyle = window.getComputedStyle(document.documentElement);
    const bannerHeight = parseInt(
      computedStyle.getPropertyValue("--layout-banner-height"),
      10
    );
    if (getTop() >= bannerHeight) {
      document.body.classList.add("fixed-toc");
    } else {
      document.body.classList.remove("fixed-toc");
    }
  }

  function activateTopHeadingTocElement() {
    debug("activating top header element");
    const topHeadingElement = getTopHeading();
    activateTocElement(headingElementToTocElement.get(topHeadingElement));
  }

  function getTopHeading() {
    const top = getTop();
    const topHeadingPos = top + 45;
    for (let i = 0; i < headingElements.length; ++i) {
      if (headingElements[i].offsetTop > topHeadingPos) {
        return headingElements[i - 1 >= 0 ? i - 1 : 0];
      }
    }
    return headingElements[headingElements.length - 1];
  }

  function getHeadingElementByHref(href) {
    for (let i = 0; i < headingElements.length; ++i) {
      if (getChildAnchorHref(headingElements[i]) === href) {
        return headingElements[i];
      }
    }
    return null;
  }

  function getChildAnchorHref(element) {
    const achorElement = element.querySelector("a[href]");
    return achorElement ? achorElement.getAttribute("href") : null;
  }

  function activateTocElement(element) {
    if (element && element !== lastActiveTocElement) {
      debug("activating " + element.textContent);
      deactivateTocElement(lastActiveTocElement);
      lastActiveTocElement = element;
      element.classList.add("active");
      while (element.parentElement && element.parentElement !== tocElement) {
        element.parentElement.classList.add("expanded");
        element = element.parentElement;
      }
    }

    function deactivateTocElement(element) {
      if (element) {
        element.classList.remove("active");
        while (element.parentElement && element.parentElement !== tocElement) {
          element.parentElement.classList.remove("expanded");
          element = element.parentElement;
        }
      }
    }
  }

  function getTop() {
    return document.documentElement.scrollTop || document.body.scrollTop;
  }

  function isInViewport(element) {
    if (!element) {
      return false;
    }
    const rect = element.getBoundingClientRect();
    return (
      rect.top >= 0 &&
      rect.bottom <=
        (window.innerHeight || document.documentElement.clientHeight)
    );
  }

  function debug(message) {
    if (debugMode) {
      console.debug(message);
    }
  }
})();
