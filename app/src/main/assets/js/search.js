//binds callbacks to an invisible webview to take in the search events
console.log('Binding Search');
var page = document.querySelector('#page');
var x = new MutationObserver(function(mutations) {
  Frost.handleHtml(page.innerHTML);
});
x.observe(page, {
  childList: true,
  subtree: true
});
