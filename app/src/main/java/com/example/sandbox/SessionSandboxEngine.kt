package com.example.sandbox

import android.net.Uri
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.data.model.ClonedInstanceEntity
import org.json.JSONObject

object SessionSandboxEngine {

  private val blockedTrackerDomains = listOf(
    "doubleclick.net",
    "googlesyndication.com",
    "google-analytics.com",
    "connect.facebook.net/en_US/fbevents.js",
    "analytics.tiktok.com",
    "ads-twitter.com",
    "scorecardresearch.com",
    "criteo.com",
    "taboola.com",
    "outbrain.com",
  )

  private val trackingQueryParams = setOf(
    "utm_source",
    "utm_medium",
    "utm_campaign",
    "utm_term",
    "utm_content",
    "fbclid",
    "gclid",
    "igshid",
    "twclid",
    "mc_cid",
  )

  fun isTrackerUrl(url: String): Boolean {
    val lower = url.lowercase()
    return blockedTrackerDomains.any { lower.contains(it) }
  }

  fun sanitizeTrackingParams(url: String, enabled: Boolean): String {
    if (!enabled) return url
    return try {
      val uri = Uri.parse(url)
      val queryNames = uri.queryParameterNames
      if (queryNames.isEmpty() || queryNames.none { it.lowercase() in trackingQueryParams }) {
        return url
      }
      val builder = uri.buildUpon().clearQuery()
      for (name in queryNames) {
        if (name.lowercase() !in trackingQueryParams) {
          for (value in uri.getQueryParameters(name)) {
            builder.appendQueryParameter(name, value)
          }
        }
      }
      builder.build().toString()
    } catch (_: Exception) {
      url
    }
  }

  /**
   * Parses a JSON object string into a key-value map without triggering WebView/Chromium initialization.
   */
  fun parseJsonMap(rawJson: String): Map<String, String> {
    if (rawJson.isBlank() || rawJson == "{}") return emptyMap()
    return try {
      val json = JSONObject(rawJson)
      val result = linkedMapOf<String, String>()
      val keys = json.keys()
      while (keys.hasNext()) {
        val k = keys.next()
        result[k] = json.optString(k, "")
      }
      result
    } catch (_: Exception) {
      emptyMap()
    }
  }

  fun encodeJsonMap(map: Map<String, String>): String {
    return try {
      val json = JSONObject()
      map.forEach { (k, v) -> json.put(k, v) }
      json.toString()
    } catch (_: Exception) {
      "{}"
    }
  }

  /**
   * Restores the isolated CookieJar for the given instance.
   * Only touches Android's Chromium CookieManager when syncWithSystemWebView is true,
   * preventing unnecessary Chromium GPU/MESA rendernode initialization in software emulators.
   */
  fun applyIsolatedCookiesForInstance(
    instance: ClonedInstanceEntity,
    syncWithSystemWebView: Boolean = false,
  ) {
    if (!syncWithSystemWebView) return
    try {
      val cookieManager = CookieManager.getInstance()
      cookieManager.setAcceptCookie(true)
      val baseUri = Uri.parse(instance.baseUrl)
      val host = baseUri.host ?: return
      val origin = "${baseUri.scheme ?: "https"}://$host"

      val currentCookiesRaw = cookieManager.getCookie(origin)
      if (!currentCookiesRaw.isNullOrBlank()) {
        currentCookiesRaw.split(";").forEach { pair ->
          val eqIdx = pair.indexOf('=')
          val name = if (eqIdx > 0) pair.substring(0, eqIdx).trim() else pair.trim()
          if (name.isNotEmpty()) {
            cookieManager.setCookie(origin, "$name=; Max-Age=0; path=/")
          }
        }
      }

      val json = JSONObject(instance.cookieJarJson.ifBlank { "{}" })
      val keys = json.keys()
      while (keys.hasNext()) {
        val key = keys.next()
        val value = json.optString(key, "")
        cookieManager.setCookie(origin, "$key=$value; path=/; SameSite=Lax")
      }
      cookieManager.flush()
    } catch (_: Exception) {
      // Safe fallback
    }
  }

  /**
   * Captures current cookies for the URL and merges them into a JSON string for this instance.
   */
  fun captureCookiesAsJson(
    url: String,
    fallbackExistingJson: String,
    syncWithSystemWebView: Boolean = false,
  ): Pair<String, Int> {
    if (!syncWithSystemWebView) {
      val map = parseJsonMap(fallbackExistingJson)
      return Pair(fallbackExistingJson.ifBlank { "{}" }, map.size)
    }
    return try {
      val cookieManager = CookieManager.getInstance()
      val raw = cookieManager.getCookie(url)
      val merged = JSONObject(fallbackExistingJson.ifBlank { "{}" })
      if (!raw.isNullOrBlank()) {
        raw.split(";").forEach { cookiePair ->
          val idx = cookiePair.indexOf('=')
          if (idx > 0) {
            val k = cookiePair.substring(0, idx).trim()
            val v = cookiePair.substring(idx + 1).trim()
            if (k.isNotEmpty()) {
              merged.put(k, v)
            }
          }
        }
      }
      Pair(merged.toString(), merged.length())
    } catch (_: Exception) {
      val map = parseJsonMap(fallbackExistingJson)
      Pair(fallbackExistingJson.ifBlank { "{}" }, map.size)
    }
  }

