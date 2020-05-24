// Credits to https://codepen.io/tomhodgins/pen/KgazaE
(function () {
    const textareas = <NodeListOf<HTMLTextAreaElement>>document.querySelectorAll('textarea.frostAutoExpand');

    const _frostAutoExpand = (el: HTMLElement) => {
        el.style.height = 'inherit'
        el.style.height = `${el.scrollHeight}px`
    };
    function _frostExpandAll() {
        textareas.forEach(_frostAutoExpand);
    }
    textareas.forEach(el => {
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
