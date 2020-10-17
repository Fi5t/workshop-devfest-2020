console.log("[*] Hook is loaded");

Java.perform(function () {
    console.log("[*] Run instrumentation");

    var pinkman = Java.use("com.redmadrobot.pinkman.Pinkman");

    pinkman.isValidPin.implementation = function (arg) {
        console.log("[*] Call isValidPin()");

        return true;
    }
});