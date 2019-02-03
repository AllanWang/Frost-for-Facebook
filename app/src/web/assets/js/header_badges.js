"use strict";
// Fetches the header contents if it exists
(function () {
    var header = document.getElementById('mJewelNav');
    if (header) {
        Frost.handleHeader(header.outerHTML);
    }
}).call(undefined);
