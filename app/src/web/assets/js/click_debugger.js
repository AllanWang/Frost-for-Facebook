"use strict";
(function () {
    var _frostAContext = function (e) {
        var element = e.target || e.currentTarget || e.srcElement;
        if (!(element instanceof Element)) {
            console.log("No element found");
            return;
        }
        console.log("Clicked element " + element.tagName + " " + element.className);
    };
    document.addEventListener('contextmenu', _frostAContext, true);
}).call(undefined);
