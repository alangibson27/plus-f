+++
date = "2016-05-24T22:31:58+01:00"
draft = false
title = "Play"
+++

+F can emulate a number of different models of Spectrum, and can load games from TAP, TZX or Z80 files. TZX files are
the most authentic, TAP files are the most reliable. Only 48k Z80 snapshots are supported.

## So where is the symbol shift key?

The CTRL key is used in place of the Spectrum's symbol shift key.  

## How to use it

+F is controlled by the options in the menu bar at the top of the screen.

### File menu

* Load from File: Open a TAP, TZX or Z80 file.

    Z80 files will start automatically. To load from a TAP or TZX file, type LOAD "" (key J then symbol shift+P twice) in
    Spectrum BASIC, and then choose Play from the Tape menu.

* Load from WOS: Download and run a program from the World of Spectrum archive.

    Use the box at the top of the pop-up window to search for a program by name, then choose the program to load from
    the picker tree.

* Quit: Quit the Emulator.

### Computer menu

* Reset: Reset the Spectrum (just like pulling the plug out and putting it back in again).
* Sound: Activate or deactivate sound output.
* Joystick: Select one of the sub-options to activate a Kempston or Sinclair joystick through the keyboard. Use the
  keys below to control the joystick when activated:
    * Q: Up
    * A: Down
    * O: Left
    * P: Right
    * M: Fire
  If you're playing a network game with someone else, you won't be allowed to use the same joystick.
* Model: Select the model of Spectrum to emulate. Changing the model will reset the Spectrum.
* Speed: Choose the speed the Spectrum runs at:
    * Normal: The same speed as a real Spectrum.
    * Fast: 1.5 times the speed of a real Spectrum.
    * Double: Double the speed of a real Spectrum.
    * Turbo: Much, much faster than a real Spectrum. Use this if you're bored waiting for TAP or TZX files to load.

### Display menu

* Smooth Display Rendering: Choose this to give slightly smoother pixels when you enlarge the window.
* Extend Border: Choose this to fill any blank space around the display with the border pattern.

### Tape menu

The controls on the tape menu are also displayed on buttons at the bottom of the Emulator window. The tape icon turns
green to indicate when the tape is playing.

* Play: Play a TAP or TZX file.
* Stop: Stop the tape.
* Rewind to Start: Rewind to the start of the current tape.
* Tape Information: Shows information embedded in the tape file, if any.
* Jump to Block: Jump to a particular block of the tape.
* Turbo Load: Select this to automatically put the Spectrum into turbo mode while a tape is loading.
  When you stop the tape, the Spectrum will leave turbo mode again.

### Network menu

* Start Session: Start a multiplayer network session for someone else to join.
* Join Session: Join a multiplayer network session that someone else has already started.
* End Session: End the current multiplayer network session.

See [/network] for more details on how multiplayer network sessions work.
