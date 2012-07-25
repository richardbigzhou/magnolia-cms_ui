/**
 *
 */
package info.magnolia.ui.admincentral.thumbnail.view;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.container.JcrContainer;
import info.magnolia.ui.admincentral.thumbnail.Thumbnail;
import info.magnolia.ui.admincentral.thumbnail.ThumbnailProvider;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import javax.inject.Inject;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Slider;
import com.vaadin.ui.VerticalLayout;

/**
 * ThumbnailViewImpl.
 *
 */
public class ThumbnailViewImpl implements ThumbnailView {

    private CssLayout layout = new CssLayout();
    private VerticalLayout root = new VerticalLayout();
    private Listener listener;

    private Thumbnail selectedAsset = null;
    private Double currentValue = 1d;

    @Inject
    public ThumbnailViewImpl(final WorkbenchDefinition definition, final ThumbnailProvider thumbnailProvider) {

        final Slider slider = new Slider(1,10);
        this.currentValue = (Double)slider.getValue();

        slider.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                Double value = (Double) event.getProperty().getValue();
                boolean increase = currentValue < value;
                for(int i = 0; i < layout.getComponentCount(); i++) {
                    Component asset = layout.getComponent(i);
                    //ok this is just a dummy test
                    if(increase) {
                        asset.setHeight(asset.getHeight() + value.floatValue(), Sizeable.UNITS_PIXELS);
                        asset.setWidth(asset.getWidth() + value.floatValue(), Sizeable.UNITS_PIXELS);
                    } else {
                        asset.setHeight(asset.getHeight() - value.floatValue(), Sizeable.UNITS_PIXELS);
                        asset.setWidth(asset.getWidth() - value.floatValue(), Sizeable.UNITS_PIXELS);
                    }
                }
                currentValue = value;
                root.requestRepaintAll();
            }
        });
        root.addComponent(slider);

        try {
            Node parent = MgnlContext.getJCRSession(definition.getWorkspace()).getNode(definition.getPath());
            Iterable<Node> assets = NodeUtil.getNodes(parent, NodeUtil.EXCLUDE_META_DATA_FILTER);
            for(Node asset: assets) {
                final Thumbnail image = new Thumbnail(asset, thumbnailProvider.getThumbnail(asset, 30, 30));
                image.setType(Embedded.TYPE_IMAGE);
                image.setSizeUndefined();
                image.addListener(new MouseEvents.ClickListener() {

                    @Override
                    public void click(ClickEvent event) {
                        System.out.println("Clicked on " + event.getComponent().getCaption());
                        selectedAsset = (Thumbnail) event.getComponent();
                        if(listener != null) {
                            listener.onItemSelection(selectedAsset.getNode());
                        }
                    }
                });
                layout.addComponent(image);
            }
            layout.setSizeFull();

            root.addComponent(layout);
        } catch (PathNotFoundException e) {
            throw new RuntimeException(e);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;

    }


    @Override
    public void select(String path) {
        // TODO Auto-generated method stub
    }


    @Override
    public void refresh() {

    }


    @Override
    public void refreshItem(Item item) {
        // TODO Auto-generated method stub
    }

    @Override
    public JcrContainer getContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component asVaadinComponent() {
        return root;
    }

}
