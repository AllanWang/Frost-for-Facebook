"use strict";
// Handles media events
(function () {
    var _frostMediaClick = function (e) {
        var element = e.target || e.srcElement;
        if (!(element instanceof HTMLElement)) {
            return;
        }
        var dataset = element.dataset;
        if (!dataset || !dataset.sigil || dataset.sigil.toLowerCase().indexOf('inlinevideo') == -1) {
            return;
        }
        var i = 0;
        while (!element.hasAttribute('data-store')) {
            if (++i > 2) {
                return;
            }
            element = element.parentNode;
            if (!(element instanceof HTMLElement)) {
                return;
            }
        }
        var store = element.dataset.store;
        if (!store) {
            return;
        }
        var dataStore;
        try {
            dataStore = JSON.parse(store);
        }
        catch (e) {
            return;
        }
        var url = dataStore.src;
        // !startsWith; see https://stackoverflow.com/a/36876507/4407321
        if (!url || url.lastIndexOf('http', 0) !== 0) {
            return;
        }
        console.log("Inline video " + url);
        if (Frost.loadVideo(url, dataStore.animatedGifVideo || false)) {
            e.stopPropagation();
        }
    };
    document.addEventListener('click', _frostMediaClick, true);
}).call(undefined);
