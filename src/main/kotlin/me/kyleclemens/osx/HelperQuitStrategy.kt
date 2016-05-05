/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.osx

enum class HelperQuitStrategy {

    SYSTEM_EXIT_0,
    CLOSE_ALL_WINDOWS;

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun convert(original: Any): HelperQuitStrategy {
            val originalClass = original.javaClass
            if (originalClass.name != "com.apple.eawt.${HelperQuitStrategy::class.java.simpleName.substring(6)}") {
                throw IllegalArgumentException("Not the right object")
            }
            return valueOf(originalClass.simpleName)
        }
    }

}
