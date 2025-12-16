# Next Steps - Build Failed

It seems the path to your Java 17 installation was not correct. The build failed with the error: `Java home supplied is invalid`.

Please follow these steps to find the correct path to your Java 17 installation and update the project configuration.

## 1. Find your Java 17 Installation Path

Here are a few ways to find your Java 17 installation path on Windows:

### Option A: Check Common Installation Directories

Look in the following common directories for a folder named something like `jdk-17`, `jdk-17.0.x`, or similar:

*   `C:\Program Files\Java\`
*   `C:\Program Files\Android\Android Studio\jbr` (Android Studio's bundled Java runtime)
*   `C:\Program Files\Eclipse Adoptium\`
*   `C:\Program Files\Amazon Corretto\`

### Option B: Use Command Prompt

1.  Open a Command Prompt.
2.  If you have multiple Java versions, you can try to find the path by typing `where java`. This might give you a hint.

## 2. Update `gradle.properties`

Once you have found the correct path, open the `gradle.properties` file in the project's root directory.

Find the line that starts with `org.gradle.java.home=` and replace the path with the correct one you found in the previous step.

**Important:** Remember to use double backslashes (`\\`) for the path in the `gradle.properties` file. For example:

`org.gradle.java.home=C:\\Program Files\\Java\\jdk-17.0.2`

## 3. Run the Build

After updating the `gradle.properties` file, open a terminal in the project directory and run the following command:

```
gradlew.bat assembleDebug
```

This should now build the project successfully.