# emit key once half the viewport is covered

isReady = ->
  if not (document?.body?)
    return false
  return document.body.scrollHeight > innerHeight + 100

if isReady()
  console.log("Already ready")
  Frost?.isReady()
  return

console.log("Injected document watcher")

observer = new MutationObserver(() ->
  if isReady()
    observer.disconnect()
    Frost?.isReady()
    console.log("Documented surpassed height in #{performance.now()}")
)

observer.observe document,
  childList: true
  subtree: true