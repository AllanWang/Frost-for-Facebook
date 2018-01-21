"use strict";

(function () {
  // we will handle media events
  var _frostMediaClick;

  _frostMediaClick = function _frostMediaClick(e) {
    var dataStore, element, playerChild, url;
    element = e.target || e.srcElement;
    if (!element) {
      return;
    }
    // Get first player child. May be self or parent
    // Depending on what is clicked
    playerChild = element.parentElement.parentElement.querySelector("[data-sigil*=playInlineVideo]");
    if (!playerChild) {
      return;
    }
    try {
      dataStore = JSON.parse(playerChild.parentElement.getAttribute("data-store"));
    } catch (error) {
      return;
    }
    url = dataStore != null ? dataStore.src : void 0;
    if (!url || !url.startsWith("http")) {
      return;
    }
    console.log("Inline video " + url);
    if (typeof Frost !== "undefined" && Frost !== null) {
      Frost.loadVideo(url, dataStore.animatedGifVideo);
    }
    e.stopPropagation();
    e.preventDefault();
  };

  document.addEventListener("click", _frostMediaClick, true);
}).call(undefined);