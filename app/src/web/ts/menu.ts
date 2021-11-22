// Click menu and move contents to main view
(function () {
    const viewport = document.querySelector("#viewport");
    const root = document.querySelector("#root");
    const bookmarkJewel = document.querySelector("#bookmarks_jewel");
    if (!viewport || !root || !bookmarkJewel) {
        console.log('Menu.js: main elements not found');
        Frost.emit(0);
        return
    }
    const menuA = bookmarkJewel.querySelector("a");
    if (!menuA) {
        console.log('Menu.js: menu links not found');
        Frost.emit(0);
        return
    }
    const jewel = document.querySelector('#mJewelNav');
    if (!jewel) {
        console.log('Menu.js: jewel is null');
        return
    }

    // menu container
    const bookmarkFlyout = document.querySelector('#bookmarks_flyout');
    if (bookmarkFlyout instanceof HTMLElement) {
        bookmarkFlyout.style.marginTop = "0";
    }

    // Js handling is a bit slow so we need to wait 
    setTimeout(() => {
        menuA.click();
        console.log("Menu setup clicked");
        // Reaction is also slow so we need to wait
        setTimeout(() => {
            Frost.emit(0);
        }, 100);
    }, 200);
}).call(undefined);
