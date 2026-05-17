package com.devara.splitnow.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class UrlOpener actual constructor() {
    actual fun open(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}
