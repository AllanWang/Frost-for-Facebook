//context menu for links
//largely mimics click_a.js
if (!window.hasOwnProperty('frost_context_a')) {
  console.log('frost_context_a frost_click_a');
  window.frost_context_a = true;

  var _frostAContext = function(e) {


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
}
