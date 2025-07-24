# InJoy - Feel movies. Offline or Online.

## Overview
InJoy is a modern movie discovery app built with Jetpack Compose, offering seamless offline and online experiences. Enjoy fast search, beautiful UI, and smooth navigation.

## Features
- Offline-first movie browsing & search
- Trending & Now Playing with pagination
- Movie bookmarking
- Detail screens with rich info
- Clean, minimal UI (Material3 + Compose)
- Fast search, instant results
- Modern MVVM + Clean Architecture

## Architecture
- MVVM + Clean Architecture
- Dependency Injection with Koin
- Paging3 for infinite scroll
- Room for local storage & offline support

## Tech Stack
- Kotlin, Jetpack Compose, Material3
- Room, Paging3, Retrofit
- Koin (DI), Coroutines, Flow
- TMDB API

## Setup Instructions
1. Clone this repo
2. Open in Android Studio (Giraffe+ recommended)
3. Add your TMDB API key to `local.properties`:
   ```
   TMBD_BEARER_TOKEN=<your_key>
   ```
4. Build & run on device/emulator