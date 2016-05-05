/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.osx.events

open class HelperEventObject(source: Any?) {

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Transient protected var _source: Any

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    val source: Any
        get() = this._source

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    init {
        if (source == null) {
            throw IllegalArgumentException("null source")
        }
        this._source = source
    }

    /**
     * Returns a String representation of this EventObject.

     * @return  A a String representation of this EventObject.
     */
    override fun toString(): String {
        return this.javaClass.name + "[source=" + this.source + "]"
    }

}
