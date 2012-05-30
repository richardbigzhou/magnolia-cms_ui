package info.magnolia.ui.widget.actionbar.gwt.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;


public class VActionbarViewImpl extends FlowPanel implements VActionbarView {

    public static final String CLASSNAME = "v-actionbar";

    private Element root = DOM.createElement("section");

    private Presenter presenter;

    public VActionbarViewImpl() {
        this.root = getElement();
        // setElement(root);
        setStyleName(CLASSNAME);

        Element title = DOM.createElement("h3");
        title.setClassName("section-title");
        title.setInnerText("Actions");
        getElement().appendChild(title);

    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return getChildren().contains(component);
    }

}
