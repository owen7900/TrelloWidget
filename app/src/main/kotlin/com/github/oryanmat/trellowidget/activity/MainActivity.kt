package com.github.oryanmat.trellowidget.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.util.AUTH_URL
import com.github.oryanmat.trellowidget.util.TOKEN_PREF_KEY
import com.github.oryanmat.trellowidget.util.preferences

class MainActivity : androidx.fragment.app.FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val userToken = preferences().getString(TOKEN_PREF_KEY, "")
            replaceFragment(if (userToken == null || userToken.isEmpty()) LoginFragment() else LoggedInFragment())
        }
    }

    fun startBrowserWithAuthURL(view: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL))

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_VIEW) {
            saveUserToken(intent)
        }
    }

    private fun saveUserToken(intent: Intent) {
        preferences().edit()
                .putString(TOKEN_PREF_KEY, intent.data?.fragment ?: "")
                .apply()

        replaceFragment(LoggedInFragment())
    }

    @JvmOverloads fun logout(view: View? = null) {
        preferences().edit()
                .remove(TOKEN_PREF_KEY)
                .apply()

        replaceFragment(LoginFragment())
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) = supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
}