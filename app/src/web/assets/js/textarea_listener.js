"use strict";
(function () {
    var _frostFocus = function (e) {
        var element = e.target || e.srcElement;
        if (!(element instanceof Element)) {
            return;
        }
        console.log("FrostJSI focus, " + element.tagName);
        if (element.tagName == 'TEXTAREA') {
            Frost.disableSwipeRefresh(true);
        }
    };
    var _frostBlur = function (e) {
        var element = e.target || e.srcElement;
        if (!(element instanceof Element)) {
            return;
        }
        console.log("FrostJSI blur, " + element.tagName);
        Frost.disableSwipeRefresh(false);
    };
    document.addEventListener("focus", _frostFocus, true);
    document.addEventListener("blur", _frostBlur, true);
}).call(undefined);
