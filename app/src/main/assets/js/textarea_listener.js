//focus listener for textareas
//since swipe to refresh is quite sensitive, we will disable it
//when we detect a user typing
//note that this extends passed having a keyboard opened,
//as a user may still be reviewing his/her post
//swiping should automatically be reset on refresh
if (!window.hasOwnProperty('frost_textarea_listener')) {
  console.log('Registering frost_textarea_listener');
  window.frost_textarea_listener = true;

  var _frostFocus = function(e) {
    var element = e.target || e.srcElement;
    console.log('Frost focus', element.tagName);
    if (element.tagName === 'TEXTAREA')
      if (typeof Frost !== 'undefined') Frost.disableSwipeRefresh(true);
  }

  var _frostBlur = function(e) {
    var element = e.target || e.srcElement;
    console.log('Frost blur', element.tagName);
    if (typeof Frost !== 'undefined') Frost.disableSwipeRefresh(false);
  }

  document.addEventListener('focus', _frostFocus, true);
  document.addEventListener('blur', _frostBlur, true);

}
