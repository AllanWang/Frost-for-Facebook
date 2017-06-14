// we will handle click events
document.onclick = function(e) {
  e = e || window.event;
  var element = e.target || e.srcElement;
  if (element.tagName !== 'A')
    element = element.parentNode;
  if (element.tagName === 'A') {
    var url = element.href;
    console.log('Click Intercept');
    console.log(url);
    if (url !== "https://m.facebook.com/settings" && url !== "https://m.facebook.com/settings#" && url !== "https://m.facebook.com/settings#!/settings?soft=bookmarks")
      Frost.loadUrl(url);
      Frost.reloadBaseUrl(); //temporary workaround
  }
};
