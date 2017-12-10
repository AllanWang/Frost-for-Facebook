'use strict';

(function () {
  // for desktop only
  var _frostAContext;

  _frostAContext = function _frostAContext(e) {
    /*
     * Commonality; check for valid target
     */
    var element;
    element = e.target || e.currentTarget || e.srcElement;
    if (!element) {
      return;
    }
    console.log('Clicked element: ' + element.tagName + ' ' + element.className);
  };

  document.addEventListener('contextmenu', _frostAContext, true);
}).call(undefined);