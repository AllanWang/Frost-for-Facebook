# we will handle media events
_frostMediaClick = (e) ->
  element = e.target or e.srcElement

  if !element
    return

  # Get first player child. May be self or parent
  # Depending on what is clicked
  playerChild = element.parentElement.parentElement.querySelector("[data-sigil*=playInlineVideo]")

  if !playerChild
    return

  try
    dataStore = JSON.parse(playerChild.parentElement.getAttribute("data-store"))
  catch
    return

  url = dataStore?.src

  if !url || !url.startsWith("http")
    return

  console.log "Inline video #{url}"
  Frost?.loadVideo url, dataStore.animatedGifVideo
  e.stopPropagation()
  e.preventDefault()
  return

document.addEventListener "click", _frostMediaClick, true

