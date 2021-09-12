// Fetches the header contents if it exists
(function() {
    const header = document.getElementById('header');
    if (header) {
        Frost.handleHeader(header.outerHTML);
    }
}).call(undefined);
