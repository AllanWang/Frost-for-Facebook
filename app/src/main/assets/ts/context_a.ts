/**
 * Context menu for links
 * Largely mimics click_a.js
 */
(function () {
    let longClick = false;
    const _frostAContext = (e: Event) => {
        Frost.longClick(true);
        longClick = true;

        /*
         * Commonality; check for valid target
         */
        let element = e.target || e.currentTarget || e.srcElement;
        if (!(element instanceof Element)) {
            console.log("No element found");
            return
        }
        // Notifications are two layers under
        for (let i = 0; i < 2; i++) {
            if (element.tagName != 'A') {
                element = element.parentNode;
                if (!(element instanceof Element)) {
                    console.log("No element found");
                    return
                }
            }
        }
        if (element.tagName == 'A') {
            const url = element.getAttribute('href');
            if (!url || url == '#') {
                return
            }
            const text = (<HTMLElement>element.parentNode).innerText;
            // Check if image item exists, first in children and then in parent
            let image = element.querySelector("[style*=\"background-image: url(\"]");
            if (!image) {
                image = (<Element>element.parentNode).querySelector("[style*=\"background-image: url(\"]")
            }
            if (image) {
                const imageUrl = (<String>window.getComputedStyle(image, null).backgroundImage).trim().slice(4, -1);
                console.log(`Context image: ${imageUrl}`);
                Frost.loadImage(imageUrl, text);
                e.stopPropagation();
                e.preventDefault();
                return
            }
            // Check if true img exists
            const img = element.querySelector("img[src*=scontent]");
            if (img instanceof HTMLMediaElement) {
                const imgUrl = img.src;
                console.log(`Context img: ${imgUrl}`);
                Frost.loadImage(imgUrl, text);
                e.stopPropagation();
                e.preventDefault();
                return
            }
            console.log(`Context content ${url} ${text}`);
            Frost.contextMenu(url, text);
            e.stopPropagation();
            e.preventDefault();
        }
    };

    document.addEventListener('contextmenu', _frostAContext, true);
    document.addEventListener('touchend', () => {
        if (longClick) {
            Frost.longClick(false);
            longClick = false
        }
    }, true);
}).call(undefined);
