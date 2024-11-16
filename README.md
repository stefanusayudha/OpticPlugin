Powered by: Singularity Indonesia

# Prolog
Optics are awesome, right? Yeah, I love optics, and I love Kotlin Arrow as well.
But Arrow Optics has some temporary issues:

1. Arrow generates lenses for all properties in the data class, which makes the app bloated.
2. The last time I checked, it didn’t work with Compose for Web.

That’s okay, but I wish to ask Santa if I could exclude the lenses I don’t need to save space—especially since I’m working with Web Compose right now.

As a backup, I decided to create it myself. And hey, I finished it before Christmas! I’ve changed my mind, Santa—I want a unicorn now.

# Usage
To use this feature, all you need to do is put the `@GenerateLens` annotation **on top of a property**.
Then you click build, and the Lenses will be generated for you.

Example:
```kotlin
data class HomePaneState(
    @GenerateLens
    val newsSection: NewsSection = NewsSection(),
) {

    data class NewsSection(
        val news: List<News> = emptyList(),
        @GenerateLens
        val showLoading: Boolean = false,
        val errorMessage: String? = null,
    )
}

// #BEGIN Auto Generated Lenses, Do not Modify!
// Powered by: Singularity Indonesia

object  HomePaneStateLens {
    val NewsSectionLens = object : Lens< HomePaneState, NewsSection> {
        override fun modify(
            parent:  HomePaneState,
            children: (NewsSection) -> NewsSection
        ):  HomePaneState = parent.copy(newsSection = children.invoke(parent.newsSection))
    
        override fun get(parent:  HomePaneState): NewsSection = parent.newsSection
    }
}

object  NewsSectionLens {
    val ShowLoadingLens = object : Lens< NewsSection, Boolean> {
        override fun modify(
            parent:  NewsSection,
            children: (Boolean) -> Boolean
        ):  NewsSection = parent.copy(showLoading = children.invoke(parent.showLoading))
    
        override fun get(parent:  NewsSection): Boolean = parent.showLoading
    }
}
// #END Auto Generated Lenses, Do not Modify!
```

# Features
For now, it can only generate simple Lenses.
The Lens provides 3 major usecase:
1. `Lens.get()` to get a value of a prop
2. `Lens.modify()` to modify a prop
3. `Lens + Lens` to concat

Example:
```kotlin
// Get
val newsSection = HomePaneStateLens.NewsSectionLens.get(state)

// Modification
_uiState.safeUpdate { state ->
    HomePaneStateLens.NewsSectionLens.modify(state) { newsSection ->
        newsSection.copy(
            news = news,
            errorMessage = errorMessage,
            showLoading = false
        )
    }
}

// Lens Concatnation
val showLoadingLens = HomePaneStateLens.NewsSectionLens + NewsSectionLens.ShowLoadingLens
_uiState.safeUpdate { state ->
    showLoadingLens.modify(state) { true }
}
```

# Implementation
In build gradle
```kotlin
plugins {
  id("io.github.stefanusayudha.OpticGeneratorPlugin") version "1.0.0"
}
```

Or for legacy app:
```kotlin
buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("io.github.stefanusayudha:optic:1.0.0")
  }
}

apply(plugin = "io.github.stefanusayudha.OpticGeneratorPlugin")
```

# Catch
This plugin may impact your build performance. I'm still working on optimizing it. Use it at your own risk.

# Meet the Author
[Stefanus Ayudha](https://www.linkedin.com/in/stefanus-ayudha-447a98b5/)
