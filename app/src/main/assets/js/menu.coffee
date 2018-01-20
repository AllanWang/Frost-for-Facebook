# click menu and move contents to main view
viewport = document.querySelector("#viewport")
root = document.querySelector("#root")
if !viewport
    console.log "Menu.js: viewport is null"
if !root
    console.log "Menu.js: root is null"
y = new MutationObserver((mutations) ->
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
menuA = document.querySelector("#bookmarks_jewel").querySelector("a")
if !menuA
    console.log "Menu.js: jewel is null"
menuA.click()