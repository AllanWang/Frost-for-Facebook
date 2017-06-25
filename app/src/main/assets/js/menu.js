//click menu and move contents to main view
console.log('Fetching menu');
var viewport = document.querySelector('#viewport');
var root = document.querySelector('#root');
var y = new MutationObserver(function(mutations) {
  viewport.removeAttribute('style');
  root.removeAttribute('style');
});
y.observe(viewport, {
  attributes: true
});
y.observe(root, {
  attributes: true
});
var x = new MutationObserver(function(mutations) {
  var menu = document.querySelector('.mSideMenu');
  if (menu !== null) {
    x.disconnect();
    console.log('Found side menu');
    while (root.firstChild)
      root.removeChild(root.firstChild);
    while (menu.childNodes.length)
      root.appendChild(menu.childNodes[0]);
    Frost.emit(0);
    setTimeout(function() {
      y.disconnect();
      console.log('Unhook styler');
      Frost.handleHtml(document.documentElement.outerHTML);
    }, 500);
  }
});
x.observe(document.querySelector('#mJewelNav'), {
  childList: true,
  subtree: true
});
document.querySelector('#bookmarks_jewel').querySelector('a').click();
