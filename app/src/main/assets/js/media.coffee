# we will handle media events
_frostMediaClick = (e) ->

	###
	# Commonality; check for valid target
	###

	element = e.target or e.srcElement
	if !element.hasAttribute("data-sigil") or !element.getAttribute("data-sigil").toLowerCase().includes("inlinevideo")
		return
	console.log "Found inline video"
	element = element.parentNode
	if !element.hasAttribute("data-store")
		return
	dataStore = undefined
	try
		dataStore = JSON.parse(element.getAttribute("data-store"))
	catch e
		return
	if !dataStore.src
		return
	console.log "Inline video #{dataStore.src}"
	Frost?.loadVideo dataStore.src, dataStore.animatedGifVideo
	e.stopPropagation()
	e.preventDefault()
	return

document.addEventListener "click", _frostMediaClick, true

