// For desktop only

(function () {
    const _frostAContext = (e: Event) => {
        // Commonality; check for valid target
        const element = e.target || e.currentTarget || e.srcElement;
        if (!(element instanceof Element)) {
            console.log("No element found");
            return
        }
        console.log(`Clicked element ${element.tagName} ${element.className}`);
    };

    document.addEventListener('contextmenu', _frostAContext, true);
}).call(undefined);
