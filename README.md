# OS X Helper

Integrating with OS X in a Java application is made very easy by the availability of classes in the `com.apple.eawt`
package, which allow cross-platform Java applications to behave more like native OS X apps. However, using these classes
prevents compilation, and in some cases, execution on non-OS X platforms.

An easy way to deal with this is to use Reflection, but that quickly gets messy and difficult to use, especially when
more advanced features are needed.

This is where OS X Helper comes in. OSXH has already done the reflection, hidden away behind classes and interfaces that
are completely cross-platform. OSXH behaves like `com.apple.eawt`, but it does't require that package to compile.

## Examples

### Setting the dock icon

```java
import java.awt.Image;
import me.kyleclemens.osx.HelperApplication;

class Example {

    public static void main(final String[] args) {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            final HelperApplication application = new HelperApplication();
            final Image dockImage = ...;
            application.setDockIconImage(dockImage);
        }
    }

}
```

### Listening for app reopens

```java
import me.kyleclemens.osx.HelperApplication;

class Example {

    public static void main(final String[] args) {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            final HelperApplication application = new HelperApplication();
            // with a lambda
            application.addAppEventListener((HelperAppReOpenedListener) event -> {
                // do something
            });
            // without
            application.addAppEventListener(new HelperAppReOpenedListener() {
                @Override
                public void appReOpened(final HelperAppReOpenedEvent event) {
                    // do something
                }
            });
        }
    }

}
```

## Behind the scenes

OSXH has recreated the `com.apple.eawt` API in structure, and it uses Reflection to communicate between this facade and
the real API. Listeners are registered in the real API using proxies that match all methods. Events are converted into
facade events before being passed back to facade listeners.

OS X Helper is written in Kotlin, which is 100% interoperable with Java. OSXH works in both Kotlin and Java, and should
work naturally in both, as well.
