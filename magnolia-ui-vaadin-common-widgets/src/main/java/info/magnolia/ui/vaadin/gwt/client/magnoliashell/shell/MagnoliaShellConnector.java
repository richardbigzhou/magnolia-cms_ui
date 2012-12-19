package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.ShellAppLauncher.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellClientRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellServerRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VShellMessage.MessageType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector.ViewportConnector;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.magnoliashell.BaseMagnoliaShell;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.shared.Connector;
import com.vaadin.shared.communication.SharedState;
import com.vaadin.shared.ui.Connect;

@Connect(BaseMagnoliaShell.class)
public class MagnoliaShellConnector extends AbstractLayoutConnector implements MagnoliaShellView.Presenter {

    /**
     * Enumeration of possible viewport types.
     */
    public enum ViewportType {
        SHELL_APP_VIEWPORT("shell:"), APP_VIEWPORT("app:"), DIALOG_VIEWPORT("");

        private String fragmentPrefix;

        private ViewportType(String fragmentPrefix) {
            this.fragmentPrefix = fragmentPrefix;
        }

        public String getFragmentPrefix() {
            return fragmentPrefix;
        }

    }

    private ShellServerRpc rpc = RpcProxy.create(ShellServerRpc.class, this);

    private MagnoliaShellView view;

    private EventBus eventBus = new SimpleEventBus();

    public MagnoliaShellConnector() {
        addStateChangeHandler(new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent event) {
                MagnoliaShellState state = getState();
                Iterator<Entry<ShellAppLauncher.ShellAppType, Integer>> it = getState().indications.entrySet().iterator();
                while (it.hasNext()) {
                    final Entry<ShellAppLauncher.ShellAppType, Integer> entry = it.next();
                    view.setShellAppIndication(entry.getKey(), entry.getValue());
                }
                final Connector active = state.activeViewport;
                if (active != null) {
                    view.setActiveViewport(((ViewportConnector)active).getWidget());    
                }
                
            }
        });

        registerRpc(ShellClientRpc.class, new ShellClientRpc() {

            @Override
            public void navigate(String appId, String subAppId, String param) {
                view.navigate(appId, subAppId, param);
            }

            @Override
            public void activeViewportChanged(Connector viewport) {
                view.setActiveViewport(((ViewportConnector)viewport).getWidget());
            }

            @Override
            public void showMessage(String type, String topic, String msg, String id) {
                view.showMessage(MessageType.valueOf(type), topic, msg, id);
            }

            @Override
            public void hideAllMessages() {
                view.hideAllMessages();
            }
        });
    }

    @Override
    public void updateCaption(ComponentConnector connector) {

    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        List<ComponentConnector> children = getChildComponents();
        
        for (ComponentConnector connector : children) {
            final ViewportConnector vc = (ViewportConnector)connector;
            view.updateViewport(vc.getWidget(), vc.getType());
        }
        
//      }
//      boolean handleCurrentHistory = !proxy.isClientInitialized();
//      proxy.update(this, uidl, client);
//      if (handleCurrentHistory) {
//          History.fireCurrentHistoryState();
//      }
    }

    @Override
    protected Widget createWidget() {
        this.view = new MagnoliaShellViewImpl(eventBus);
        return view.asWidget();
    }

    @Override
    protected SharedState createState() {
        return super.createState();
    }

    @Override
    public MagnoliaShellState getState() {
        return (MagnoliaShellState) super.getState();
    }

    @Override
    public Widget getWidget() {
        return super.getWidget();
    }

    @Override
    public void loadApp(String appId, String subAppId, String parameter) {
        rpc.activateApp(appId, subAppId, parameter);
    }

    @Override
    public void loadShellApp(ShellAppType shellAppType, String token) {
        rpc.activateShellApp(shellAppType.name().toLowerCase(), token);
    }

    @Override
    public void updateViewportLayout(ViewportWidget activeViewport) {
        // client.runDescendentsLayout(viewport);
    }

    @Override
    public void destroyChild(Widget child) {

    }

    @Override
    public void closeCurrentApp() {
        rpc.closeCurrentApp();
    }

    @Override
    public void closeCurrentShellApp() {
        rpc.closeCurrentShellApp();
    }

    @Override
    public void removeMessage(String id) {
        rpc.removeMessage(id);
    }

    @Override
    public boolean isAppRegistered(String appName) {
        return getState().registeredAppNames.contains(appName);
    }

    @Override
    public boolean isAppRunning(String appName) {
        return getState().runningAppNames.contains(appName);
    }

    @Override
    public void startApp(String appId, String subAppId, String parameter) {
        rpc.startApp(appId, subAppId, parameter);
    }

    @Override
    public void handleHistoryChange(String fragment) {
        /*
         * if (!proxy.isClientInitialized()) { return; } final FragmentDTO dto =
         * FragmentDTO.fromFragment(fragment); if (dto.getAppType() ==
         * FragmentType.SHELL_APP) { eventBus.fireEvent(new
         * ShellAppNavigationEvent(ShellAppType.resolveType(dto.getAppId()),
         * dto.getParameter())); } else { final String appId = dto.getAppId();
         * final String subAppId = dto.getSubAppId(); final String parameter =
         * dto.getParameter(); if (isAppRegistered(appId)) { if
         * (!isAppRunning(appId)) { view.showAppPreloader(appId, new
         * PreloaderCallback() {
         * 
         * @Override public void onPreloaderShown(String appName) {
         * startApp(appName, subAppId, parameter); } }); } else { loadApp(appId,
         * "", parameter); } } else { eventBus.fireEvent(new
         * ShellAppNavigationEvent(ShellAppType.APPLAUNCHER,
         * dto.getParameter())); } }
         */
    }

}
