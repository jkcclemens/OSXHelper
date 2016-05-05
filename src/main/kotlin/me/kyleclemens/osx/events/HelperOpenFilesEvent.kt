/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.util.os.osx.events

import java.io.File

class HelperOpenFilesEvent(source: Any?, files: List<File>, val searchTerm: String) : HelperFilesEvent(source, files) {

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun convert(original: Any): HelperOpenFilesEvent {
            val originalClass = original.javaClass
            if (originalClass.name != "com.apple.eawt.AppEvent\$${HelperOpenFilesEvent::class.java.simpleName.substring(6)}") {
                throw IllegalArgumentException("Not the right event")
            }
            val source = originalClass.getMethod("getSource")(original)
            @Suppress("UNCHECKED_CAST")
            val files = originalClass.getMethod("getFiles")(original) as List<File>
            val searchTerm = originalClass.getDeclaredMethod("getSearchTerm")(original) as String
            return HelperOpenFilesEvent(source, files, searchTerm)
        }
    }
}
