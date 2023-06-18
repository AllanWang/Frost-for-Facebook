type TestModel = {
    type: 'test-model'
    message: string
}

type UrlClickModel = {
    type: 'url-click'
    url: string
}

type ExtensionModel = TestModel | UrlClickModel
