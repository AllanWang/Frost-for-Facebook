var viewport = document.getElementById('viewport');
var root = document.getElementById('root');

var y = new MutationObserver(function(mutations) {
  viewport.removeAttribute('style');
  root.removeAttribute('style');
})

y.observe(viewport, {
  attributes: true
});

y.observe(root, {
  attributes: true
});

var x = new MutationObserver(function(mutations) {
  if (document.getElementsByClassName('mSideMenu').length) {
    x.disconnect();
    console.log('Found side menu');
    var menu = document.getElementsByClassName('mSideMenu')[0];
    while (root.firstChild)
      root.removeChild(root.firstChild);
    while (menu.childNodes.length)
      root.appendChild(menu.childNodes[0]);
    setTimeout(function() {
      y.disconnect();
      console.log('Unhook styler');
    }, 500)
  }
});

x.observe(document.getElementById('mJewelNav'), {
  childList: true,
  subtree: true
});
document.getElementById('bookmarks_jewel').getElementsByTagName('a')[0].click();
