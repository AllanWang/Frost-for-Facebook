async function readCookies() {

    const application = "frostBackgroundChannel"

    browser.runtime.sendNativeMessage(application, "start cookie fetch")

    // Testing with domains or urls didn't work
    const cookies = await browser.cookies.getAll({});

    browser.runtime.sendNativeMessage(application, cookies)
}

// todo change to better listener
browser.tabs.onActivated.addListener(readCookies);