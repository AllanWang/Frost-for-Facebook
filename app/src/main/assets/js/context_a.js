//context menu for links
//largely mimics click_a.js
//we will also bind a listener here to notify the activity not to deal with viewpager scrolls
//since the long press is also associated witho
if (!window.hasOwnProperty('frost_context_a')) {
  console.log('frost_context_a frost_click_a');
  window.frost_context_a = true;

  var longClick = false;

  var _frostAContext = function(e) {
    Frost.longClick(true);
    longClick = true;

    /*
     * Commonality; check for valid target
     */
    var element = e.target || e.currentTarget || e.srcElement;
    if (!element) return;
    if (element.tagName !== 'A') element = element.parentNode;
    //Notifications is two layers under
    if (element.tagName !== 'A') element = element.parentNode;
    if (element.tagName === 'A' && element.getAttribute('href') !== '#') {
      var url = element.getAttribute('href');
      if (!url) return;
      if (url.includes('photoset_token')) return;

      var text = element.parentNode.innerText;

      // console.log('Context Intercept', element.tagName, element.id, element.className)
      console.log('Context Content', url, text);
      Frost.contextMenu(url, text);
      e.stopPropagation();
      e.preventDefault();
    }
  }

  document.addEventListener('contextmenu', _frostAContext, true);

  document.addEventListener('touchend', function _frostEnd(e) {
    if (longClick) {
      Frost.longClick(false);
      longClick = false;
    }
  }, true);
}
