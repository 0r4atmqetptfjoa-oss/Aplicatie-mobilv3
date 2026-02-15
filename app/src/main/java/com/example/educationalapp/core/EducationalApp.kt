package com.example.educationalapp.core

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base application class for the EducationalApp.
 *
 * This class is annotated with [HiltAndroidApp] so that Dagger Hilt
 * can perform dependency injection across the entire application.  By
 * locating the class in the `core` package we match the application
 * name specified in the `AndroidManifest.xml` (see the crash log for
 * reference), ensuring that the Android runtime can instantiate the
 * class correctly at startup.
 */
@HiltAndroidApp
class EducationalApp : Application()
