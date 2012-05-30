package info.magnolia.ui.widget.actionbar.gwt.client;

import java.util.Iterator;
import java.util.Set;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;


/**
 * Vaadin implementation of Action bar client side (Presenter).
 */
public class VActionbar extends Composite implements HasWidgets, Container, VActionbarView.Presenter {

    protected String paintableId;

    protected ApplicationConnection client;

    private final VActionbarView view;

    public VActionbar() {
        view = new VActionbarViewImpl();
        view.setPresenter(this);
        initWidget(view.asWidget());
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        updateSections(uidl);
    }

    private void updateSections(UIDL uidl) {
    }

    private void updateGroups(UIDL uidl) {
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (hasChildComponent(child)) {
            return new RenderSpace(getOffsetWidth(), getOffsetHeight());
        }
        return new RenderSpace();
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return view.hasChildComponent(component);
    }

    @Override
    public void add(Widget w) {
        view.add(w);
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return view.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return view.remove(w);
    }

}
