//click menu and move contents to main view
if (!window.hasOwnProperty('frost_menu')) {
  console.log('Registering frost_menu');
  window.frost_menu = true;
  var viewport = document.querySelector('#viewport');
  var root = document.querySelector('#root');
  if (!viewport) console.log('Menu.js: viewport is null');
  if (!root) console.log('Menu.js: root is null');
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
      while (menu.childNodes.length) {
        console.log('append');
        viewport.appendChild(menu.childNodes[0]);
      }
      if (typeof Frost !== 'undefined') Frost.handleHtml(viewport.outerHTML);
      setTimeout(function() {
        y.disconnect();
        console.log('Unhook styler');
      }, 500);
    }
  });
  var jewel = document.querySelector('#mJewelNav');
  if (!jewel) console.log('Menu.js: jewel is null');
  x.observe(jewel, {
    childList: true,
    subtree: true
  });
  var menuA = document.querySelector('#bookmarks_jewel').querySelector('a');
  if (!menuA) console.log('Menu.js: jewel is null')
  menuA.click();
}
