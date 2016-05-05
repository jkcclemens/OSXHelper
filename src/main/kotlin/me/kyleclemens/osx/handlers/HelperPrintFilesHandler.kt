/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.osx.handlers

import me.kyleclemens.osx.events.HelperPrintFilesEvent

interface HelperPrintFilesHandler {
    fun printFiles(event: HelperPrintFilesEvent)
}
