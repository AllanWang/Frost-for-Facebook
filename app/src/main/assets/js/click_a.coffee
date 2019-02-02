prevented = false

_frostAClick = (e) ->

  ###
  # Commonality; check for valid target
  ###
  element = e.target or e.srcElement
  if element.tagName != "A"
    element = element.parentNode
  # Notifications is two layers under
  if element.tagName != "A"
    element = element.parentNode
  if element.tagName == "A"
    if !prevented
      url = element.getAttribute("href")
      console.log "Click Intercept #{url}"
      # if frost is injected, check if loading the url through an overlay works
      if Frost?.loadUrl(url) == true
        e.stopPropagation()
        e.preventDefault()
    else
      console.log "Click Intercept Prevented"
  return

###
# On top of the click event, we must stop it for long presses
# Since that will conflict with the context menu
# Note that we only override it on conditions where the context menu
# Will occur
###

_frostPreventClick = ->
  console.log "Click _frostPrevented"
  prevented = true
  return

document.addEventListener "click", _frostAClick, true
clickTimeout = undefined
document.addEventListener "touchstart", ((e) ->
  clickTimeout = setTimeout(_frostPreventClick, 400)
  return
), true
document.addEventListener "touchend", ((e) ->
  prevented = false
  clearTimeout clickTimeout
  return
), true
