"use strict";

(function () {
  // focus listener for textareas
  // since swipe to refresh is quite sensitive, we will disable it
  // when we detect a user typing
  // note that this extends passed having a keyboard opened,
  // as a user may still be reviewing his/her post
  // swiping should automatically be reset on refresh
  var _frostBlur, _frostFocus;

  _frostFocus = function _frostFocus(e) {
    var element;
    element = e.target || e.srcElement;
    console.log("FrostJSI focus", element.tagName);
    if (element.tagName === "TEXTAREA") {
      if (typeof Frost !== "undefined" && Frost !== null) {
        Frost.disableSwipeRefresh(true);
      }
    }
  };

  _frostBlur = function _frostBlur(e) {
    var element;
    element = e.target || e.srcElement;
    console.log("FrostJSI blur", element.tagName);
    if (typeof Frost !== "undefined" && Frost !== null) {
      Frost.disableSwipeRefresh(false);
    }
  };

  document.addEventListener("focus", _frostFocus, true);

  document.addEventListener("blur", _frostBlur, true);
}).call(undefined);
