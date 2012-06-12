package info.magnolia.ui.framework.app;

/**
 * @version $Id$
 */
public interface App {

    AppView start(AppCallback callback, String token);

    void tokenChanged(String token);

    void stop();
}
