var viewport = document.getElementById('viewport');
var root = document.getElementById('root');
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
  var menuChildren = document.getElementsByClassName('mSideMenu');
  if (menuChildren.length > 0) {
    x.disconnect();
    console.log('Found side menu');
    var menu = menuChildren[0];
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
x.observe(document.getElementById('mJewelNav'), {
  childList: true,
  subtree: true
});
document.getElementById('bookmarks_jewel').getElementsByTagName('a')[0].click();
