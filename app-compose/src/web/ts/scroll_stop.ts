// Listen when scrolling events stop
(function () {
    let scrollTimeout: number | undefined = undefined;
    let scrolling: boolean = false;

    window.addEventListener('scroll', function (event) {

        if (!scrolling) {
            Frost.setScrolling(true);
            scrolling = true;
        }

        window.clearTimeout(scrollTimeout);

        scrollTimeout = setTimeout(function () {
            if (scrolling) {
                Frost.setScrolling(false);
                scrolling = false;
            }
        }, 600);
        // For our specific use case, we want to release other features pretty far after scrolling stops
        // For general scrolling use cases, the delta can be much smaller
        // My assumption for context menus is that the long press is 500ms
    }, false);
}).call(undefined);