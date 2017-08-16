// we will handle click events
if (!window.hasOwnProperty('frost_click_a')) {
  console.log('Registering frost_click_a');
  window.frost_click_a = true;

  var prevented = false;

  var _frostAClick = function(e) {


    /*
     * Commonality; check for valid target
     */
    var element = e.target || e.srcElement;
    if (element.tagName !== 'A') element = element.parentNode;
    //Notifications is two layers under
    if (element.tagName !== 'A') element = element.parentNode;
    if (element.tagName === 'A') {
      if (!prevented) {
        var url = element.getAttribute('href');
        console.log('Click Intercept', url);
        // if frost is injected, check if loading the url through an overlay works
        if (typeof Frost !== 'undefined' && Frost.loadUrl(url)) {
            e.stopPropagation();
            e.preventDefault();
        }
      }
    }
  }

  /*
   * On top of the click event, we must stop it for long presses
   * Since that will conflict with the context menu
   * Note that we only override it on conditions where the context menu
   * Will occur
   */
  var _frostPreventClick = function() {
    console.log('Click prevented')
    prevented = true;
  }

  document.addEventListener('click', _frostAClick, true);

  document.addEventListener('touchstart', function _frostStart(e) {
    setTimeout(_frostPreventClick, 400);
  }, true);

  document.addEventListener('touchend', function _frostEnd(e) {
    prevented = false;
  }, true);
}
