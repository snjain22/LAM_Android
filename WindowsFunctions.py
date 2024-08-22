import os
import pyautogui
from ctypes import cast, POINTER
from comtypes import CLSCTX_ALL
from pycaw.pycaw import AudioUtilities, IAudioEndpointVolume
import screen_brightness_control as sbc

def set_brightness(value):
    try:
        sbc.set_brightness(value)
        return f"Brightness set to {value}%"
    except Exception as e:
        return f"Failed to set brightness: {e}"

def set_volume(value):
    try:
        devices = AudioUtilities.GetSpeakers()
        interface = devices.Activate(
            IAudioEndpointVolume._iid_, CLSCTX_ALL, None)
        volume = cast(interface, POINTER(IAudioEndpointVolume))
        volume.SetMasterVolumeLevelScalar(value / 100, None)
        return f"Volume set to {value}%"
    except Exception as e:
        return f"Failed to set volume: {e}"

def open_application(app_path):
    try:
        os.startfile(app_path)
        return f"Opened {app_path}"
    except Exception as e:
        return f"Failed to open {app_path}: {e}"

def press_key(key):
    try:
        pyautogui.press(key)
        return f"Pressed {key}"
    except Exception as e:
        return f"Failed to press {key}: {e}"

set_brightness(30)
