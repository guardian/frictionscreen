package guardian.com.friction.demo

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import guardian.com.frictionscreen.FrictionScreenManager
import kotlinx.android.synthetic.main.activity_article.*

class ArticleActivity: AppCompatActivity() {

    private lateinit var frictionScreenManager: FrictionScreenManager
    private val articleUrl = "https://www.theguardian.com/education/2018/nov/22/3000-pounds-for-a-school-trip-you-must-be-joking"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        webview.loadUrl(articleUrl)

        // This can move to application class to avoid initializing multiple times
        val config = FrictionScreenManager.Config(3, 1, true, true)
        frictionScreenManager = FrictionScreenManager(this, config)

        frictionScreenManager.recordArticleRead("article-" + System.currentTimeMillis())
    }

    override fun onResume() {
        super.onResume()

        if(frictionScreenManager.shouldShowSubsScreen()) {
            showPremiumDialog()
            frictionScreenManager.setSubscriptionScreenDisplayed()
        }
    }

    private fun showPremiumDialog() {
        val builder = AlertDialog.Builder(this)
                .setTitle("Premium Purchase")
                .setView(R.layout.dialog_premium)
                .setPositiveButton("Buy") { _, _ -> Toast.makeText(this@ArticleActivity, getString(R.string.premium_purchase), Toast.LENGTH_LONG).show() }
                .setNegativeButton("Close") { _, _ ->}

        builder.create().show()
    }
}

