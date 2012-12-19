package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc;

import com.vaadin.shared.Connector;
import com.vaadin.shared.communication.ClientRpc;

public interface ShellClientRpc extends ClientRpc {
    
    void navigate(String appId, String subAppId, String param);
    
    void activeViewportChanged(Connector viewport);
    
    void showMessage(String type, String topic, String msg, String id);
    
    void hideAllMessages();
}
