// Copy of menu.ts without timeouts or notifications
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

    menuA.click();
}).call(undefined);
