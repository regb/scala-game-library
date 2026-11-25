<?xml version="1.0" encoding="utf-8"?>
<!-- AndroidGraphicsProxy resolves drawable names at runtime with getIdentifier.
     Keep every density variant, including legacy assets_dir drawables. The
     AdMob settings are looked up by name too (AndroidAdMobAds.fromResources):
     without them a release falls back to the test ad units. Other
     resource types can still shrink. This does not affect files in assets/. -->
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:keep="@drawable/*,@string/sgl_admob_*,@bool/sgl_admob_*" />
