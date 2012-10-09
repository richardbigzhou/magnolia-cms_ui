package info.magnolia.ui.app.showcase.main;

import com.google.inject.Inject;

import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.View;

public class FormsPresenter implements FormsView.Listener {

    private FormsView view;
    private MagnoliaShell shell;

    @Inject
    public FormsPresenter(FormsView formsView, Shell shell) {
        this.view = formsView;
        this.shell = (MagnoliaShell) shell;
    }

    public View start() {
        view.setListener(this);
        return view;
    }

    @Override
    public void onViewInDialog() {

        shell.openDialog(view.asBaseDialog());
    }

    @Override
    public void onCloseDialog() {
        shell.removeDialog(view.asBaseDialog());
    }
}
