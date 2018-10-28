[![license](https://img.shields.io/github/license/ohmae/custom-tabs-browser.svg)](./LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/ohmae/custom-tabs-browser.svg)](https://github.com/ohmae/custom-tabs-browser/issues)
[![GitHub closed issues](https://img.shields.io/github/issues-closed/ohmae/custom-tabs-browser.svg)](https://github.com/ohmae/custom-tabs-browser/issues?q=is%3Aissue+is%3Aclosed)
# Custom Tabs Browser Sample

A sample implementation for browser that support Chrome Custom Tabs.

|![](readme/screenshot1.gif)|![](readme/screenshot2.gif)|![](readme/screenshot3.gif)|
|-|-|-|

## Implementation status

- Intent
  - [x] launchUrl
  - [x] setShowTitle
  - [x] setToolbarColor
  - [x] enableUrlBarHiding
  - [x] addDefaultShareMenuItem
  - [x] addMenuItem
  - [x] setActuionButton
    - [x] shouldTint
  - [x] addToolbarItm
  - [x] setCloseButtonIcon
  - [x] setSecondaryToolbarColor
  - [x] setStartAnimations
  - [x] setExitAnimations
  - [x] setSecondaryToolbarViews
  - [ ] setInstantAppsEnabled
  - [x] setAlwaysUseBrowserUI
- Service
  - [x] bind/unbind
  - [ ] warmup
  - [ ] newSession
  - [ ] mayLaunchUrl
  - [x] onNavigationEvent
  - [x] extraCallback
    - [x] onBottomBarScrollStateChanged
    - [x] onOpenInBrowser
  - [ ] onRelationshipValidationResult
  - [ ] onMessageChannelReady
  - [ ] onPostMessage
- Callback
  - [x] onNavigationEvent
  - [x] extraCallback
    - [x] onBottomBarScrollStateChanged
    - [x] onOpenInBrowser
  - [ ] onMessageChannelReady
  - [ ] onPostMessage
  - [ ] onRelationshipValidationResult

## Author
大前 良介 (OHMAE Ryosuke)
http://www.mm2d.net/

## License
[MIT License](./LICENSE)
