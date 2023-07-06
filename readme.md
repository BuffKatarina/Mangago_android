# Mangago for Android

## Overview
Android app for the site https://mangago.me/ which contains comics from various sources. \
⚠ Site contains NSFW and other sensitive content — proceed at your own discretion.

## Problems with the site 
  - Site has no API
  - Image sources are generated randomly 
  
## Workings
 - Uses android WebView 
 - Notifications when a new chapter is released
     - Comics selected to be notified for updates are stored in a SQL database
     - Achieved by checking the RSS periodically for updates

## Known Bugs
  - Immersive view not working for android API < 30

## Possible Improvements
  - Reduce overhead
  - Better management of android activity lifecycle 
    




