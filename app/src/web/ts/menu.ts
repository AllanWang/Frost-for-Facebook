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

    /*
     * Required to remove height restrictions
     */
    const y = new MutationObserver(() => {
        viewport.removeAttribute('style');
        root.removeAttribute('style');
    });

    y.observe(viewport, {
        attributes: true
    });
    y.observe(root, {
        attributes: true
    });

    const x = new MutationObserver(() => {
        const menu = document.querySelector('.mSideMenu');
        if (menu) {
            x.disconnect();
            console.log("Found side menu");
            // Transfer elements
            while (root.firstChild) {
                root.removeChild(root.firstChild);
            }
            while (menu.childNodes.length) {
                viewport.appendChild(menu.childNodes[0]);
            }
            Frost.emit(0);
            setTimeout(() => {
                y.disconnect();
                console.log('Unhook styler');
            }, 500);
        }
    });
    x.observe(jewel, {
        childList: true,
        subtree: true
    });
    menuA.click();
}).call(undefined);
