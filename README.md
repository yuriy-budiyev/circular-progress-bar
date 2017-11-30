# CircularProgressBar
[ ![Download](https://api.bintray.com/packages/yuriy-budiyev/maven/circular-progress-bar/images/download.svg) ](https://bintray.com/yuriy-budiyev/maven/circular-progress-bar/_latestVersion)

Circular progress bar

### Usage
Add dependency:
```gradle
dependencies {
    implementation 'com.budiyev.android:circular-progress-bar:1.0.7'
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
        app:foregroundStrokeColor="#ffff4081"
        app:foregroundStrokeWidth="3dp"
        app:indeterminate="false"
        app:indeterminateRotationAnimationDuration="2000"
        app:indeterminateMinimumAngle="45"
        app:indeterminateArcAnimationDuration="1000"
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
       progressBar.configure().animateProgress(true).maximum(40).progress(30).apply();
   }
}
```
