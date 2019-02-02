# click menu and move contents to main view
viewport = window.document.querySelector("#viewport")
root = window.document.querySelector("#root")
menuA = window.document.querySelector("#bookmarks_jewel").querySelector("a")
if !viewport
  console.log "Menu.js: viewport is null"
  Frost?.emit 0
  return
if !root
  console.log "Menu.js: root is null"
  Frost?.emit 0
  return
if !menuA
  console.log "Menu.js: jewel is null"
  Frost?.emit 0
  return

y = new MutationObserver(() ->
  viewport.removeAttribute "style"
  root.removeAttribute "style"
  return
)

y.observe viewport, attributes: true
y.observe root, attributes: true

x = new MutationObserver(() ->
  menu = document.querySelector(".mSideMenu")
  if menu != null
    x.disconnect()
    console.log "Found side menu"
    while root.firstChild
      root.removeChild root.firstChild
    while menu.childNodes.length
      console.log "append"
      viewport.appendChild menu.childNodes[0]
    Frost?.emit 0
    setTimeout (->
      y.disconnect()
      console.log "Unhook styler"
      return
    ), 500
  return
)
jewel = document.querySelector("#mJewelNav")
if !jewel
  console.log "Menu.js: jewel is null"
x.observe jewel,
  childList: true
  subtree: true

menuA.click()
