# Nebula

Nebula is a showcase of modern Android application design using [Nasa's Astronomy Picture of the Day](https://apod.nasa.gov/apod/astropix.html).

- Networking with [OkHttp](https://square.github.io/okhttp/) + [Retrofit](https://square.github.io/retrofit/)
- Image loading with [Glide](https://bumptech.github.io/glide/)
- Local database persistence with [Room](https://developer.android.com/topic/libraries/architecture/room)
- Dependency Injection with [Hilt](https://dagger.dev/hilt/)
- Styled with [Material Components for Android](https://github.com/material-components/material-components-android)
- Written in [Kotlin](https://kotlinlang.org/)

## Architecture

The architecture is built around [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/). Logic is kept away from Activities and Fragments and moved into [ViewModels](https://developer.android.com/topic/libraries/architecture/viewmodel). Data is then observed using [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) to update the UI. A repository layer is used to fetch from both a remote API and local database cache. It provides data using [Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html).
