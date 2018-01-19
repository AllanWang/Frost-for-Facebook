"use strict";

(function () {
  // click menu and move contents to main view
  var jewel, menuA, root, viewport, x, y;

  viewport = document.querySelector("#viewport");

  root = document.querySelector("#root");

  if (!viewport) {
    console.log("Menu.js: viewport is null");
  }

  if (!root) {
    console.log("Menu.js: root is null");
  }

  y = new MutationObserver(function (mutations) {
    viewport.removeAttribute("style");
    root.removeAttribute("style");
  });

  y.observe(viewport, {
    attributes: true
  });

  y.observe(root, {
    attributes: true
  });

  x = new MutationObserver(function () {
    var menu;
    menu = document.querySelector(".mSideMenu");
    if (menu !== null) {
      x.disconnect();
      console.log("Found side menu");
      while (root.firstChild) {
        root.removeChild(root.firstChild);
      }
      while (menu.childNodes.length) {
        console.log("append");
        viewport.appendChild(menu.childNodes[0]);
      }
      if (typeof Frost !== "undefined" && Frost !== null) {
        Frost.emit(0);
      }
      setTimeout(function () {
        y.disconnect();
        console.log("Unhook styler");
      }, 500);
    }
  });

  jewel = document.querySelector("#mJewelNav");

  if (!jewel) {
    console.log("Menu.js: jewel is null");
  }

  x.observe(jewel, {
    childList: true,
    subtree: true
  });

  menuA = document.querySelector("#bookmarks_jewel").querySelector("a");

  if (!menuA) {
    console.log("Menu.js: jewel is null");
  }

  menuA.click();
}).call(undefined);