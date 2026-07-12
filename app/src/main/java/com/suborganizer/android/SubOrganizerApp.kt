package com.suborganizer.android

import android.app.Application
import com.suborganizer.android.data.SupabaseClientProvider

class SubOrganizerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Touch the lazy client once at startup so the first real usage (login screen)
        // doesn't pay the init cost.
        SupabaseClientProvider.client
    }
}
