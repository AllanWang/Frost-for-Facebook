"use strict";
// For desktop only
(function () {
    var _frostAContext = function (e) {
        // Commonality; check for valid target
        var element = e.target || e.currentTarget || e.srcElement;
        if (!(element instanceof Element)) {
            console.log("No element found");
            return;
        }
        console.log("Clicked element " + element.tagName + " " + element.className);
    };
    document.addEventListener('contextmenu', _frostAContext, true);
}).call(undefined);
