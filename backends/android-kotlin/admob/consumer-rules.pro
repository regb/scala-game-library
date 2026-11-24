# Play Services Ads brings old Room and WorkManager versions, whose own rules keep these classes but
# not the constructors called by reflection. R8 full mode then removes the constructors, and a
# minified app crashes on start (Failed to create an instance of androidx.work.impl.WorkDatabase).
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class * extends androidx.work.InputMerger { <init>(); }
