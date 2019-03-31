"use strict";
(function () {
    var prevented = false;
    var _frostAClick = function (e) {
        var target = e.target || e.currentTarget || e.srcElement;
        if (!(target instanceof Element)) {
            console.log("No element found");
            return;
        }
        var element = target;
        for (var i = 0; i < 2; i++) {
            if (element.tagName !== 'A') {
                element = element.parentElement;
            }
        }
        if (element.tagName === 'A') {
            if (!prevented) {
                var url = element.getAttribute('href');
                if (!url || url === '#') {
                    return;
                }
                console.log("Click intercept " + url);
                if (Frost.loadUrl(url)) {
                    e.stopPropagation();
                    e.preventDefault();
                }
            }
            else {
                console.log("Click intercept prevented");
            }
        }
    };
    var _frostPreventClick = function () {
        console.log("Click _frostPrevented");
        prevented = true;
    };
    document.addEventListener('click', _frostAClick, true);
    var clickTimeout = undefined;
    document.addEventListener('touchstart', function () {
        clickTimeout = setTimeout(_frostPreventClick, 400);
    }, true);
    document.addEventListener('touchend', function () {
        prevented = false;
        clearTimeout(clickTimeout);
    }, true);
}).call(undefined);
