(async function () {
    let prevented = false;


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
     * Attempts to find a url entry at most [depth] away
     * A url is found if the element has tag 'A', and the url isn't blank
     */
    function _parentUrl(el: HTMLElement, depth: number): string | null {
        const element = _parentEl(el, depth, (el) => el.tagName === 'A');
        if (!element) {
            return null;
        }
        const url = element.getAttribute('href');
        if (!url || url === '#') {
            return null;
        }
        return url
    }

    /**
     * Given event and target, return true if handled and false otherwise.
     */
    type EventHandler = (e: Event, target: HTMLElement) => Promise<Boolean>

    const _frostGeneral: EventHandler = async (e, target) => {
        // We now disable clicks for the main notification page
        if (document.getElementById("notifications_list")) {
            return false
        }
        const url = _parentUrl(target, 2);
        return frost.loadUrl(url);
    };

    const _frostLaunchpadClick: EventHandler = async (e, target) => {
        if (!_parentEl(target, 6, (el) => el.id === 'launchpad')) {
            return false
        }
        console.log('Clicked launchpad');
        const url = _parentUrl(target, 5);
        return frost.loadUrl(url);
    };

    const handlers: EventHandler[] = [_frostLaunchpadClick, _frostGeneral];

    const _frostAClick = async (e: Event) => {
        if (prevented) {
            console.log("Click intercept prevented");
            return
        }
        /*
         * Commonality; check for valid target
         */
        const target = e.target || e.currentTarget || e.srcElement;
        if (!(target instanceof HTMLElement)) {
            console.log("No element found");
            return
        }
        // TODO cannot use await here; copy logic over here
        for (const h of handlers) {
            if (await h(e, target)) {
                e.stopPropagation();
                e.preventDefault();
                return
            }
        }
    };

    /*
     * On top of the click event, we must stop it for long presses
     * Since that will conflict with the context menu
     * Note that we only override it on conditions where the context menu
     * Will occur
     */
    const _frostPreventClick = () => {
        console.log("Click _frostPrevented");
        prevented = true;
    };

    const _frostAllowClick = () => {
        prevented = false;
        clearTimeout(clickTimeout)
    };

    document.addEventListener('click', _frostAClick, true);
    let clickTimeout: number | undefined = undefined;
    document.addEventListener('touchstart', () => {
        clickTimeout = setTimeout(_frostPreventClick, 400);
    }, true);
    document.addEventListener('touchend', _frostAllowClick, true);
}).call(undefined);

