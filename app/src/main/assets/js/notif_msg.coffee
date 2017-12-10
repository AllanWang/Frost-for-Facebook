# binds callbacks to an invisible webview to take in the search events
finished = false
x = new MutationObserver((mutations) ->
	_f_thread = document.querySelector("#threadlist_rows")
	if !_f_thread
		return
	console.log "Found message threads #{_f_thread.outerHTML}"
	Frost?.handleHtml _f_thread.outerHTML
	finished = true
	x.disconnect()
	return
)
x.observe document,
	childList: true
	subtree: true
setTimeout (->
	if !finished
		finished = true
		console.log "Message thread timeout cancellation"
		Frost?.handleHtml ""
	return
), 20000
