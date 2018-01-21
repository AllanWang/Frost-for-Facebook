frostPip = (element) ->
  data = JSON.parse(element.dataset.store)
  url = data.src
  isGif = data.animatedGifVideo
  console.log("Launching pip video for #{url} #{isGif}")
  Frost?.loadVideo url, isGif

addPip = (element) ->
  element.className += " frost-video"
  delete element.dataset.sigil
  for child in element.querySelectorAll("[data-sigil")
    delete child.dataset.sigil
  return

observer = new MutationObserver(() ->
  for player in document.querySelectorAll("[data-sigil=inlineVideo]:not(.frost-video)")
    addPip player
  return
)

observer.observe document,
  childList: true
  subtree: true

_frostMediaClick = (e) ->
  element = e.target or e.srcElement
  i = 0
  while !element.className.includes("frost-video")
    if i++ > 3
      return
    element = element.parentElement

  frostPip(element)
  e.stopPropagation()
  e.preventDefault()
  return

document.addEventListener "click", _frostMediaClick, true
