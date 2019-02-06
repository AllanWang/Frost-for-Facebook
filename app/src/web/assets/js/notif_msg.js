"use strict";
(function () {
    var finished = false;
    var x = new MutationObserver(function () {
        var _f_thread = document.querySelector('#threadlist_rows');
        if (!_f_thread) {
            return;
        }
        console.log("Found message threads " + _f_thread.outerHTML);
        Frost.handleHtml(_f_thread.outerHTML);
        finished = true;
        x.disconnect();
    });
    x.observe(document, {
        childList: true,
        subtree: true
    });
    setTimeout(function () {
        if (!finished) {
            finished = true;
            console.log('Message thread timeout cancellation');
            Frost.handleHtml("");
        }
    }, 20000);
}).call(undefined);
