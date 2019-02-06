/**
 * Context menu for links
 * Largely mimics click_a.js
 */

(function () {
    let longClick = false;

    /**
     * Given event and target, return true if handled and false otherwise.
     */
    type EventHandler = (e: Event, target: Element) => Boolean

    /**
     * Posts should click a tag, with two parents up being div.story_body_container
     */
    const _frostCopyPost: EventHandler = (e, target) => {
        if (target.tagName !== 'A') {
            return false;
        }
        const parent1 = target.parentElement;
        if (!parent1 || parent1.tagName !== 'DIV') {
            return false;
        }
        const parent2 = parent1.parentElement;
        if (!parent2 || !parent2.classList.contains('story_body_container')) {
            return false;
        }
        const url = target.getAttribute('href')!;
        const text = parent1.innerText;
        console.log(`Copy post ${url} ${text}`);
        Frost.contextMenu(url, text);
        return true;
    };

    const _frostImage: EventHandler = (e, target) => {
        let element: Element = target;
        // Notifications are two layers under
        for (let i = 0; i < 2; i++) {
            if (element.tagName !== 'A') {
                element = <Element>element.parentElement;
            }
        }
        if (element.tagName !== 'A') {
            return false
        }
        const url = element.getAttribute('href');
        if (!url || url === '#') {
            return false
        }
        const text = (<HTMLElement>element.parentElement).innerText;
        // Check if image item exists, first in children and then in parent
        let image = element.querySelector("[style*=\"background-image: url(\"]");
        if (!image) {
            image = (<Element>element.parentElement).querySelector("[style*=\"background-image: url(\"]")
        }
        if (image) {
            const imageUrl = (<String>window.getComputedStyle(image, null).backgroundImage).trim().slice(4, -1);
            console.log(`Context image: ${imageUrl}`);
            Frost.loadImage(imageUrl, text);
            return true
        }
        // Check if true img exists
        const img = element.querySelector("img[src*=scontent]");
        if (img instanceof HTMLMediaElement) {
            const imgUrl = img.src;
            console.log(`Context img: ${imgUrl}`);
            Frost.loadImage(imgUrl, text);
            return true
        }
        console.log(`Context content ${url} ${text}`);
        Frost.contextMenu(url, text);
        return true
    };

    const handlers = [_frostCopyPost, _frostImage];

    const _frostAContext = (e: Event) => {
        Frost.longClick(true);
        longClick = true;

        /*
         * Commonality; check for valid target
         */
        const target = e.target || e.currentTarget || e.srcElement;
        if (!(target instanceof Element)) {
            console.log("No element found");
            return
        }
        for (const h of handlers) {
            if (h(e, target)) {
                e.stopPropagation();
                e.preventDefault();
                return
            }
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
