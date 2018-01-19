"use strict";

(function () {
  // emit key once half the viewport is covered
  var isReady, observer;

  isReady = function isReady() {
    if (!((typeof document !== "undefined" && document !== null ? document.body : void 0) != null)) {
      return false;
    }
    return document.body.scrollHeight > innerHeight + 100;
  };

  if (isReady()) {
    console.log("Already ready");
    if (typeof Frost !== "undefined" && Frost !== null) {
      Frost.isReady();
    }
    return;
  }

  console.log("Injected document watcher");

  observer = new MutationObserver(function () {
    if (isReady()) {
      observer.disconnect();
      if (typeof Frost !== "undefined" && Frost !== null) {
        Frost.isReady();
      }
      return console.log("Documented surpassed height in " + performance.now());
    }
  });

  observer.observe(document, {
    childList: true,
    subtree: true
  });
}).call(undefined);