# Nebula

Nebula is a showcase of one approach to Android application design using [Nasa's Astronomy Picture of the Day (Apod)](https://apod.nasa.gov/apod/astropix.html).

- Networking with [OkHttp](https://square.github.io/okhttp/) + [Retrofit](https://square.github.io/retrofit/)
- Image loading with [Glide](https://bumptech.github.io/glide/)
- Local database persistence with [Room](https://developer.android.com/topic/libraries/architecture/room)
- Dependency Injection with [Hilt](https://dagger.dev/hilt/)
- Styled with [Material Components for Android](https://github.com/material-components/material-components-android)
- Written in [Kotlin](https://kotlinlang.org/)

## Setup

- First, clone the project from `https://github.com/jlorett/nebula.git`
- Import the project into Android Studio
- Get a Nasa API Key from [Nasa's API](https://api.nasa.gov)
- Add the key as a string resource `<string name="key">YOUR_API_KEY</string>`. (Note: if you change
the key's resource name, make sure to update `app/src/main/java/com/joshualorett/nebula/di/ApodServiceModule.kt`)
- You should now be able to run the application

## Architecture

Nebula's architecture follows [Android Jetpack's Guide to App Architecture](https://developer.android.com/jetpack/guide).

```
+------+   LiveData    +-----------+   Flow/Coroutine    +------------+   Flow/Coroutine    +--------------+
| View | ------------> | ViewModel | ------------------> | Repository | ------------------> | Data Service |
+------+               +-----------+                     +------------+                     +--------------+
                                                               |
                                                               | Flow/Coroutine
                                                               |
                                                               V
                                                          +----------+
                                                          | Database |
                                                          +----------+
```

- **View**: The view consists of Activities, Fragments, and other user interface (UI) related code. It relies on a [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) to provide data for specific UI components via an observable data stream.
- **ViewModel**: The ViewModel provides UI data to the view via an observable data stream known as [LiveData](https://developer.android.com/topic/libraries/architecture/livedata). It communicates with a repository to perform business related tasks such as fetching an Apod. Since the ViewModel provides data through an observable stream, it knows nothing about the view layer itself and isn't affected by configuration changes.
- **Repository**: The Repository provides a clean API to post and retrieve data. It also acts as a mediator between the Data Service and the Database. It can perform one time tasks using [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) or provide observable data streams using [Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html).
- **Data Service**: The Data Service reaches out to Nasa's API for Apods.
- **Database**: The Database persists Apod data locally to prevent duplicate network requests.
