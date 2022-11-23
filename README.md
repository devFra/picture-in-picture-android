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
)
```

## **Step 3 - Implementazione PIP**
Ora che abbiamo implementato il player, possiamo procedere con l'implementazione della funzionalità picture-in-picture. 

Prima di tutto andiamo a definire un' attributo per sapere se la feature PIP è supportata, questo andrà valorizzato con un valore booleano.

```kotlin
private val isPipSupported by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    } else false
}
```

Poi andiamo a definire un' altro attributo che conterrà un'istanza dell' oggetto *Rect*. Questa istanza ci servirà in seguito per contere il player. 

```kotlin
private var videoViewBounds = Rect()
```

Ora tramite modificatore della funzione AndroidView andiamo a definire cosa avviene quando le coordinate cambiano, in questo caso assegnamo le nuove coordinate all' istanza "videoViewBounds".

```kotlin
modifier = Modifier.fillMaxSize()
    .onGloballyPositioned { 
        videoViewBounds = it.boundsInWindow().toAndroidRect()
    }
```

Definiamo un metodo che ritorna i parametri per il picture-in-picture tramite builder, come segue:
```kotlin
private fun updatePipParams(): PictureInPictureParams? {
    return if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
        PictureInPictureParams.Builder()
            .setSourceRectHint(videoViewBounds)
            .setAspectRatio(Rational(16,9))
            .build()
    } else {
        return null
    }
}
```

In fine definiamo quando entrare nella modalità PIP, ovvero quando l' utente mette l'app in background:
```kotlin
override fun onUserLeaveHint() {
    super.onUserLeaveHint()
    if (!isPipSupported) return

    updatePipParams().let { params ->
        if ( params != null )
            enterPictureInPictureMode(params)
    }
}
```


## **Step 4 - Definizione actions in PIP (facoltativo)**
In caso si voglia definire dei pulsanti a cui assegnare delle funzionalità, come ad esempio il play/pause in caso di un player video, è possibile farlo tramite il metodo *"setActions()"* del PictureInPicture builder. 

### *Definizione Enum di controllo*
Come primo step procediamo con la definizione di un enum che usaremo per controllare gli eventi emessi dalle actions definite nel builder del PIP. 

```kotlin
enum class PlayerEvent(val value: Int) {
    PLAYER_CONTROL(1), // Define intent
    PLAYBACK_CONTROL(2), // Define key intent
    PLAY_PAUSE(3) // Define value intent
}
```


### *Definizione del broadcast reciever*

```kotlin
/**
* Broadcast receiver for handling action items on the pip mode
*/
private val broadcastReceiver = object : BroadcastReceiver(){
    override fun onReceive(p0: Context?, intent: Intent?) {
        if (intent == null || intent.action != PlayerEvent.PLAYER_CONTROL.value.toString()) {
            return
        }
        when(intent.getIntExtra(PlayerEvent.PLAYBACK_CONTROL.value.toString(), 0)){
                PlayerEvent.PLAY_PAUSE.value -> {
                    if ( playerController?.isPlaying!! )
                        layerController?.pause()
                    else 
                        playerController?.start()
            }

        }
    }
}
```

Estendiamo l'ogetto BroadcastReciever e facendo l'override del metodo *onReceiver* controlliamo la ricezione del nostro intent, in caso positivo definiamo un controllo when per ogni tipo di azione definita. 

In fine andiamo a registrare il BroadcastReciever appena definito nell' onCreate dell' activity:

```kotlin
registerReceiver(broadcastReceiver, IntentFilter(PlayerEvent.PLAYER_CONTROL.value.toString()))
```

### *Definizione delle actions*
Come ultimo step non resta che definire le azioni che vogliamo nella modalità PIP, nel nostro caso definiamo l'azione per metter in pausa o riassumere la riproduzione del video. Tramite la pipe del builder aggiungiamo il metodo *"setActions"* che accetta come parametro una lista di RemoteAction:

```kotlin
PictureInPictureParams.Builder()
    .setSourceRectHint(videoViewBounds)
    .setAspectRatio(Rational(16,9))
    .setActions(listOf( // <---
        RemoteAction(
            Icon.createWithResource(applicationContext, R.drawable.ic_pause),
            "pause", // title 
            "pause playback", // contentDescription
            PendingIntent.getBroadcast(
                applicationContext,
                0,
                Intent(PlayerEvent.PLAYER_CONTROL.value.toString()).putExtra(PlayerEvent.PLAYBACK_CONTROL.value.toString(), PlayerEvent.PLAY_PAUSE.value),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    ))
    .build()
```
con quest'ultimo step abbiamo terminito l'implemntazione della modalità PIP con dei controlli custom. 


Grazie  😉 

[Francesco Mariani](francesco.mariani@it.clara.net)