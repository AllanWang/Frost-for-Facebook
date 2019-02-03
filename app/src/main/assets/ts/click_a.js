"use strict";
(function () {
    var prevented = false;
    var _frostAClick = function (e) {
        // check for valid target
        var target = e.target || e.currentTarget || e.srcElement;
        if (!(target instanceof Element)) {
            console.log("No element found");
            return;
        }
        var element = target;
        // Notifications are two layers under
        for (var i = 0; i < 2; i++) {
            if (element.tagName != 'A') {
                element = element.parentElement;
            }
        }
        if (element.tagName == 'A') {
            if (!prevented) {
                var url = element.getAttribute('href');
                if (!url || url == '#') {
                    return;
                }
                console.log("Click intercept " + url);
                // If Frost is injected, check if loading the url through an overlay works
                if (Frost.loadUrl(url)) {
                    e.stopPropagation();
                    e.preventDefault();
                }
            }
            else {
                console.log("Click intercept _frostPrevented");
            }
        }
    };
    /*
     * On top of the click event, we must stop it for long presses
     * Since that will conflict with the context menu
     * Note that we only override it on conditions where the context menu
     * Will occur
     */
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
