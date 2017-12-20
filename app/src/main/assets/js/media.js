"use strict";

(function () {
  // we will handle media events
  var _frostMediaClick;

  _frostMediaClick = function _frostMediaClick(e) {
    /*
     * Commonality; check for valid target
     */
    var dataStore, element;
    element = e.target || e.srcElement;
    if (!element.hasAttribute("data-sigil") || !element.getAttribute("data-sigil").toLowerCase().includes("inlinevideo")) {
      return;
    }
    console.log("Found inline video");
    element = element.parentNode;
    if (!element.hasAttribute("data-store")) {
      return;
    }
    dataStore = void 0;
    try {
      dataStore = JSON.parse(element.getAttribute("data-store"));
    } catch (error) {
      e = error;
      return;
    }
    if (!dataStore.src) {
      return;
    }
    console.log("Inline video " + dataStore.src);
    if (typeof Frost !== "undefined" && Frost !== null) {
      Frost.loadVideo(dataStore.src, dataStore.animatedGifVideo);
    }
    e.stopPropagation();
    e.preventDefault();
  };

  document.addEventListener("click", _frostMediaClick, true);
}).call(undefined);