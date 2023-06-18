declare namespace browser.runtime {
    interface Port {
        postMessage: (message: string) => void;
        postMessage: (message: ExtensionModel) => void;
    }
    function sendNativeMessage(application: string, message: ExtensionModel): Promise<any>;
}