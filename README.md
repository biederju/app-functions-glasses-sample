# Sample Android App for App Functions on Display Glasses

A minimal sample android project
demonstrating [app functions](https://developer.android.com/ai/appfunctions?hl=en)
for [display glasses](https://developer.android.com/develop/xr/jetpack-xr-sdk/glasses/build?hl=en)
with [compose glimmer](https://developer.android.com/develop/xr/jetpack-xr-sdk/jetpack-compose-glimmer?hl=en).

## Features

* **Contextual UI Display:** The display glasses render focused compose glimmer elements when an AI assistant triggers a matching app function via voice command.
* **Assistant Data Retrieval:** The AI assistant queries structured app data via an app function and relays the information back to the user hands-free.

## Requirements

* [Android Studio (Latest canary build)](https://developer.android.com/studio/preview?hl=en)
* [Configure Android Studio for XR development](https://developer.android.com/develop/xr/jetpack-xr-sdk/get-studio?hl=en)

## Quick Start

1. Clone the repository and open the project in Android Studio.
2. [Create a display glasses emulator and pair it with a compatible phone emulator.](https://developer.android.com/develop/xr/jetpack-xr-sdk/run/create-avds/glasses?hl=en)
3. Build and run the debug configuration of the app on both emulators.

For technical project specifications please refer to [AGENTS.md](./AGENTS.md).