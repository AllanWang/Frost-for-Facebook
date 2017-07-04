var e = document.getElementById('main-search-input');
if (e) {
  e.value = '$input';
  var n = new Event('input', {
    bubbles: !0,
    cancelable: !0
  });
  e.dispatchEvent(n);
  e.dispatchEvent(new Event('focus'));
} else console.log('Input field not found')
