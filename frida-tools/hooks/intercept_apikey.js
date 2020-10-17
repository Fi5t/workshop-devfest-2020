console.log("[*] Hook is loaded");

Java.perform(function () {
    console.log("[*] Run instrumentation");

    var nativeInspection = Java.use("com.redmadrobot.vulnerableapp.ui.login.LoginViewModel")

    nativeInspection.getApiKey.overload().implementation = function () {
        console.log("[*] Calling getApiKey()")

        var key = this.getApiKey()
        console.log(">>> API key: " + key)

        return key
    }
});