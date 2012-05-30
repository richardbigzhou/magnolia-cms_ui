package info.magnolia.ui.widget.actionbar.gwt.client;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;


public interface VActionbarView extends HasWidgets, IsWidget {

    interface Presenter {
    }

    void setPresenter(Presenter presenter);

    boolean hasChildComponent(Widget component);

}
