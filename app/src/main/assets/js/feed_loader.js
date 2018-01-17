"use strict";

(function () {
  // checks when feed is visible through mutation
  var id, observer, query;

  id = "MComposer";

  query = "#" + id;

  if (document.querySelector(query)) {
    console.log(query + " already exists");
    if (typeof Frost !== "undefined" && Frost !== null) {
      Frost.loaded();
    }
    return;
  }

  console.log("Injected " + query + " listener");

  observer = new MutationObserver(function (mutation) {
    var add, i, j, len, len1, m, ref;
    for (i = 0, len = mutation.length; i < len; i++) {
      m = mutation[i];
      ref = m.addedNodes;
      for (j = 0, len1 = ref.length; j < len1; j++) {
        add = ref[j];
        if (!(add instanceof Element)) {
          continue;
        }
        if (add.id === id) {
          console.log("Found " + query);
          if (typeof Frost !== "undefined" && Frost !== null) {
            Frost.loaded();
          }
          return;
        }
      }
    }
  });

  window;

  observer.observe(document, {
    childList: true,
    subtree: true
  });
}).call(undefined);