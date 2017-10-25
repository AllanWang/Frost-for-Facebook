//for desktop only
var _frostAContext = function(e) {
  /*
   * Commonality; check for valid target
   */
  var element = e.target || e.currentTarget || e.srcElement;
  if (!element) return;
  console.log("Clicked element:");
  console.log(element.tagName);
  console.log(element.className);
}

document.addEventListener('contextmenu', _frostAContext, true);
