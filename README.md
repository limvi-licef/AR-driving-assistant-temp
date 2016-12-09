# AR-driving-assistant

TODO: Project description

## Getting started

TODO: Getting Started

### Prerequisites

* AndroidApp
  * Android Studio 2.2 or later

* UnityApp
 * For HoloLens
   * Windows 10 (required for deployment)
    * Microsoft Visual Studio 2015 Update 3 (required for deployment)
     * Unity 5.5.0f3 or later (earlier versions do not include support for HoloLens)
    
     See https://developer.microsoft.com/en-us/windows/holographic/install_the_tools for more info
 * For Android
   * Unity 5.5.0f3 or later
 
### Installation

* Check the following options during Unity's installation :
  * Microsoft Visual Studio Tools for Unity
   * Windows Store .NET Scripting Backend
    * Android Build Support
    
* Check the following options during Visual Studio's installation :
  * Tools (1.4) under Universal Windows App Development Tools
   * Windows 10 SDK (10.0.10586) under Universal Windows App Development Tools

## Deployment

###### AndroidApp
   * Nothing specific, simply open Android Studio and run the app on your phone
  
###### UnityApp
  * HoloLens (Windows 10 required)
   * Build the app in Unity
     * Select the 'AppUI-Hololens' scene 
     * Switch to the 'Windows Store' platform
     * Set Target device to 'HoloLens'
    * Open 'AR-driving-assistant.sln' **located in the UnityApp/Build subfolder** with Visual Studio
    * In Visual Studio, set configuration type from Debug to Release or Master, set the architecture to x86 and set the deployment target to 'Device' 
    * Connect the HoloLens via USB and then select Debug > Start Without Debugging from the menu
    
    See https://developer.microsoft.com/en-us/windows/holographic/using_visual_studio for more info
     
  * Android
   * Move the HoloToolkit folder out of the Assets folder as it will prevent building for Android
   * Build the app in Unity
     * Select the 'AppUI-Android' scene 
     * Switch to the 'Android' platform
   * Choose Build & Run to deploy the apk file directly to the phone via USB

## Usage

TODO: Write usage instructions

## License

TODO: License
