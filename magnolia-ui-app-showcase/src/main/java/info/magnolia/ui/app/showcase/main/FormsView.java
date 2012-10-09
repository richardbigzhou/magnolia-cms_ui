package info.magnolia.ui.app.showcase.main;

import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.widget.dialog.BaseDialog;

public interface FormsView extends View {
    public interface Listener {

        void onViewInDialog();

        void onCloseDialog();

    }

    void setListener(Listener listener);

    BaseDialog asBaseDialog();

}
