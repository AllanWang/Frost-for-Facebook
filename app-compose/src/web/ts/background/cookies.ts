async function updateCookies(changeInfo: browser.cookies._OnChangedChangeInfo) {

    const application = "frostBackgroundChannel"

    browser.runtime.sendNativeMessage(application, changeInfo)

    return

    browser.runtime.sendNativeMessage(application, 'start cookie fetch for')

    // Testing with domains or urls didn't work
    const cookies = await browser.cookies.getAll({});

    browser.runtime.sendNativeMessage(application, cookies)
}

browser.cookies.onChanged.addListener(updateCookies);