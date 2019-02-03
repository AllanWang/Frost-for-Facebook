(function () {
    let prevented = false;

    const _frostAClick = (e: Event) => {
        // check for valid target
        const target = e.target || e.currentTarget || e.srcElement;
        if (!(target instanceof Element)) {
            console.log("No element found");
            return
        }
        let element: Element = target;
        // Notifications are two layers under
        for (let i = 0; i < 2; i++) {
            if (element.tagName != 'A') {
                element = <Element>element.parentElement;
            }
        }
        if (element.tagName == 'A') {
            if (!prevented) {
                const url = element.getAttribute('href');
                if (!url || url == '#') {
                    return
                }
                console.log(`Click intercept ${url}`);
                // If Frost is injected, check if loading the url through an overlay works
                if (Frost.loadUrl(url)) {
                    e.stopPropagation();
                    e.preventDefault();
                }
            } else {
                console.log("Click intercept _frostPrevented")
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

    document.addEventListener('click', _frostAClick, true);
    let clickTimeout: number | undefined = undefined;
    document.addEventListener('touchstart', () => {
        clickTimeout = setTimeout(_frostPreventClick, 400);
    }, true);
    document.addEventListener('touchend', () => {
        prevented = false;
        clearTimeout(clickTimeout)
    }, true);
}).call(undefined);

