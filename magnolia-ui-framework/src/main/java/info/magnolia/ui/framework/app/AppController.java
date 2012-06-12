package info.magnolia.ui.framework.app;

import info.magnolia.ui.framework.view.ViewPort;

/**
 * @version $Id$
 */
public interface AppController {

    void startIfNotAlreadyRunning(String name);

    void startIfNotAlreadyRunningThenFocus(String name);

    void stopApp(String name);

    void stopCurrentApp();

    boolean isAppStarted(String name);

    void setViewPort(ViewPort viewport);
}
