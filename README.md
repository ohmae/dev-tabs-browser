[![license](https://img.shields.io/github/license/ohmae/custom-tabs-browser.svg)](./LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/ohmae/custom-tabs-browser.svg)](https://github.com/ohmae/custom-tabs-browser/issues)
[![GitHub closed issues](https://img.shields.io/github/issues-closed/ohmae/custom-tabs-browser.svg)](https://github.com/ohmae/custom-tabs-browser/issues?q=is%3Aissue+is%3Aclosed)
# Custom Tabs Browser Sample

A sample implementation for browser that support Chrome Custom Tabs.

|![](readme/screenshot1.gif)|![](readme/screenshot2.gif)|
|-|-|

## Implementation status

- Intent
  - :heavy_check_mark: launchUrl
  - :heavy_check_mark: setShowTitle
  - :heavy_check_mark: setToolbarColor
  - enableUrlBarHiding
  - :heavy_check_mark: addDefaultShareMenuItem
  - :heavy_check_mark: addMenuItem
  - :heavy_check_mark: setActuionButton
    - :heavy_check_mark: shouldTint
  - :heavy_check_mark: addToolbarItm
  - :heavy_check_mark: setCloseButtonIcon
  - :heavy_check_mark: setSecondaryToolbarColor
  - :heavy_check_mark: setStartAnimations
  - :heavy_check_mark: setExitAnimations
  - setSecondaryToolbarViews
  - setInstantAppsEnabled
  - :heavy_check_mark: setAlwaysUseBrowserUI
- Service
  - :heavy_check_mark: bind/unbind
  - warmup
  - newSession
  - mayLaunchUrl
  - :heavy_check_mark: onNavigationEvent
  - extraCallback
  - onRelationshipValidationResult
  - onMessageChannelReady
  - onPostMessage

## Author
大前 良介 (OHMAE Ryosuke)
http://www.mm2d.net/

## License
[MIT License](./LICENSE)
