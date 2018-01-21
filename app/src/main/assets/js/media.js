"use strict";

(function () {
  var _frostMediaClick, addPip, frostPip, observer;

  frostPip = function frostPip(element) {
    var data, isGif, url;
    data = JSON.parse(element.dataset.store);
    url = data.src;
    isGif = data.animatedGifVideo;
    console.log("Launching pip video for " + url + " " + isGif);
    return typeof Frost !== "undefined" && Frost !== null ? Frost.loadVideo(url, isGif) : void 0;
  };

  addPip = function addPip(element) {
    var child, j, len, ref;
    element.className += " frost-video";
    delete element.dataset.sigil;
    ref = element.querySelectorAll("[data-sigil");
    for (j = 0, len = ref.length; j < len; j++) {
      child = ref[j];
      delete child.dataset.sigil;
    }
  };

  observer = new MutationObserver(function () {
    var j, len, player, ref;
    ref = document.querySelectorAll("[data-sigil=inlineVideo]:not(.frost-video)");
    for (j = 0, len = ref.length; j < len; j++) {
      player = ref[j];
      addPip(player);
    }
  });

  observer.observe(document, {
    childList: true,
    subtree: true
  });

  _frostMediaClick = function _frostMediaClick(e) {
    var element, i;
    element = e.target || e.srcElement;
    i = 0;
    while (!element.className.includes("frost-video")) {
      if (i++ > 3) {
        return;
      }
      element = element.parentElement;
    }
    frostPip(element);
    e.stopPropagation();
    e.preventDefault();
  };

  document.addEventListener("click", _frostMediaClick, true);
}).call(undefined);