/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.osx

class HelperQuitResponse internal constructor(private val realQuitResponse: Any) {

    fun performQuit() {
        this.realQuitResponse.javaClass.getDeclaredMethod("performQuit")(this.realQuitResponse)
    }

    fun cancelQuit() {
        this.realQuitResponse.javaClass.getDeclaredMethod("cancelQuit")(this.realQuitResponse)
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun convert(original: Any): HelperQuitResponse {
            val originalClass = original.javaClass
            if (originalClass.name != "com.apple.eawt.QuitResponse") throw IllegalArgumentException("Not the right event")
            return HelperQuitResponse(original)
        }
    }

}
