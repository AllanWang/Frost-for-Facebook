async function updateCookies(changeInfo: browser.cookies._OnChangedChangeInfo) {

    const application = "frostBackgroundChannel"

    browser.runtime.sendNativeMessage(application, changeInfo)
}

async function readCookies() {
    const application = "frostBackgroundChannel"

    browser.runtime.sendNativeMessage(application, 'start cookie fetch')

    // Testing with domains or urls didn't work
    const cookies = await browser.cookies.getAll({});

    const cookies2 = await browser.cookies.getAll({ storeId: "firefox-container-frost-context-1" })

    const cookieStores = await browser.cookies.getAllCookieStores();

    browser.runtime.sendNativeMessage(application, { name: "cookies", data: cookies.length, stores: cookieStores.map((s) => s.id), data2: cookies2.length, data3: cookies.filter(s => s.storeId != 'firefox-default').length })
}

async function handleMessage(request: any, sender: browser.runtime.MessageSender, sendResponse: (response?: any) => void) {
    browser.runtime.sendNativeMessage("frostBackgroundChannel", 'pre send')

    await new Promise(resolve => setTimeout(resolve, 1000));

    browser.runtime.sendNativeMessage("frostBackgroundChannel", 'post send')

    sendResponse({ received: request, asdf: "asdf" })
}

// Reading cookies with storeId might not be fully supported on Android
// https://stackoverflow.com/q/76505000/4407321
// Using manifest 3 stopped getAll from working
// Reading now always shows storeId as firefox-default
// Setting a cookie with a custom container does not seem to work

// browser.cookies.onChanged.addListener(updateCookies);
// browser.tabs.onActivated.addListener(readCookies);
// browser.runtime.onStartup.addListener(readCookies);

// browser.runtime.onMessage.addListener(handleMessage);

