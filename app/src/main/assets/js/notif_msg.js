//binds callbacks to an invisible webview to take in the search events
if (!window.hasOwnProperty('frost_notif_msg')) {
  console.log('Registering frost_notif_msg');
  window.frost_notif_msg = true;
  var finished = false;
  var x = new MutationObserver(function(mutations) {
    var _f_thread = document.querySelector('#threadlist_rows');
    if (!_f_thread) return;
    console.log('Found message threads', _f_thread.outerHTML);
    if (typeof Frost !== 'undefined') Frost.handleHtml(_f_thread.outerHTML);
    finished = true;
    x.disconnect();
  });
  x.observe(document, {
    childList: true,
    subtree: true
  });
  setTimeout(function() {
    if (!finished) {
      finished = true;
      console.log('Message thread timeout cancellation')
      if (typeof Frost !== 'undefined') Frost.handleHtml("");
    }
  }, 20000);
}
