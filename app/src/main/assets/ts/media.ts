// Handles media events
(function () {
    const _frostMediaClick = (e: Event) => {
        const target = e.target || e.srcElement;
        if (!(target instanceof HTMLElement)) {
            return
        }
        let element: HTMLElement = target;
        const dataset = element.dataset;
        if (!dataset || !dataset.sigil || dataset.sigil.toLowerCase().indexOf('inlinevideo') == -1) {
            return
        }
        let i = 0;
        while (!element.hasAttribute('data-store')) {
            if (++i > 2) {
                return
            }
            element = <HTMLElement>element.parentNode;
        }
        const store = element.dataset.store;
        if (!store) {
            return
        }

        let dataStore;

        try {
            dataStore = JSON.parse(store)
        } catch (e) {
            return
        }

        const url = dataStore.src;

        // !startsWith; see https://stackoverflow.com/a/36876507/4407321
        if (!url || url.lastIndexOf('http', 0) !== 0) {
            return
        }

        console.log(`Inline video ${url}`);
        if (Frost.loadVideo(url, dataStore.animatedGifVideo || false)) {
            e.stopPropagation()
        }
    };

    document.addEventListener('click', _frostMediaClick, true);
}).call(undefined);
