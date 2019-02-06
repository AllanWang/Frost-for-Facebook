/*
 * focus listener for textareas
 * since swipe to refresh is quite sensitive, we will disable it
 * when we detect a user typing
 * note that this extends passed having a keyboard opened,
 * as a user may still be reviewing his/her post
 * swiping should automatically be reset on refresh
 */
(function () {
    const _frostFocus = (e: Event) => {
        const element = e.target || e.srcElement;
        if (!(element instanceof Element)) {
            return
        }
        console.log(`FrostJSI focus, ${element.tagName}`);
        if (element.tagName === 'TEXTAREA') {
            Frost.disableSwipeRefresh(true);
        }
    };

    const _frostBlur = (e: Event) => {
        const element = e.target || e.srcElement;
        if (!(element instanceof Element)) {
            return
        }
        console.log(`FrostJSI blur, ${element.tagName}`);
        Frost.disableSwipeRefresh(false);
    };
    document.addEventListener("focus", _frostFocus, true);
    document.addEventListener("blur", _frostBlur, true);
}).call(undefined);
