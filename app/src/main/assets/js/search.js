//binds callbacks to an invisible webview to take in the search events
console.log('Binding Search');
var _f_page = document.querySelector('#page');
var _f_input = document.querySelector('#main-search-input')
if (!_f_page || !_f_input) return Frost.emit(1);
Frost.emit(0);
var x = new MutationObserver(function(mutations) {
  Frost.handleHtml(page.innerHTML);
});
x.observe(_f_page, {
  childList: true,
  subtree: true
});
