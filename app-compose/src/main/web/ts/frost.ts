/**
 * Mobile browsers don't support modules, so I'm creating a shared variable.
 * 
 * No idea if this is good practice.
 */
const frost = (function () {
    const application = "frostContentChannel"

    async function sendMessage<T>(message: ExtensionModel): Promise<T> {
        return browser.runtime.sendNativeMessage(application, message)
    }

    async function loadUrl(url: string | null): Promise<boolean> {
        if (url == null) return false
        return sendMessage({ type: "url-click", url: url })
    }

    return {
        sendMessage, loadUrl
    }
}).call(undefined);