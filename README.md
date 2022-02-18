# Circular Progress Bar
[![Release](https://jitpack.io/v/yuriy-budiyev/circular-progress-bar.svg)](https://jitpack.io/#yuriy-budiyev/circular-progress-bar)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Circular%20Progress%20Bar-blue.svg?style=flat)](https://android-arsenal.com/details/1/6515)
[![API](https://img.shields.io/badge/API-14%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=14)

Circular progress bar, supports animations and indeterminate mode, highly customizable, Kotlin-friendly

### Usage ([sample](https://github.com/yuriy-budiyev/lib-demo-app))

Step 1. Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
    ...
    repositories {
        ...
        maven { url 'https://jitpack.io' }
   }
}
```

or in settings.gradle file:
```gradle
dependencyResolutionManagement {
    ...
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add dependency:
```gradle
dependencies {
    implementation 'com.github.yuriy-budiyev:circular-progress-bar:1.2.3'
}
```

Define a view in your layout file:
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.budiyev.android.circularprogressbar.CircularProgressBar
        android:id="@+id/progress_bar"
        android:layout_width="64dp"
        android:layout_height="64dp"
        app:animateProgress="true"
        app:backgroundStrokeColor="#ff3f51b5"
        app:backgroundStrokeWidth="2dp"
        app:drawBackgroundStroke="false"
        app:foregroundStrokeCap="butt"
        app:foregroundStrokeColor="#ffff4081"
        app:foregroundStrokeWidth="3dp"
        app:indeterminate="false"
        app:indeterminateRotationAnimationDuration="1200"
        app:indeterminateSweepAnimationDuration="600"
        app:indeterminateMinimumAngle="45"
        app:maximum="100"
        app:progress="50"
        app:progressAnimationDuration="100"
        app:startAngle="270"/>
</FrameLayout>
```

And (or) add following code to your activity:

Kotlin
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val progressBar = findViewById<CircularProgressBar>(R.id.progress_bar)
        progressBar.progress = 30f
    }
}
```

Java
```java
public class MainActivity extends AppCompatActivity {
   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       CircularProgressBar progressBar = findViewById(R.id.progress_bar);
       progressBar.setProgress(30f);
   }
}
```

Progress bar can be fully configured from code

### Preview
![Preview screenshot](https://raw.githubusercontent.com/yuriy-budiyev/circular-progress-bar/master/images/circular_progress_bar_preview.png)
