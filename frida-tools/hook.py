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
