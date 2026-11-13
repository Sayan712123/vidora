package com.vidora.app.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

/**
 * The official YouTube embedded player, hosted in a WebView.
 *
 * This is the only playback path the app uses: no stream extraction, no third-party
 * player, no download. The iframe is served by youtube.com and the video is played by
 * YouTube's own player, which is what the YouTube API Services Terms require.
 *
 * The WebView is deliberately minimal - no JavaScript bridges, no file access, no
 * geolocation - and it is destroyed with the screen so nothing lingers in memory.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = true
) {
    val html = remember(videoId, autoplay) { playerHtml(videoId, autoplay) }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black),
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(AndroidColor.BLACK)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    // Nothing below is needed for an embedded player.
                    allowFileAccess = false
                    allowContentAccess = false
                    setGeolocationEnabled(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                }
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                loadDataWithBaseURL(
                    "https://www.youtube.com",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        },
        onRelease = { webView ->
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        }
    )
}

/**
 * A 16:9 iframe that fills the WebView. `playsinline=1` keeps playback inside the page
 * instead of handing off to the native app, and `rel=0` limits related videos to the
 * same channel.
 */
private fun playerHtml(videoId: String, autoplay: Boolean): String {
    val autoplayFlag = if (autoplay) 1 else 0
    return """
        <!DOCTYPE html>
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
            <style>
              html, body { margin:0; padding:0; background:#000; height:100%; overflow:hidden; }
              .frame { position:absolute; inset:0; }
              iframe { width:100%; height:100%; border:0; display:block; }
            </style>
          </head>
          <body>
            <div class="frame">
              <iframe
                src="https://www.youtube.com/embed/$videoId?playsinline=1&autoplay=$autoplayFlag&rel=0&modestbranding=1"
                title="Vidora player"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen"
                allowfullscreen>
              </iframe>
            </div>
          </body>
        </html>
    """.trimIndent()
}

/** A plain black 16:9 box, shown while the video metadata is still loading. */
@Composable
fun PlayerPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    )
}
