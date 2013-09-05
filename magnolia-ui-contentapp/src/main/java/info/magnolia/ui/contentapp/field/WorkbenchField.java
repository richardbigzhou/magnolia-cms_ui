package info.magnolia.ui.contentapp.field;

import com.vaadin.data.Item;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.SearchEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/2/13
 * Time: 1:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class WorkbenchField extends CustomField<Object> {

    private static final String DEFAULT_HEIGHT = "500px";

    private Logger log = LoggerFactory.getLogger(getClass());

    private ImageProviderDefinition imageProvider;

    private WorkbenchDefinition workbenchDefinition;

    private WorkbenchPresenter presenter;

    private WorkbenchView view;

    private EventBus workbenchEventbus = new SimpleEventBus();

    public WorkbenchField(WorkbenchDefinition definition, ImageProviderDefinition imageProvider, WorkbenchPresenter presenter) {
        this.workbenchDefinition = definition;
        this.imageProvider = imageProvider;
        this.presenter = presenter;
    }

    @Override
    protected Component initContent() {
        this.view = presenter.start(workbenchDefinition, imageProvider, workbenchEventbus);
        this.view.setViewType(TreePresenterDefinition.VIEW_TYPE);
        this.view.asVaadinComponent().setHeight(DEFAULT_HEIGHT);
        if (getConvertedValue() instanceof JcrItemAdapter) {
            presenter.select(Arrays.asList(((JcrItemAdapter) getConvertedValue()).getItemId()));
        }

        workbenchEventbus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                setValue(event.getFirstItem(), false);
            }
        });

        workbenchEventbus.addHandler(SearchEvent.class, new SearchEvent.Handler() {
            @Override
            public void onSearch(SearchEvent event) {
                presenter.doSearch(event.getSearchExpression());
            }
        });

        return this.view.asVaadinComponent();
    }

    @Override
    public void setPropertyDataSource(com.vaadin.data.Property newDataSource) {
        super.setPropertyDataSource(null);
        setConverter(new ItemStringConverter());
    }

    @Override
    public Class<? extends Item> getType() {
        return Item.class;
    }

    private class ItemStringConverter implements Converter<Object, Item> {
        @Override
        public Item convertToModel(Object value, Class<? extends Item> targetType, Locale locale) throws ConversionException {
            if (value instanceof  Item) {
                return (Item)value;
            }
            if (StringUtils.isBlank((String)value)) {
                return null;
            }
            String potentialPath = String.valueOf(value);
            try {
                String itemId = JcrItemUtil.getItemId(workbenchDefinition.getWorkspace(), potentialPath);
                if (!StringUtils.isBlank(itemId) || !JcrItemUtil.itemExists(workbenchDefinition.getWorkspace(), potentialPath)) {
                    potentialPath = itemId;
                }
                javax.jcr.Item jcrItem = JcrItemUtil.getJcrItem(workbenchDefinition.getWorkspace(), potentialPath);
                return jcrItem.isNode() ?
                    new JcrNodeAdapter((Node) jcrItem):
                    new JcrPropertyAdapter((Property) jcrItem);
            } catch (RepositoryException e) {
                log.warn("Unable to set the selected item", potentialPath, e);
            }

            return null;
        }

        @Override
        public Object convertToPresentation(Item value, Class<?> targetType, Locale locale) throws ConversionException {
            return value;
        }

        @Override
        public Class<Item> getModelType() {
            return Item.class;
        }

        @Override
        public Class<Object> getPresentationType() {
            return Object.class;

        }
    }
}
