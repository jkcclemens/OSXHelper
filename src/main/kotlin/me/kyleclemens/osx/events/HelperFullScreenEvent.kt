/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.osx.events

import java.awt.Window

class HelperFullScreenEvent(source: Any?, val window: Window) : HelperAppEvent(source) {

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun convert(original: Any): HelperFullScreenEvent {
            val originalClass = original.javaClass
            if (originalClass.name != "com.apple.eawt.AppEvent\$${HelperFullScreenEvent::class.java.simpleName.substring(6)}") {
                throw IllegalArgumentException("Not the right event")
            }
            val source = originalClass.getMethod("getSource")(original)
            val window = originalClass.getDeclaredMethod("getWindow")(original) as Window
            return HelperFullScreenEvent(source, window)
        }
    }
}
