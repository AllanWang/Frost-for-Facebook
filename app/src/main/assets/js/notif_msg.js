"use strict";

(function () {
  // binds callbacks to an invisible webview to take in the search events
  var finished, x;

  finished = false;

  x = new MutationObserver(function (mutations) {
    var _f_thread;
    _f_thread = document.querySelector("#threadlist_rows");
    if (!_f_thread) {
      return;
    }
    console.log("Found message threads " + _f_thread.outerHTML);
    if (typeof Frost !== "undefined" && Frost !== null) {
      Frost.handleHtml(_f_thread.outerHTML);
    }
    finished = true;
    x.disconnect();
  });

  x.observe(document, {
    childList: true,
    subtree: true
  });

  setTimeout(function () {
    if (!finished) {
      finished = true;
      console.log("Message thread timeout cancellation");
      if (typeof Frost !== "undefined" && Frost !== null) {
        Frost.handleHtml("");
      }
    }
  }, 20000);
}).call(undefined);