package com.mrsiktyyy.textdark

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private var pendingSave: Pair<String, String>? = null // base64, name

    private val openLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { deliver(it) }
    }
    private val saveLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val p = pendingSave ?: return@registerForActivityResult
        if (uri == null) return@registerForActivityResult
        write(uri, p.first)
        js("onSaved(${q(p.second)}, ${q(uri.toString())})")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        web = WebView(this)
        setContentView(web)
        ViewCompat.setOnApplyWindowInsetsListener(web) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            WindowInsetsCompat.CONSUMED
        }
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this)).build()
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
        }
        web.setBackgroundColor(0xFF0B0D12.toInt())
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(v: WebView, r: WebResourceRequest): WebResourceResponse? =
                loader.shouldInterceptRequest(r.url)
            override fun onPageFinished(v: WebView, url: String) { handleIntent(intent) }
        }
        web.addJavascriptInterface(Bridge(), "Android")
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); handleIntent(intent) }

    private fun handleIntent(i: Intent?) {
        if (i?.action == Intent.ACTION_VIEW && i.data != null) { deliver(i.data!!); i.data = null }
    }

    private fun deliver(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } catch (_: Exception) {}
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
        var name = "document.txt"
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) { val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (i >= 0) name = c.getString(i) }
        }
        js("onOpened(${q(name)}, ${q(Base64.encodeToString(bytes, Base64.NO_WRAP))}, ${q(uri.toString())})")
    }

    private fun write(uri: Uri, b64: String) {
        contentResolver.openOutputStream(uri, "wt")?.use { it.write(Base64.decode(b64, Base64.DEFAULT)) }
    }

    private fun q(s: String) = "\u0027" + s.replace("\\", "\\\\").replace("\u0027", "\\\u0027").replace("\n", "\\n") + "\u0027"
    private fun js(code: String) = runOnUiThread { web.evaluateJavascript(code, null) }

    inner class Bridge {
        @JavascriptInterface fun openFile() = runOnUiThread { openLauncher.launch(arrayOf("*/*")) }
        @JavascriptInterface fun saveFile(name: String, b64: String, uri: String) = runOnUiThread {
            if (uri.isNotEmpty()) {
                try { write(Uri.parse(uri), b64); js("onSaved(${q(name)}, ${q(uri)})"); return@runOnUiThread } catch (_: Exception) {}
            }
            pendingSave = b64 to name
            saveLauncher.launch(name)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { js("window.onBack && onBack()") }
}
