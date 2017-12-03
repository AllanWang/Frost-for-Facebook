//context menu for links
//largely mimics click_a.js
//we will also bind a listener here to notify the activity not to deal with viewpager scrolls
//since the long press is also associated witho
if (!window.hasOwnProperty('frost_context_a')) {
  console.log('frost_context_a frost_click_a');
  window.frost_context_a = true;

  var longClick = false;

  var _frostAContext = function(e) {
    if (typeof Frost !== 'undefined') Frost.longClick(true);
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
      var text = element.parentNode.innerText;

      //check if image item exists, first in children and then in parent
      var image = element.querySelector('[style*="background-image: url("]');
      if (!image) image = element.parentNode.querySelector('[style*="background-image: url("]');
      if (image) {
        var imageUrl = window.getComputedStyle(image, null).backgroundImage.trim().slice(4, -1);
        console.log('Context image: ' + imageUrl);
        if (typeof Frost !== 'undefined') Frost.loadImage(imageUrl, text);
        e.stopPropagation();
        e.preventDefault();
        return;
      }

      //check if true img exists
      var img = element.querySelector('img[src*=scontent]')
      if (img) {
        var imgUrl = img.src;
        console.log('Context img', imgUrl);
        if (typeof Frost !== 'undefined') Frost.loadImage(imgUrl, text);
        e.stopPropagation();
        e.preventDefault();
        return;
      }

      console.log('Context Content', url, text);
      if (typeof Frost !== 'undefined') Frost.contextMenu(url, text);

      e.stopPropagation();
      e.preventDefault();
    }
  }

  document.addEventListener('contextmenu', _frostAContext, true);

  document.addEventListener('touchend', function _frostEnd(e) {
    if (longClick) {
      if (typeof Frost !== 'undefined') Frost.longClick(false);
      longClick = false;
    }
  }, true);
}
