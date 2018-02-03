'use strict';

(function () {
  var header, jewel;

  header = document.querySelector('#header');

  if (!header) {
    return;
  }

  jewel = header.querySelector('#mJewelNav');

  if (!jewel) {
    return;
  }

  header.style.display = 'none';
}).call(undefined);