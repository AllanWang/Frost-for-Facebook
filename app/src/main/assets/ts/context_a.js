"use strict";
/**
 * Context menu for links
 * Largely mimics click_a.js
 */
(function () {
    var longClick = false;
    var _frostAContext = function (e) {
        Frost.longClick(true);
        longClick = true;
        /*
         * Commonality; check for valid target
         */
        var element = e.target || e.currentTarget || e.srcElement;
        if (!(element instanceof Element)) {
            console.log("No element found");
            return;
        }
        // Notifications are two layers under
        for (var i = 0; i < 2; i++) {
            if (element.tagName != 'A') {
                element = element.parentNode;
                if (!(element instanceof Element)) {
                    console.log("No element found");
                    return;
                }
            }
        }
        if (element.tagName == 'A') {
            var url = element.getAttribute('href');
            if (!url || url == '#') {
                return;
            }
            var text = element.parentNode.innerText;
            // Check if image item exists, first in children and then in parent
            var image = element.querySelector("[style*=\"background-image: url(\"]");
            if (!image) {
                image = element.parentNode.querySelector("[style*=\"background-image: url(\"]");
            }
            if (image) {
                var imageUrl = window.getComputedStyle(image, null).backgroundImage.trim().slice(4, -1);
                console.log("Context image: " + imageUrl);
                Frost.loadImage(imageUrl, text);
                e.stopPropagation();
                e.preventDefault();
                return;
            }
            // Check if true img exists
            var img = element.querySelector("img[src*=scontent]");
            if (img instanceof HTMLMediaElement) {
                var imgUrl = img.src;
                console.log("Context img: " + imgUrl);
                Frost.loadImage(imgUrl, text);
                e.stopPropagation();
                e.preventDefault();
                return;
            }
            console.log("Context content " + url + " " + text);
            Frost.contextMenu(url, text);
            e.stopPropagation();
            e.preventDefault();
        }
    };
    document.addEventListener('contextmenu', _frostAContext, true);
    document.addEventListener('touchend', function () {
        if (longClick) {
            Frost.longClick(false);
            longClick = false;
        }
    }, true);
}).call(undefined);
