// we will handle click events
console.log('Registering click interceptor');
document.addEventListener('click', function _menuClick(e) {
  var element = e.target || e.srcElement;
  console.log(element.tagName);
}, true);
