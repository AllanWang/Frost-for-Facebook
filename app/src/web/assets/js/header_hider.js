"use strict";
(function () {
    var header = document.querySelector('#header');
    if (!header) {
        return;
    }
    var jewel = header.querySelector('#mJewelNav');
    if (!jewel) {
        return;
    }
    header.style.display = 'none';
}).call(undefined);
