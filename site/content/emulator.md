+++
date = "2016-05-24T22:31:58+01:00"
draft = false
title = "The Emulator"
+++

The Plus-F Emulator can emulate either a 48k Spectrum or a Spectrum +2, and can load games from TAP, TZX or Z80 format files (TAP files have the best support). At present there is no sound support, and the graphics timing is not 100% accurate, so don't expect programs which generate "rainbow" effects to work properly.

## So where is the symbol shift key?

The CTRL key is used in place of the Spectrum's symbol shift key.  

## How to use it

The Emulator is controlled by the options in the menu bar at the top of the screen.

### File menu

* Open: Open a TAP, TZX or Z80 file.
    
    Z80 files will start automatically. To load from a TAP or TZX file, type LOAD "" (key J then symbol shift+P twice) in
    Spectrum BASIC, and then choose Play from the Tape menu. 
  
* Quit: Quit the Emulator.

### Computer menu

* Reset: Reset the Emulator (just like pulling the plug out and putting it back in again).
* Host Joystick: Select this option to emulate a Sinclair 1 joystick through the keyboard. Controls are not redefinable and are fixed to the conventional ones below:
    * Q: Up
    * A: Down
    * O: Left
    * P: Right
    * M: Fire
* Model: Choose the model of Spectrum to emulate, either 48k or +2. Changing the model will reset the Emulator.
* Speed: Choose the speed the Emulator runs at:
    * Normal: The same speed as a real Spectrum.
    * Fast: 1.5 times the speed of a real Spectrum.
    * Double: Double the speed of a real Spectrum.
    * Turbo: Much, much faster than a real Spectrum. Use this if you're bored waiting for TAP or TZX files to load.

### Tape menu

The controls on the tape menu are also displayed on buttons at the bottom of the Emulator window. The tape icon turns
green to indicate when the tape is playing.

* Play: Play a TAP or TZX file.
* Stop: Stop the tape.
* Rewind to Start: Rewind to the start of the current tape.
* Tape Information: Shows information embedded in the tape file, if any.
* Jump to Block: Jump to a particular block of the tape.

### Network menu

* Connect: Enter a connection code to pair up with a Guest for a two-player gaming session.
* Disconnect: Disconnect from a Guest to stop a two-player gaming session.
