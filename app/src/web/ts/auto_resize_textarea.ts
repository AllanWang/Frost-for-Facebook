// Credits to https://codepen.io/tomhodgins/pen/KgazaE
(function () {
    const textareas = <NodeListOf<HTMLTextAreaElement>>document.querySelectorAll('textarea:not(.frostAutoExpand)');

    const dataAttribute = 'data-frost-minHeight';

    const _frostAutoExpand = (el: HTMLElement) => {
        if (!el.hasAttribute(dataAttribute)) {
            el.setAttribute(dataAttribute, el.offsetHeight.toString());
        }
        // If no height is defined, have min bound to current height;
        // otherwise we will allow for height decreases in case user deletes text
        const minHeight = parseInt(el.getAttribute(dataAttribute) ?? '0');
        el.style.height = 'inherit';
        el.style.height = `${Math.max(el.scrollHeight, minHeight)}px`;
    };
    function _frostExpandAll() {
        textareas.forEach(_frostAutoExpand);
    }
    textareas.forEach(el => {
        el.classList.add('frostAutoExpand')
        const __frostAutoExpand = () => {
            _frostAutoExpand(el)
        };
        el.addEventListener('paste', __frostAutoExpand)
        el.addEventListener('input', __frostAutoExpand)
        el.addEventListener('keyup', __frostAutoExpand)
    });
    window.addEventListener('load', _frostExpandAll)
    window.addEventListener('resize', _frostExpandAll)
}).call(undefined);
