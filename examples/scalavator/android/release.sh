
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore release.keystore target/android/output/scalavator-android-release-unsigned.apk scalavator

zipalign -v 4 target/android/output/scalavator-android-release-unsigned.apk scalavator.apk 

