# Setup

Since the Firebase services are stored in the cloud, we need to setup the wifi on our Android Things so it can communicate to the internet.

To setup the wifi on your Android Things you can use the setup utility tool or use the command below.
Setup wifi on your Android Things, replace the `$HERE` values
```
adb shell am startservice -n com.google.wifisetup/.WifiSetupService -a WifiSetupService.Connect -e ssid $YOUR_SSID_HERE -e passphrase $YOUR_WIFI_PASSWORD_HERE
```

To make the lesson easier, the base code for it is already done (based on the previous tasks)
So clone [this repository](https://github.com/jamescoggan/AndroidThingsWorkshop) and open the `worshopapps` folder.

If you have any issues, you can checkout the `full-project` branch and look at the implemented code.

This project contains 3 modules: 
- common: where the common code between the modules is stored
- things: this is our Android Things application
- mobile: this is our mobile phone application

---
## 1. Connect the pins

Now that we have our software setup, we need to connect our hardware together.
The pins are same as the previous tasks, so if you are already setup, you don't need this.

![](Diagrams/Temperature_Sensor/pi%20with%20temp%20sensor_bb.png)

---
## 2. Create a Firebase project and add it to the apps

There are 2 ways to setup Firebase from the console or in AndroidStudio.

### A. In the Firebase console

#### 1. Open the console

Open the [Firebase Console](https://console.firebase.google.com)

---
#### 2. Add a new project

Create a new project

![](images/add_project.png)

---
#### 3. Add project details

For the new project, any name is fine.

![](images/new_project.png)

---
#### 4. Add Firebase to the Android apps

Now that we have a Firebase project, we need to add it to our Android Project

![](images/add_to_android.png)

---
#### 5. Setup the Android package name

The package name must match the one in your project, in this case `com.jamescoggan.workshopapp`

![](images/package_name.png)

---
#### 6. Download the json file

After that the package name is setup, you need to download the JSON file, this file is used in the project to configure your Android apps with Firebase.

![](images/download_json.png)

---
#### 7. Add the JSON to the apps

Because we have 2 apps (One for the Android things, another for the mobile phone) you need to add the JSON to both modules root folder as shown below.

![](images/services_json.png)

---
### B. In Android Studio

Alternatively you can create it in Android Studio. [If you have done the setup in Firebase Console above, skip to the step 3.](#3-open-the-firebase-console)

#### 1. Open Firebase Assistant in Android Studio

Click on `Tools` > `Firebase`

![](images/step1.png)

---
#### 2. Expand Realtime Database section

Android Studio will open a new tab that gives your a list of all the features you can automatically install.
In this case we wan't to use the Firebase Realtime database.

![](images/step2.png)

---
#### 3. Open save and retrieve data page

Click on the `Save and retrieve data` button

![](images/step3.png)

---
#### 4. Connect Android Studio to Firebase
First of all, you need to connect your Android Studio to Firebase, so select the click on the `Connect to Firebase` button.

![](images/step4.png)

![](images/step5.png)

---
#### 5. Select the Firebase project you want to use or create a new one.

You need to select what project you want to connect, or you can create a new one.

![](images/step6.png)

---
#### 6. Connect the `mobile` and `things` modules to Firebase

Once connected you can choose the module you want to connect to Firebase, you will need to connect to the mobile and things modules, so to this step twice

![](images/step7.png)

---
#### 7. Add changes to your gradle files

And add the changes to your gradle files. In this case we wan't to connect both

![](images/step8.png)

---
#### 8. Confirm you've added both modules

You have added one module to the Firebase project

![](images/step9.png)

---
## 3. Open the Firebase Console

Now open the [Firebase Console](https://console.firebase.google.com)

![](images/step10.png)

---
## 4. Navigate to the authentication tab

And open the authentication tab

![](images/step11.png)

---
## 3. Enable anonymous login

For this lesson we are going to use the anonymous login, but remember you need to change this for a production build.
So select the anonymous option.

![](images/step12.png)

and enable it.

![](images/step13.png)

---
## 4. Enable the loading of the play services

Because the JSON files were missing ,the loading of the play services was disabled, so now we need to add that code again in both modules, for that:

#### Open `mobile/build.gradle` and go to the end of the file

```
//Todo: Add the missing google-services.json and uncomment the line below
// apply plugin: 'com.google.gms.google-services'
```

#### Remove the Todo and uncomment the code

```
apply plugin: 'com.google.gms.google-services'
```

#### Open `things/build.gradle` and do the same thing

```
//Todo: Add the missing google-services.json and uncomment the line below
// apply plugin: 'com.google.gms.google-services'
```

#### Remove the Todo and uncomment the code

```
apply plugin: 'com.google.gms.google-services'
```

---
## 5. Check the common module files

The common module already has the files needed to save and retrieve data from Firebase, lets have a look at them.
The files are located in `common/src/main/java/com.jamescoggan.common`

### `HomeInformation.kt`

The `HomeInformation.kt` file contains the data structure used to save and retrieve data from Firebase

```
// HomeInformation.kt
data class HomeInformation(var button: Boolean = false,
                           var light: Boolean = false,
                           var temperature: Float = 0f)
```

---
### `HomeInformationLiveData.kt`

We want to observe data changes for the HomeInformation reference in the database.
For better isolation of the code, we are going to use LiveData from the Architectural Components, we could use the value listener direct or implement LiveData on it. 

```
// HomeInformationLiveData.kt
class HomeInformationLiveData(private val databaseReference: DatabaseReference) : LiveData<HomeInformation>() {

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val newValue = snapshot.getValue(HomeInformation::class.java)
            Timber.d("New data received! $newValue")
            value = newValue
        }

        override fun onCancelled(error: DatabaseError) {
            Timber.w(error.toException(), "onCancelled")
        }
    }

    override fun onActive() {
        databaseReference.addValueEventListener(valueEventListener)
    }

    override fun onInactive() {
        databaseReference.removeEventListener(valueEventListener)
    }
}
```

---
### `HomeInformationStorage.kt`

On the other side, we want to store data in the database, so we are going to isolate the logic in a similar way and in the `HomeInformationStorage` class

```
// Common module - HomeInformationStorage.kt
class HomeInformationStorage(private val reference: DatabaseReference) {
    companion object {
        private const val HOME_INFORMATION_LIGHT = "light"
        private const val HOME_INFORMATION_BUTTON = "button"
        private const val HOME_INFORMATION_TEMPERATURE = "temperature"
    }

    fun saveLightState(isOn: Boolean) {
        reference.child(HOME_INFORMATION_LIGHT).setValue(isOn)
    }

    fun saveButtonState(isPressed: Boolean) {
        reference.child(HOME_INFORMATION_BUTTON).setValue(isPressed)
    }

    fun saveTemperature(temperature: Float) {
        reference.child(HOME_INFORMATION_TEMPERATURE).setValue(temperature)
    }
}
```

---
## 6. Setup Firebase on the `things` module

Now that we have looked at the common module files to read/write in Firebase, now is the time to add the logic to our modules, first lets start with Android Things.

Before we can read/store data we need to connect to Firebase, then once connected we can observe the data.
So we are going to load the FirebaseApp on our app creation, and login when our activity is started.

```
// ThingsApplication.kt
FirebaseApp.initializeApp(this)
```

```
    // ThingsActivity.kt
    private fun loginFirebase() {
        val firebase = FirebaseAuth.getInstance()
        firebase.signInAnonymously()
                .addOnSuccessListener { observeData() }
                .addOnFailureListener { Timber.e("Failed to login $it") }
    }

    override fun onStart() {
        super.onStart()
        ...
        loginFirebase()
    }

    private fun observeData(){
    }
```

---
## 7. Setup LiveData and storage on the `things` module

On the same activity, we need to setup LiveData and storage, so on our observeData we are going to load our database reference and create the instances of those objects.
For our implementation of the observer, when we receive the light object, we set that value in the LED.

```
    // ThingsActivity.kt Things
    private var homeInformationLiveData: HomeInformationLiveData? = null
    private var homeInformationStorage: HomeInformationStorage? = null

    private val homeDataObserver = Observer<HomeInformation> { led.setState(it?.light ?: false) }
    
    override fun onStop() {
        homeInformationLiveData?.removeObserver(homeDataObserver)
        ...
    }
    
    private fun observeData() {
        Timber.d("Logged in, observing data")
        val reference = FirebaseDatabase.getInstance().reference.child("home")
        homeInformationLiveData = HomeInformationLiveData(reference)
        homeInformationLiveData?.observe(this, homeDataObserver)
        homeInformationStorage = HomeInformationStorage(reference)
    }    
```

---
## 8. Store data when button state or temperature changes

On the case of the button and temperature changes, we are going to store them in our database when changed.

```
    // ThingsActivity.kt
    private fun onSwitch(state: Boolean) {
        Timber.d("Button pressed: $state")
        homeInformationStorage?.saveButtonState(state)
        homeInformationStorage?.saveLightState(state)
    }

    private fun onTemp(state: Int) {
        Timber.d("Current Temperature: $state")
        homeInformationStorage?.saveTemperature(state.toFloat())
    }
```

## 9. Run the `things` module

Now you can launch your Android Things app. On Android Studio, select the Things module and click the play/run button to run the app in your Android Things device.

[](https://github.com/jamescoggan/AndroidThingsWorkshop/blob/7bd9559e6922fbb9f7ed9b0c360f25b14359d439/images/run.png)

If you open the Firebase Console, and expand your database, you will see something similar to the image below, where the data is being updated from the Android Things.

[](https://github.com/jamescoggan/AndroidThingsWorkshop/blob/3a0f38ebf0f4f1a6086062eacee314aebff99539/images/step16.png)

---
## 10. Setup Firebase on the `mobile` module

If you want to also run the app on your Android Phone, you can continue. Now we need to do the same approach in the mobile app, with some slight differences.

Same as the things application, we need to load our FirebaseApp on app creation and login into Firebase on activity load.
For the case of the mobile app we are using the onResume method instead.

```
// MobileApplication.kt
FirebaseApp.initializeApp(this)
```

```
    // MobileActivity.kt
    override fun onResume() {
        super.onResume()

        loginFirebase()
    }
    
    private fun loginFirebase() {
        val firebase = FirebaseAuth.getInstance()
        firebase.signInAnonymously()
                .addOnSuccessListener { observeData() }
                .addOnFailureListener { Timber.e("Failed to login $it") }
    }

    private fun observeData(){
    }
```

---
## 11. Add UI elements to the `activity_mobile.xml` file

On the mobile app we want to be able to interact with the database changes, so we need to create a few UI elements in our `activity_mobile.xml` file

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context="com.jamescoggan.workshopapp.MainActivity">

    <TextView
        android:id="@+id/temperature_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tools:text="Temperature: 29 °C" />

    <TextView
        android:id="@+id/button_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tools:text="Button is pressed" />

    <TextView
        android:id="@+id/led_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tools:text="Led is on" />

    <ToggleButton
        android:id="@+id/led_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

</LinearLayout>
 
```

---
## 12. Setup the `mobile` module to observe data changes

Now we setup the observer of the data changes and set the UI accordingly
Also remember to stop observing the data on pause.

```
    // MobileActivity.kt
    private var homeInformationLiveData: HomeInformationLiveData? = null
    private var homeInformationStorage: HomeInformationStorage? = null

    private val homeDataObserver = Observer<HomeInformation> {
        val ledText = "Led is " + if (it?.light == true) "on" else "off"
        led_status.text = ledText

        val buttonText = "Button is " + if (it?.button == true) "pressed" else "not pressed"
        button_status.text = buttonText

        val tempText = "Temperature: ${it?.temperature} °C"
        temperature_status.text = tempText

        led_button.isChecked = it?.light ?: false
    }   
```

---
## 13. Observe data changes

Now we can observe the data after the login success

``` 
    // MobileActivity.kt
    private fun observeData() {
        Timber.d("Logged in, observing data")
        val reference = FirebaseDatabase.getInstance().reference.child("home")
        homeInformationLiveData = HomeInformationLiveData(reference)
        homeInformationLiveData?.observe(this, homeDataObserver)
        homeInformationStorage = HomeInformationStorage(reference)
    }
    
    override fun onPause() {
        homeInformationLiveData?.removeObserver(homeDataObserver)

        super.onPause()
    }    
```

---
## 14. Save the button state to the database

For our toggle button, we wan't to save its state selection in the led object of the database.

```
    // MobileActivity.kt
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        led_button.setOnCheckedChangeListener { _, state -> setLed(state) }
    }
    
    private fun setLed(state: Boolean) {
        homeInformationStorage?.saveLightState(state)
    }
```
