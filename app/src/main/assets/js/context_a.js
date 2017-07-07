//context menu for links
//largely mimics click_a.js
if (!window.hasOwnProperty('frost_context_a')) {
  console.log('frost_context_a frost_click_a');
  window.frost_context_a = true;

  var _frostAContext = function(e) {
    var element = e.target || e.srcElement;
    if (element.tagName !== 'A') element = element.parentNode;
    //Notifications is two layers under
    if (element.tagName !== 'A') element = element.parentNode;
    if (element.tagName === 'A' && element.getAttribute('href') !== '#') {
      var url = element.getAttribute('href');
      if (url.includes('photoset_token')) return;
      console.log('Context Intercept', url);
      Frost.contextMenu(url);
      e.stopPropagation();
      e.preventDefault();
    }
  }

  document.addEventListener('contextmenu', _frostAContext, true);
}
