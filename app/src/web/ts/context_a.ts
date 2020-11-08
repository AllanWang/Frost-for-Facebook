/**
 * Context menu for links
 * Largely mimics click_a.js
 */

(function () {
    let longClick = false;

    /**
     * Go up at most [depth] times, to retrieve a parent matching the provided predicate
     * If one is found, it is returned immediately.
     * Otherwise, null is returned.
     */
    function _parentEl(el: HTMLElement, depth: number, predicate: (el: HTMLElement) => boolean): HTMLElement | null {
        for (let i = 0; i < depth + 1; i++) {
            if (predicate(el)) {
                return el
            }
            const parent = el.parentElement;
            if (!parent) {
                return null
            }
            el = parent
        }
        return null
    }

    /**
     * Given event and target, return true if handled and false otherwise.
     */
    type EventHandler = (e: Event, target: HTMLElement) => Boolean

    const _frostCopyComment: EventHandler = (e, target) => {
        if (!target.hasAttribute('data-commentid')) {
            return false;
        }
        const text = target.innerText;
        console.log(`Copy comment ${text}`);
        Frost.contextMenu(null, text);
        return true;
    };

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
        const url = target.getAttribute('href');
        const text = parent1.innerText;
        console.log(`Copy post ${url} ${text}`);
        Frost.contextMenu(url, text);
        return true;
    };

    const _getImageStyleUrl = (el: Element): string | null => {
        // Emojis and special characters may be images from a span
        const img = el.querySelector("[style*=\"background-image: url(\"]:not(span)");
        if (!img) {
            return null
        }
        return (<String>window.getComputedStyle(img, null).backgroundImage).trim().slice(4, -1);
    };

    /**
     * Opens image activity for posts with just one image
     */
    const _frostImage: EventHandler = (e, target) => {
        const element = _parentEl(target, 2, (el) => el.tagName === 'A');
        if (!element) {
            return false;
        }
        const url = element.getAttribute('href');
        if (!url || url === '#') {
            return false;
        }
        const text = (<HTMLElement>element.parentElement).innerText;
        // Check if image item exists, first in children and then in parent
        const imageUrl = _getImageStyleUrl(element) || _getImageStyleUrl(<Element>element.parentElement);
        if (imageUrl) {
            console.log(`Context image: ${imageUrl}`);
            Frost.loadImage(imageUrl, text);
            return true;
        }
        // Check if true img exists
        const img = element.querySelector("img[src*=scontent]");
        if (img instanceof HTMLMediaElement) {
            const imgUrl = img.src;
            console.log(`Context img: ${imgUrl}`);
            Frost.loadImage(imgUrl, text);
            return true;
        }
        console.log(`Context content ${url} ${text}`);
        Frost.contextMenu(url, text);
        return true;
    };

    const handlers: EventHandler[] = [_frostImage, _frostCopyComment, _frostCopyPost];

    const _frostAContext = (e: Event) => {
        Frost.longClick(true);
        longClick = true;

        /**
         * Don't handle context events while scrolling
         */
        if (Frost.isScrolling()) {
            console.log("Skip from scrolling");
            return;
        }

        /*
         * Commonality; check for valid target
         */
        const target = e.target || e.currentTarget || e.srcElement;
        if (!(target instanceof HTMLElement)) {
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
