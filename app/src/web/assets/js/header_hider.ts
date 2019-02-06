(function () {
    const header = document.querySelector('#header');

    if (!header) {
        return
    }

    const jewel = header.querySelector('#mJewelNav');

    if (!jewel) {
        return
    }

    (<HTMLElement>header).style.display = 'none'
}).call(undefined);


