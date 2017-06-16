// we will handle click events
console.log('Registering menu click');
document.addEventListener('click', function(e) {
  var element = e.target || e.srcElement;
  if (element.tagName !== 'A')
    element = element.parentNode;
  if (element.tagName === 'A' && element.getAttribute('href') !== '#') {
    var url = element.href;
    console.log('Click Intercept');
    console.log(url);
    Frost.loadUrl(url);
    e.stopPropagation();
    e.preventDefault();
  }
}, true);
