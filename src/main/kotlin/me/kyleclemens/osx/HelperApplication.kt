/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.osx

import me.kyleclemens.osx.handlers.HelperAboutHandler
import me.kyleclemens.osx.handlers.HelperOpenFilesHandler
import me.kyleclemens.osx.handlers.HelperOpenURIHandler
import me.kyleclemens.osx.handlers.HelperPreferencesHandler
import me.kyleclemens.osx.handlers.HelperPrintFilesHandler
import me.kyleclemens.osx.handlers.HelperQuitHandler
import me.kyleclemens.osx.listeners.HelperAppEventListener
import java.awt.Image
import java.awt.PopupMenu
import java.awt.Window
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import javax.swing.JMenuBar

class HelperApplication(application: Any? = null) {

    private val applicationClass = Class.forName("com.apple.eawt.Application")
    private val application = application ?: applicationClass.getDeclaredMethod("getApplication")(null)

    init {
        if (application != null && !this.applicationClass.isAssignableFrom(application.javaClass)) {
            throw IllegalArgumentException("Invalid Application given.")
        }
    }

    private fun getMethod(name: String, parameters: List<Any>): Method {
        return this.applicationClass.getDeclaredMethod(
            name,
            *parameters
                .map {
                    if (it is String) {
                        Class.forName(it)
                    } else if (it is Class<*>) {
                        it
                    } else {
                        throw IllegalArgumentException("Parameter list must be composed of Strings and Classes")
                    }
                }
                .toTypedArray()
        )
    }

    private fun callMethod(name: String, parameters: List<Any> = listOf(), vararg args: Any) = this.getMethod(name, parameters)(this.application, *args)

