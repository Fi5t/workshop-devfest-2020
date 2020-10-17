console.log("[*] Hook is loaded");

Java.perform(function () {
    console.log("[*] Run instrumentation");

    var aes = Java.use("com.redmadrobot.vulnerableapp.internal.AesEncryption");

    var aesInstance = aes.$new();
    var plainText = aesInstance.decrypt("C5gQUGTWhlhetkx2G+1cdg==");

    console.log("[*] Password: " + plainText);
});