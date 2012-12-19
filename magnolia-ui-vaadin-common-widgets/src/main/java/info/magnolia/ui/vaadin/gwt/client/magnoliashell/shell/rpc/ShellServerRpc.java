package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc;

import com.vaadin.shared.communication.ServerRpc;

public interface ShellServerRpc extends ServerRpc {
    
    void removeMessage(String id);
    
    void closeCurrentShellApp();
    
    void closeCurrentApp();
    
    void activateApp(String appId, String subAppId, String parameter);
    
    void startApp(String appId, String subAppId, String parameter);
    
    void activateShellApp(String type, String token);
}
