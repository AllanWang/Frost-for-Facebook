"use strict";

(function () {
  // bases the header contents if it exists
  var header;

  header = document.getElementById("mJewelNav");

  if (header !== null) {
    if (typeof Frost !== "undefined" && Frost !== null) {
      Frost.handleHeader(header.outerHTML);
    }
  }
}).call(undefined);