+++
date = "2016-05-24T22:31:58+01:00"
draft = false
title = "The Emulator"
+++

The Plus-F Emulator is a program that emulates a ZX Spectrum, with the following features:

* Emulates a 48k Spectrum
* Loads from TAP, Z80 and TZX files (TAP is the most reliable)
* No sound support
* Emulation timing is not 100% accurate with respect to a "real" Spectrum 

## So where is the symbol shift key?

The CTRL key is used in place of the Spectrum's symbol shift key.  

## How to use it

The Emulator is controlled by the options in the menu bar at the top of the screen.

### File menu

* Open: Open a TAP, Z80 or TZX file.
    
    Z80 files will start automatically. To load from a TAP or TZX file, type LOAD "" (key J then symbol shift+P twice) in
    Spectrum BASIC, and then choose Play from the Tape menu. 
  
* Quit: Quit the Emulator.

### Computer menu

* Reset: Reset the Emulator (just like pulling the plug out and putting it back in again).
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

### Network menu

* Connect to guest: Enter a connection code to pair up with a Guest for a two-player gaming session.
* Disconnect from guest: Disconnect from a Guest to stop a two-player gaming session.