  private fun escapeJsStringLiteral(raw: String): String {
    val sb = StringBuilder("\"")
    for (ch in raw) {
      when (ch) {
        '\\' -> sb.append("\\\\")
        '"' -> sb.append("\\\"")
        '\n' -> sb.append("\\n")
        '\r' -> sb.append("\\r")
        '\t' -> sb.append("\\t")
        else -> sb.append(ch)
      }
    }
    sb.append("\"")
    return sb.toString()
  }

  fun buildSandboxInjectionScript(instance: ClonedInstanceEntity): String {
    val prefix = "__ts_ns_${instance.id}_"
    val safeLocalJson = escapeJsStringLiteral(instance.localStorageJson.ifBlank { "{}" })
    val safeSessionJson = escapeJsStringLiteral(instance.sessionStorageJson.ifBlank { "{}" })
    val spoofCores = 4 + ((instance.id % 4).toInt() * 2)
    val spoofMemory = 4 + ((instance.id % 3).toInt() * 2)
    val canvasNoise = ((instance.id % 7) + 1).toInt()

    return """
      (function() {
        if (window.__twinSpaceInitializedFor === ${instance.id}) return;
        window.__twinSpaceInitializedFor = ${instance.id};
        const NS_PREFIX = "$prefix";

        try {
          const rawGetItem = Storage.prototype.getItem;
          const rawSetItem = Storage.prototype.setItem;
          const rawRemoveItem = Storage.prototype.removeItem;

          Storage.prototype.getItem = function(key) {
            return rawGetItem.call(this, NS_PREFIX + key);
          };
          Storage.prototype.setItem = function(key, val) {
            rawSetItem.call(this, NS_PREFIX + key, val);
            if (window.TwinSpaceBridge && window.TwinSpaceBridge.onStorageMutated) {
              window.__tsSyncStorage();
            }
          };
          Storage.prototype.removeItem = function(key) {
            rawRemoveItem.call(this, NS_PREFIX + key);
            if (window.TwinSpaceBridge && window.TwinSpaceBridge.onStorageMutated) {
              window.__tsSyncStorage();
            }
          };

          const initialLocal = JSON.parse($safeLocalJson);
          Object.keys(initialLocal).forEach(function(k) {
            rawSetItem.call(window.localStorage, NS_PREFIX + k, initialLocal[k]);
          });

          const initialSession = JSON.parse($safeSessionJson);
          Object.keys(initialSession).forEach(function(k) {
            rawSetItem.call(window.sessionStorage, NS_PREFIX + k, initialSession[k]);
          });

          window.__tsSyncStorage = function() {
            try {
              const localOut = {};
              for (let i = 0; i < window.localStorage.length; i++) {
                const fullKey = window.localStorage.key(i);
                if (fullKey && fullKey.indexOf(NS_PREFIX) === 0) {
                  const cleanKey = fullKey.substring(NS_PREFIX.length);
                  localOut[cleanKey] = rawGetItem.call(window.localStorage, fullKey);
                }
              }
              const sessionOut = {};
              for (let j = 0; j < window.sessionStorage.length; j++) {
                const fullSKey = window.sessionStorage.key(j);
                if (fullSKey && fullSKey.indexOf(NS_PREFIX) === 0) {
                  const cleanSKey = fullSKey.substring(NS_PREFIX.length);
                  sessionOut[cleanSKey] = rawGetItem.call(window.sessionStorage, fullSKey);
                }
              }
              if (window.TwinSpaceBridge) {
                window.TwinSpaceBridge.onStorageMutated(
                  JSON.stringify(localOut),
                  JSON.stringify(sessionOut),
                  Object.keys(localOut).length + Object.keys(sessionOut).length
                );
              }
            } catch (err) {}
          };
          window.__tsSyncStorage();
        } catch (storageErr) {}

        if (${instance.spoofDeviceFingerprint}) {
          try {
            Object.defineProperty(navigator, 'hardwareConcurrency', { get: function() { return $spoofCores; } });
            Object.defineProperty(navigator, 'deviceMemory', { get: function() { return $spoofMemory; } });
            const origToDataURL = HTMLCanvasElement.prototype.toDataURL;
            HTMLCanvasElement.prototype.toDataURL = function() {
              const ctx = this.getContext('2d');
              if (ctx) {
                ctx.fillStyle = 'rgba(0, 240, 255, 0.0$canvasNoise)';
                ctx.fillRect(0, 0, 1, 1);
              }
              return origToDataURL.apply(this, arguments);
            };
          } catch (fpErr) {}
        }

        if (${instance.forceDarkWebMode}) {
          try {
            const style = document.createElement('style');
            style.id = '__ts_dark_force';
            style.innerHTML = 'html { background-color: #0B0F19 !important; color-scheme: dark !important; }';
            document.head.appendChild(style);
          } catch (cssErr) {}
        }
      })();
    """.trimIndent()
  }
}

class TwinSpaceJsBridge(
  private val onStorageUpdated: (localStorageJson: String, sessionStorageJson: String, totalKeys: Int) -> Unit,
) {
  @JavascriptInterface
  fun onStorageMutated(localStorageJson: String, sessionStorageJson: String, totalKeys: Int) {
    onStorageUpdated(localStorageJson, sessionStorageJson, totalKeys)
  }
}

fun WebView.triggerSandboxSnapshotSync(instance: ClonedInstanceEntity) {
  evaluateJavascript(SessionSandboxEngine.buildSandboxInjectionScript(instance), null)
}
