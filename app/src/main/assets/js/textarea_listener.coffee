# focus listener for textareas
# since swipe to refresh is quite sensitive, we will disable it
# when we detect a user typing
# note that this extends passed having a keyboard opened,
# as a user may still be reviewing his/her post
# swiping should automatically be reset on refresh

_frostFocus = (e) ->
    element = e.target or e.srcElement
    console.log "Frost focus", element.tagName
    if element.tagName == "TEXTAREA"
        Frost?.disableSwipeRefresh true
    return

_frostBlur = (e) ->
    element = e.target or e.srcElement
    console.log "Frost blur", element.tagName
    Frost?.disableSwipeRefresh false
    return

document.addEventListener "focus", _frostFocus, true
document.addEventListener "blur", _frostBlur, true
