package info.magnolia.ui.contentapp.movedialog.action;

import com.vaadin.data.Item;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.movedialog.MoveActionCallback;
import info.magnolia.ui.contentapp.movedialog.MoveDialogPresenter;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.framework.action.MoveLocation;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 6:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class OpenMoveDialogAction extends AbstractAction<OpenMoveDialogActionDefinition>  {

    private AppContext appContext;

    private ComponentProvider componentProvider;

    private JcrNodeAdapter sourceNodeAdapter;

    private List<JcrNodeAdapter> sourceNodeAdapters;

    private MoveDialogPresenter moveDialogPresenter;
    private OverlayCloser closeHandle;

    public OpenMoveDialogAction(
            OpenMoveDialogActionDefinition definition,
            AppContext appContext,
            ComponentProvider componentProvider,
            JcrNodeAdapter sourceNodeAdapter,
            MoveDialogPresenter moveDialogPresenter) {
        this(definition, appContext, componentProvider, moveDialogPresenter, Arrays.asList(sourceNodeAdapter));
    }

    public OpenMoveDialogAction(
            OpenMoveDialogActionDefinition definition,
            AppContext appContext,
            ComponentProvider componentProvider,
            MoveDialogPresenter moveDialogPresenter,
            List<JcrNodeAdapter> sourceNodeAdapters) {
        super(definition);
        this.appContext = appContext;
        this.componentProvider = componentProvider;
        this.sourceNodeAdapters = sourceNodeAdapters;
        this.moveDialogPresenter = moveDialogPresenter;
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (appContext.getActiveSubAppContext().getSubAppDescriptor() instanceof BrowserSubAppDescriptor) {
            BrowserSubAppDescriptor descriptor = (BrowserSubAppDescriptor) appContext.getActiveSubAppContext().getSubAppDescriptor();
            DialogView moveDialog = moveDialogPresenter.start(descriptor, sourceNodeAdapters, new MoveActionCallback() {
                @Override
                public void onMoveCancelled() {
                    closeHandle.close();
                }

                @Override
                public void onMovePerformed(Item newHost, MoveLocation moveLocation) {
                    closeHandle.close();
                }
            });

            this.closeHandle = appContext.openOverlay(moveDialog);
        }
    }
}
