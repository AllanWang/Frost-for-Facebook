# checks when feed is visible through mutation
id = "MComposer"
query = "##{id}"
if document.querySelector(query)
    console.log("#{query} already exists")
    Frost?.loaded()
    return

console.log("Injected #{query} listener")

observer = new MutationObserver((mutation) ->
    for m in mutation
        for add in m.addedNodes
            if add not instanceof Element
                continue
            if add.id == id
                console.log("Found #{query}")
                Frost?.loaded()
                return
)
window
observer.observe document,
    childList: true
    subtree: true