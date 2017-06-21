// we will handle click events
console.log('Registering frost_a click');

var _frostAClick = function(e) {
  var element = e.target || e.srcElement;
  if (element.tagName !== 'A')
    element = element.parentNode;
  if (element.tagName === 'A' && element.getAttribute('href') !== '#') {
    var url = element.getAttribute('href');
    console.log('Click Intercept', url);
    Frost.loadUrl(url);
    e.stopPropagation();
    e.preventDefault();
  }
}

document.addEventListener('click', _frostAClick, true);
