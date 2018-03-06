# Circular Progress Bar
[ ![Download](https://api.bintray.com/packages/yuriy-budiyev/maven/circular-progress-bar/images/download.svg) ](https://bintray.com/yuriy-budiyev/maven/circular-progress-bar/_latestVersion)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Circular%20Progress%20Bar-blue.svg?style=flat)](https://android-arsenal.com/details/1/6515)
[![API](https://img.shields.io/badge/API-14%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=14)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/04aff697e57642bb96579fbaa6cc3dad)](https://www.codacy.com/app/yuriy-budiyev/circular-progress-bar?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yuriy-budiyev/circular-progress-bar&amp;utm_campaign=Badge_Grade)

Circular progress bar, supports animations and indeterminate mode, highly customizable

### Usage ([sample](https://github.com/yuriy-budiyev/lib-demo-app))
Add dependency:
```gradle
dependencies {
    implementation 'com.budiyev.android:circular-progress-bar:1.1.8'
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
```java
public class MainActivity extends AppCompatActivity {
   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       CircularProgressBar progressBar = findViewById(R.id.progress_bar);
       progressBar.setProgress(30f);
       //Progress bar can be fully configured from code
   }
}
```
### Preview
![Preview screenshot](https://raw.githubusercontent.com/yuriy-budiyev/circular-progress-bar/master/images/circular_progress_bar_preview.png)
