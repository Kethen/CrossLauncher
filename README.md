Language : English | [Bahasa Indonesia](README_ID.md)
# Cross Launcher
Sony XMB like Android Launcher, Mainly inspired by Sony PlayStation 3(TM) XMB.

## ⚠ Hiatus Notice ⚠
I cannot work on this project for very often as the company I'm working at currently have a very thigh schedule for projects, be it app or games. 
And sometime I lost my mood to work in this project when I have free time. Please understand that this project is my own side-project. 
There is probably a time that I may forgot this project at all.

## Main Focus
This launcher is not really focused to Android touch-based devices, But for Android devices that 
naturally doesn't have a native touch interface like TV, PC, Laptop, and Emulators.

The launcher is still fairly usable on Phones. Just with a bit struggle if you have a
lot of apps to navigate.

**You must've known that there is non-touch screen Android devices. Do not go toxic with your stupidity!**

## Usage
| Function          | Keyboard | DualShock | Xbox     | Touch            |
|-------------------|----------|-----------|----------|------------------|
| Navigate Items    | Arrow Pad| D-Pad     | D-Pad    | Swipe            |
| Execute Item      | Enter    | X/O       | A/B      | Icon touch       |
| Hide/Show Options | Menu/Tab | Triangle  | Y        | Two-finger Touch |

## Memory Usage
On my test devices, Base memory usage (without items) is about 10MB.
With about 500Kb~1MB per item since icons will be cached in memory.

18/11/2020: Icon bitmap loading is more dynamic now, but possibly caused hiccups when
an icon is first drawn on screen after hidden before

## Screenshots
![Apps list screenshot](readme_asset/ss_apl.png)
App List

![Music list screenshot](readme_asset/ss_musiclist.png)
Music List

![Video player screenshot](readme_asset/ss_videoplayer.png)
Video Player

## Available Features
- [x] Switchable Confirm Button (Cross / Circle)
- [x] Android built-in and Dictionary-based Game Detection
- [x] Music and Video Gallery
- [ ] Notification section
- [x] Gamepad Support (partial)
- [x] Keystroke-to-Name Item finding
- [x] Launcher Startup & App Launch Animation (Switchable), see 
[Animation Modding](https://github.com/EmiyaSyahriel/CrossLauncher/blob/master/README.md#animation-modding) to modify it
- [ ] PS3-like Dialogs
  - [x] Preliminary state
- [ ] Item Options (Just like when you press Triangle on PS3)
  - [x] Preliminary state
- [ ] Item Hiding
- [ ] Open video file with default apps
- [ ] Using a custom icon and custom BGM
- [ ] Built-in media player
  - [ ] Music (Need some fix)
  - [ ] Video (Available, Barely function UI)
  
## TODOs:
- ~Render padding so it doesn't get rendered behind system bars~
- Soft-coding strings
- Some optimizations regarding item caching and loading
- Make separate media player activities and integrate it to main launcher activity
- Use binary-based configuration system instead of Android built-in shared preference
- Add different on-screen gamepad mapping instruction
- Create a ViewGroup based on XMB dialog to be used on xml layout file
- Implement an Icon, Background Image and Hover BGM Loading similar to PS3 Content Info File Structure (ICON0.png, SND0.aac and PIC0.png)
- Make a better vertical slide for touchscreen user.

## Animation Modding
You can modify the Launcher Startup / App Launch Animation and Sound by
adding/changing files in `/sdcard/Android/data/id.psw.vshlauncher/files/`.
| File name    | Correspond to   |
|--------------|-----------------|
| coldboot.png | Startup logo    |
| coldboot.mp3 | Startup Audio   |
| gameboot.png | App launch logo |
| gameboot.mp3 | App launch audio|

## Releases
You can build it yourself, Or go to [Release](https://github.com/EmiyaSyahriel/CrossLauncher/releases)
page for pre-built packages

## Contribution
Translations and fixes are welcome.

I am still a beginner on Android programming. one and a half year experience in non-professional capacity.

## License
MIT License.