    private fun proxyClass(className: String, methodHandler: (Any?, Method, Array<Any?>?) -> Any?): Any {
        val clazz = Class.forName(className)
        val proxy = Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz)) { proxy, method, args -> methodHandler(proxy, method, args) }
        return proxy
    }

    private fun Class<*>.toHelperClass(): Class<*> {
        val parts = this.name.split(".")
        if (parts.size != 4 || parts.subList(0, 3) != listOf("com", "apple", "eawt")) {
            throw IllegalArgumentException("Class (${this.name}) cannot be converted to a helper class.")
        }
        val name = parts[3].split("$").last()
        val pkg = when {
            name.endsWith("Event") -> "events."
            name.endsWith("Handler") -> "handlers."
            name.endsWith("Listener") -> "listeners."
            else -> ""
        }
        return try {
            Class.forName(HelperApplication::class.java.name.split(".").let { it.subList(0, it.size - 1) }.joinToString(".") + ".${pkg}Helper$name")
        } catch(ex: ClassNotFoundException) {
            throw IllegalArgumentException("No helper class for ${this.name}.")
        }
    }

    private fun getHelper(any: Any?): Class<*>? {
        if (any == null) return null
        return any.javaClass.interfaces.first { it.simpleName.startsWith("Helper") }
    }

    private fun proxyListenerOrHandler(listenerOrHandler: Any, store: Boolean = false): Any {
        val className = this.getHelper(listenerOrHandler)?.simpleName ?: throw IllegalArgumentException("Invalid listener/handler provided")
        val proxy = this.proxyClass("com.apple.eawt.${className.substring(6)}") { proxy, method, args ->
            val listenerMethod: Method? = listenerOrHandler.javaClass.declaredMethods.find {
                it.name == method.name
                    && it.returnType == method.returnType
                    && it.parameterTypes.asList() == method.parameterTypes.map { c -> c.toHelperClass() }
            }
            val convertedArgs = args?.map { any ->
                if (any == null) return@map any
                val helperClass = try {
                    any.javaClass.toHelperClass()
                } catch(ex: ClassNotFoundException) {
                    return@map any
                }
                val convert = try {
                    helperClass.getDeclaredMethod("convert", Any::class.java)
                } catch(ex: NoSuchMethodException) {
                    println("Warning: Helper class $helperClass does not have a convert method.")
                    return@map any
                }
                return@map convert(null, any)
            }
            if (listenerMethod != null) {
                return@proxyClass listenerMethod(listenerOrHandler, *convertedArgs?.toTypedArray() ?: arrayOf())
            } else {
                throw IllegalStateException("Unknown method (${method.toString()}) called on ${listenerOrHandler.javaClass.name}.")
            }
        }
        if (store) {
            this.proxies[listenerOrHandler] = proxy
        }
        return proxy
    }

    private val proxies = hashMapOf<Any, Any>()

    fun addAppEventListener(listener: HelperAppEventListener) {
        this.callMethod("addAppEventListener", listOf("com.apple.eawt.AppEventListener"), this.proxyListenerOrHandler(listener, store = true))
    }

    fun removeAppEventListener(listener: HelperAppEventListener) {
        if (listener !in this.proxies) {
            throw IllegalArgumentException("Listener is not registered")
        }
        this.callMethod("removeAppEventListener", listOf("com.apple.eawt.AppEventListener"), this.proxies[listener]!!)
    }

    fun setAboutHandler(handler: HelperAboutHandler) {
        this.callMethod("setAboutHandler", listOf("com.apple.eawt.AboutHandler"), this.proxyListenerOrHandler(handler))
    }

    fun setPreferencesHandler(handler: HelperPreferencesHandler) {
        this.callMethod("setPreferencesHandler", listOf("com.apple.eawt.PreferencesHandler"), this.proxyListenerOrHandler(handler))
    }

    fun setOpenFileHandler(handler: HelperOpenFilesHandler) {
        this.callMethod("setOpenFileHandler", listOf("com.apple.eawt.OpenFileHandler"), this.proxyListenerOrHandler(handler))
    }

    fun setPrintFileHandler(handler: HelperPrintFilesHandler) {
        this.callMethod("setPrintFileHandler", listOf("com.apple.eawt.PrintFileHandler"), this.proxyListenerOrHandler(handler))
    }

    fun setOpenURIHandler(handler: HelperOpenURIHandler) {
        this.callMethod("setOpenURIHandler", listOf("com.apple.eawt.OpenURIHandler"), this.proxyListenerOrHandler(handler))
    }

    fun setQuitHandler(handler: HelperQuitHandler) {
        this.callMethod("setQuitHandler", listOf("com.apple.eawt.QuitHandler"), this.proxyListenerOrHandler(handler))
    }

    fun setQuitStrategy(strategy: HelperQuitStrategy) {
        val quitStrategyClass = Class.forName("com.apple.eawt.QuitStrategy")
        val realStrategy = quitStrategyClass.getDeclaredField(strategy.name).get(null)
        this.callMethod("setQuitStrategy", listOf(quitStrategyClass), realStrategy)
    }

    fun enableSuddenTermination() {
        this.callMethod("enableSuddenTermination")
    }

    fun disableSuddenTermination() {
        this.callMethod("disableSuddenTermination")
    }

    fun requestForeground(var1: Boolean) {
        this.callMethod("requestForeground", listOf(Boolean::class.java), var1)
    }

    fun requestUserAttention(var1: Boolean) {
        this.callMethod("requestUserAttention", listOf(Boolean::class.java), var1)
    }

    fun openHelpViewer() {
        this.callMethod("openHelpViewer")
    }

    fun setDockMenu(menu: PopupMenu) {
        this.callMethod("setDockMenu", listOf(PopupMenu::class.java), menu)
    }

    fun getDockMenu(): PopupMenu {
        return this.callMethod("getDockMenu") as PopupMenu
    }

    fun setDockIconImage(image: Image) {
        this.callMethod("setDockIconImage", listOf(Image::class.java), image)
    }

    fun getDockIconImage(): Image {
        return this.callMethod("getDockIconImage") as Image
    }

    fun setDockIconBadge(badge: String) {
        this.callMethod("setDockIconBadge", listOf(String::class.java), badge)
    }

    fun setDefaultMenuBar(menuBar: JMenuBar) {
        this.callMethod("setDefaultMenuBar", listOf(JMenuBar::class.java), menuBar)
    }

    fun requestToggleFullScreen(window: Window) {
        this.callMethod("requestToogleFullScreen", listOf(Window::class.java), window)
    }

}
