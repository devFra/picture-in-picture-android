# **Picture In Picture - Android Jetpack Compose**

La funzionalità Picture in picture (PIP) racchiude un video in un player di piccole dimensioni per consentirti di continuare a guardarlo mentre utilizzi altre app sul tuo dispositivo mobile. Puoi spostare il piccolo player all'interno della schermata Home del tuo dispositivo e posizionarlo su altre app.

## Supporto API 
Questa modalità è stata introdotta a partire da Android 8.0 (API level 26).

## Implementazione

Iniziamo l'implementazione di questa modalità di visualizzazione, in un progetto facente uso del framework Jetpack Compose.

### **Step 1 - Registrazione Activity**: 
Di default le activity non supportano questa modalità, per renderle compatibili bisogna dichiarare nel manifest
che l'activity deve supportare questa modalità:
```xml
 <activity android:name="VideoActivity"
    android:supportsPictureInPicture="true"
    android:configChanges=
        "screenSize|smallestScreenSize|screenLayout|orientation"
    ...
```

## **Step 2 - Implentazione del player video**:
In questo step andremo a definire il player che riprodurra il video, in questo caso andremo ad utilizzare il player standard di Android. Al momento ancora non supporta Jetpack Compose, quindi lo andremo ad utilizzare all' interno di [AndroidView](doc:https://foso.github.io/Jetpack-Compose-Playground/viewinterop/androidview/), come segue:

```kotlin
AndroidView(
    factory =  {
            VideoView(it, null).apply {
                setMediaController(MediaController(this.context))
                setVideoPath("http://commandatastorage.googleapis.com/gtv-videos-bucket/sample/BigBunny.mp4")
                start()
            }
    },
    modifier = Modifier.fillMaxSize()
        .onGloballyPositioned {  }
)
```