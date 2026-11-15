# Sample Android App for App Functions on Display Glasses

A minimal sample android project
demonstrating [app functions](https://developer.android.com/ai/appfunctions)
for [display glasses](https://developer.android.com/develop/xr/jetpack-xr-sdk/glasses/build)
with [compose glimmer](https://developer.android.com/develop/xr/jetpack-xr-sdk/jetpack-compose-glimmer).
All showcase features are built around a hands-free note-taking workflow.

## Features

* **Phone Note Management:** A simple local list showing notes with a topic, text, and timestamp,
  featuring a Floating Action Button (FAB) to add notes and options to delete them.
* **Hands-Free Note Creation:** Users can speak to the AI assistant on the glasses to create a new
  note via an app function.
* **Contextual Glasses Display:** Users can ask the assistant to show the last n notes or all notes
  from a specific day, which are then rendered on the glasses using Compose Glimmer.

## Requirements

* [Android Studio (Latest canary build)](https://developer.android.com/studio/preview?hl=en)
* [Configure Android Studio for XR development](https://developer.android.com/develop/xr/jetpack-xr-sdk/get-studio)

## Quick Start

1. Clone the repository and open the project in Android Studio.
2. [Create a display glasses emulator and pair it with a compatible phone emulator.](https://developer.android.com/develop/xr/jetpack-xr-sdk/run/create-avds/glasses)
3. Build and run the debug configuration of the app on both emulators.
4. **Test App Functions** — verify the functions are registered and working:

```bash
# List all registered functions (should return both functions)
adb -s <device> shell cmd app_function list-app-functions --package de.julianbiedermann.app_functions_glasses_sample

# Show the 3 latest notes (omit count for default)
adb -s <device> shell cmd app_function execute-app-function \
  --package de.julianbiedermann.app_functions_glasses_sample \
  --function 'de.julianbiedermann.app_functions_glasses_sample.ai.NotesGlassesAppFunctionService#showLatestNotes' \
  --parameters '{}'

# Search notes containing "groceries"
adb -s <device> shell cmd app_function execute-app-function \
  --package de.julianbiedermann.app_functions_glasses_sample \
  --function 'de.julianbiedermann.app_functions_glasses_sample.ai.NotesGlassesAppFunctionService#searchNotesOnGlasses' \
  --parameters '{"query":"groceries"}'
```

For technical project specifications please refer to [AGENTS.md](./AGENTS.md).