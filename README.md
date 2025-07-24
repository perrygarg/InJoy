# InJoy - Feel movies. Offline or Online.

## Overview
InJoy is a modern movie discovery app built with Jetpack Compose, offering seamless offline and online experiences.  
It fetches from TMDB, caches locally with Room, and supports offline search and bookmarking.

## Features
- Browse Trending & Now Playing movies (with offline support)
- Search even when offline (with stale data warning)
- Bookmark movies to saved list
- Rich Movie Detail screen
- Clean Material3 UI with smooth transitions

## Dev Notes
- Architecture: Clean, modular MVVM structure
- Local DB: Movies are persisted using Room ORM
- Offline-first: UI loads from DB, not from API directly
- Debounced Search (Bonus): Search begins after user stops typing
- Dummy Deeplink (Bonus): NavHost-based navigation with deep link support
- ViewModels & Flows (Bonus): Purely ViewModels and Flow based approach
- Coroutines (Bonus): Entire data flow is async & structured
- Repository Pattern: Clean separation of concerns
- Modular Design: Layers for data, domain, UI
- Pagination: Implemented using Paging3 for infinite scrolling
- App theming: Gradient splash, Material 3, smooth transitions

## Tech Stack
- Kotlin, Jetpack Compose, Material3
- Room, Paging3, Retrofit
- Koin (DI), Coroutines, Flow
- TMDB API

## Setup Instructions
1. Clone this repo
2. Open in Android Studio
3. Add your TMDB API key to `local.properties`:
   ```
   TMBD_BEARER_TOKEN=<your_key>
   ```
4. Build & run on device/emulator

**Made with ❤️ by Perry Garg as part of his one of assignments**