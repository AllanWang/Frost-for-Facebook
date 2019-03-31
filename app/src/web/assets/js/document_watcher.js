"use strict";
(function () {
    var isReady = function () {
        return document.body.scrollHeight > innerHeight + 100;
    };
    if (isReady()) {
        console.log('Already ready');
        Frost.isReady();
        return;
    }
    console.log('Injected document watcher');
    var observer = new MutationObserver(function () {
        if (isReady()) {
            observer.disconnect();
            Frost.isReady();
            console.log("Documented surpassed height in " + performance.now());
        }
    });
    observer.observe(document, {
        childList: true,
        subtree: true
    });
}).call(undefined);
