// generic click handler
document.onclick = function(e) {
  e = e || window.event;
  var element = e.target || e.srcElement;
  if (element.tagName !== 'A')
    element = element.parentNode;
  if (element.tagName === 'A') {
    var url = element.href;
    console.log('Generic Click Intercept');
    console.log(url);
    // Frost.loadUrl(url);
  }
  return false;
};
