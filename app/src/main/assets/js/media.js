// we will media events
if (!window.hasOwnProperty('frost_media')) {
  console.log('Registering frost_media');
  window.frost_media = true;

  var _frostMediaClick = function(e) {

    /*
     * Commonality; check for valid target
     */
    var element = e.target || e.srcElement;
    if (!element.hasAttribute("data-sigil") || !element.getAttribute("data-sigil").toLowerCase().includes("inlinevideo")) return;
    console.log("Found inline video");
    element = element.parentNode;
    if (!element.hasAttribute("data-store")) return;
    var dataStore;
    try {
      dataStore = JSON.parse(element.getAttribute("data-store"));
    } catch (e) {
      return;
    }
    if (!dataStore.src) return;
    console.log("Inline video " + dataStore.src);
    if (typeof Frost !== 'undefined') Frost.loadVideo(dataStore.src, dataStore.animatedGifVideo);
    e.stopPropagation();
    e.preventDefault();
    return;
  }

  document.addEventListener('click', _frostMediaClick, true);
}
