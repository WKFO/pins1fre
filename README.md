# pins1fre
### An Android app passcode protection library for user to protect the app with a four digit code.

<img align="center" src='https://github.com/KeyLo99/pins1fre/blob/master/pins1fre/src/main/res/drawable/android_phone.png' width='335' height='679'/>

# Usage

#### Add PinS1fre module to your App Gradle dependencies

```
dependencies {
    //Other dependencies...
    
    compile 'com.tht.applocker:pins1fre:1.0:release@aar'
}

```

#### Initializing the variable

```
PinS1fre pinS1fre = new PinS1fre(getApplicationContext());
```

#### Available Functions
```
showLock, haveLock, setLock, delLock, changeLock (See the example folder for further information)
```

```
void showLock(Activity activity, PinS1fre.PasscodeEvent delegate) : Checks if the app have a pinlock. If so, shows the lock screen. With the delegate, you can determine what is going to happen after password entered.
```
```
boolean haveLock() : Checks if the app have a pinlock. Returns a true/false value.
```
```
void setLock(Activity activity) : Sets the pinlock.
```
```
void delLock(Activity activity) : For removing the pinlock.
```
```
void changeLock(Activity activity) : For changing the pinlock.
```

# Coded by KeyLo99
