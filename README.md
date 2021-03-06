# Nebula

Nebula is a showcase of one approach to Android application design using [Nasa's Astronomy Picture of the Day (Apod)](https://apod.nasa.gov/apod/astropix.html).

- Networking with [OkHttp](https://square.github.io/okhttp/) + [Retrofit](https://square.github.io/retrofit/)
- Image loading with [Glide](https://bumptech.github.io/glide/)
- Local database persistence with [Room](https://developer.android.com/topic/libraries/architecture/room)
- Dependency Injection with [Hilt](https://dagger.dev/hilt/)
- Styled with [Material Components for Android](https://github.com/material-components/material-components-android)
- Written in [Kotlin](https://kotlinlang.org/)

## Architecture

Nebula's architecture follows the [Android Guide to App Architecture](https://developer.android.com/jetpack/guide). [ViewModels](https://developer.android.com/topic/libraries/architecture/viewmodel) manage UI related data in a lifecycle conscious way. Activities and Fragments can observe the ViewModel using [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) and change according to the UI model the ViewModel provides. The ViewModel also can communicate with the repository to perform business related tasks such as fetching the Apod. The repository can fetch data from both a remote API and local database using [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html). When a request is made, the repository will first search the Room database to see if an Apod for the given date exists, if it is found, the cached Apod is returned, otherwise the repository calls out to the Remote Data Source to get the Apod from Nasa's Api and finally caches the result for later use.

```
+------+   LiveData    +-----------+   Flow    +------------+   Coroutine    +--------------------+
| View | ------------> | ViewModel | --------> | Repository | -------------> | Remote Data Source |
+------+               +-----------+           +------------+                +--------------------+
                                                     |
                                                     | Flow/Coroutine
                                                     |
                                                     V
                                                +----------+
                                                | Database |
                                                +----------+
```
