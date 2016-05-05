/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.util.os.osx.events

import java.net.URI

class HelperOpenURIEvent(source: Any?, val uri: URI) : HelperAppEvent(source) {
    companion object {
        @Suppress("unused")
        @JvmStatic
        fun convert(original: Any): HelperOpenURIEvent {
            val originalClass = original.javaClass
            if (originalClass.name != "com.apple.eawt.AppEvent\$${HelperOpenURIEvent::class.java.simpleName.substring(6)}") {
                throw IllegalArgumentException("Not the right event")
            }
            val source = originalClass.getMethod("getSource")(original)
            val uri = originalClass.getDeclaredMethod("getURI")(original) as URI
            return HelperOpenURIEvent(source, uri)
        }
    }
}
