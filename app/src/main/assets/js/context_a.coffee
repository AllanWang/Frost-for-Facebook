# context menu for links
# largely mimics click_a.js
# we will also bind a listener here to notify the activity not to deal with viewpager scrolls
longClick = false

_frostAContext = (e) ->
	Frost?.longClick true
	longClick = true

	###
	# Commonality; check for valid target
	###

	element = e.target or e.currentTarget or e.srcElement
	if !element
		return
	if element.tagName != "A"
		element = element.parentNode
	#Notifications is two layers under
	if element.tagName != "A"
		element = element.parentNode
	if element.tagName == "A" and element.getAttribute("href") != "#"
		url = element.getAttribute("href")
		if !url
			return
		text = element.parentNode.innerText
		# check if image item exists, first in children and then in parent
		image = element.querySelector("[style*=\"background-image: url(\"]")
		if !image
			image = element.parentNode.querySelector("[style*=\"background-image: url(\"]")
		if image
			imageUrl = window.getComputedStyle(image, null).backgroundImage.trim().slice(4, -1)
			console.log "Context image: #{imageUrl}"
			Frost?.loadImage imageUrl, text
			e.stopPropagation()
			e.preventDefault()
			return
		# check if true img exists
		img = element.querySelector("img[src*=scontent]")
		if img
			imgUrl = img.src
			console.log "Context img #{imgUrl}"
			Frost?.loadImage imgUrl, text
			e.stopPropagation()
			e.preventDefault()
			return
		console.log "Context Content #{url} #{text}"
		Frost?.contextMenu url, text
		e.stopPropagation()
		e.preventDefault()
	return

document.addEventListener "contextmenu", _frostAContext, true
document.addEventListener "touchend", ((e) ->
	if longClick
		Frost?.longClick false
		longClick = false
	return
), true
