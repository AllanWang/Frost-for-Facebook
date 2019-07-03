(function () {

  /**
   * Go up at most [depth] times, to retrieve a parent matching the provided predicate
   * If one is found, it is returned immediately.
   * Otherwise, null is returned.
   */
  function _parentEl(el: HTMLElement, depth: number, predicate: (el: HTMLElement) => boolean): HTMLElement | null {
    for (let i = 0; i < depth + 1; i++) {
      if (predicate(el)) {
        return el
      }
      const parent = el.parentElement;
      if (!parent) {
        return null
      }
      el = parent
    }
    return null
  }

  /**
   * Check if element can scroll horizontally.
   * We primarily rely on the overflow-x field.
   * For performance reasons, we will check scrollWidth first to see if scrolling is a possibility
   */
  function _canScrollHorizontally(el: HTMLElement): boolean {
    /*
     * Sometimes the offsetWidth is off by < 10px. We use the multiplier
     * since the trays are typically more than 2 times greater
     */
    if (el.scrollWidth > el.offsetWidth * 1.2) {
      return true
    }
    const styles = window.getComputedStyle(el);
    /*
     * Works well in testing, but on mobile it just shows 'visible'
     */
    return styles.overflowX === 'scroll';
  }

  const _frostCheckHorizontalScrolling = (e: Event) => {
    const target = e.target || e.currentTarget || e.srcElement;
    if (!(target instanceof HTMLElement)) {
      return
    }
    const scrollable = _parentEl(target, 5, _canScrollHorizontally) !== null;
    if (scrollable) {
      console.log('Pause horizontal scrolling');
      Frost.allowHorizontalScrolling(false);
    }
  };

  const _frostResetHorizontalScrolling = (e: Event) => {
    Frost.allowHorizontalScrolling(true)
  };

  document.addEventListener('touchstart', _frostCheckHorizontalScrolling, true);
  document.addEventListener('touchend', _frostResetHorizontalScrolling, true);
}).call(undefined);

