/**
 * This file Copyright (c) 2010-2015 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.contentapp.movedialog;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.ResettableEventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.overlay.OverlayLayer.ModalityLevel;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.field.WorkbenchField;
import info.magnolia.ui.contentapp.movedialog.action.MoveCancelledAction;
import info.magnolia.ui.contentapp.movedialog.action.MoveNodeActionDefinition;
import info.magnolia.ui.contentapp.movedialog.predicate.MoveAfterPossibilityPredicate;
import info.magnolia.ui.contentapp.movedialog.predicate.MoveBeforePossibilityPredicate;
import info.magnolia.ui.contentapp.movedialog.predicate.MoveInsidePossibilityPredicate;
import info.magnolia.ui.contentapp.movedialog.predicate.MovePossibilityPredicate;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.dialog.DialogCloseHandler;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.actionarea.DialogActionExecutor;
import info.magnolia.ui.dialog.actionarea.definition.ConfiguredEditorActionAreaDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.definition.SecondaryActionDefinition;
import info.magnolia.ui.framework.overlay.ViewAdapter;
import info.magnolia.ui.imageprovider.definition.ConfiguredImageProviderDefinition;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.integration.NullItem;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.tree.MoveHandler;
import info.magnolia.ui.workbench.tree.MoveLocation;
import info.magnolia.ui.workbench.tree.TreePresenter;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import com.rits.cloning.Cloner;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;

/**
 * Implementation of {@link MoveDialogPresenter}.
 */
public class MoveDialogPresenterImpl extends BaseDialogPresenter implements MoveDialogPresenter {

    private DialogView dialogView;

    private EventBus eventBus = new ResettableEventBus(new SimpleEventBus());

    private WorkbenchPresenter workbenchPresenter;

    private AppContext appContext;

    private List<Item> nodesToMove;

    private Map<MoveLocation, ActionDefinition> actionMap = new HashMap<MoveLocation, ActionDefinition>();

    private Map<MoveLocation, MovePossibilityPredicate> possibilityPredicates = new HashMap<MoveLocation, MovePossibilityPredicate>();

    private DropConstraint constraint;

    private MoveActionCallback callback;

    private Item currentHostCandidate;

    private ConfiguredWorkbenchDefinition workbenchDefinition;

    private I18nizer i18nizer;

    private ContentConnector contentConnector;

    private MoveHandler dropHandler;

    @Inject
    public MoveDialogPresenterImpl(ComponentProvider componentProvider, DialogView dialogView, WorkbenchPresenter workbenchPresenter, DialogActionExecutor executor, AppContext appContext, I18nizer i18nizer, SimpleTranslator simpleTranslator, ContentConnector contentConnector) {
        super(componentProvider, executor, dialogView, i18nizer, simpleTranslator);
        this.dialogView = dialogView;
        this.workbenchPresenter = workbenchPresenter;
        this.appContext = appContext;
        this.i18nizer = i18nizer;
        this.contentConnector = contentConnector;
        this.dropHandler = componentProvider.newInstance(MoveHandler.class);
    }

    @Override
    public Object[] getActionParameters(String actionName) {
        return new Object[] { nodesToMove, callback, appContext, getHostCandidate(), dropHandler };
    }

    @Override
    public DialogView start(BrowserSubAppDescriptor subAppDescriptor, List<Item> nodesToMove, MoveActionCallback callback) {

        final ConfiguredImageProviderDefinition imageProviderDefinition = prepareImageProviderDefinition(subAppDescriptor);
        this.workbenchDefinition = prepareWorkbenchDefinition(subAppDescriptor);

        this.nodesToMove = nodesToMove;
        this.constraint = componentProvider.newInstance(workbenchDefinition.getDropConstraintClass());
        this.callback = callback;

        initActions();
        initMovePossibilityPredicates();

        final WorkbenchField field = new WorkbenchField(
                workbenchDefinition,
                imageProviderDefinition,
                workbenchPresenter,
                eventBus);
        ViewAdapter viewAdapter = new ViewAdapter(field);
        viewAdapter.asVaadinComponent().addStyleName("choose-dialog");
        dialogView.setContent(viewAdapter);
        field.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                currentHostCandidate = contentConnector.getItem(event.getProperty().getValue());
                updatePossibleMoveLocations(currentHostCandidate);

            }
        });

        DialogDefinition dialogDefinition = prepareDialogDefinition();
        getExecutor().setDialogDefinition(dialogDefinition);
        dialogView.setCaption(dialogDefinition.getLabel());
        dialogView.addDialogCloseHandler(new DialogCloseHandler() {
            @Override
            public void onDialogClose(DialogView dialogView) {
                ((ResettableEventBus) eventBus).reset();
            }
        });
        super.start(dialogDefinition, appContext);
        updatePossibleMoveLocations(getHostCandidate());

        getView().getActionAreaView().getViewForAction(MoveLocation.INSIDE.name()).asVaadinComponent().addStyleName("commit");
        getView().setClosable(true);
        return dialogView;
    }

    private ConfiguredImageProviderDefinition prepareImageProviderDefinition(BrowserSubAppDescriptor subAppDescriptor) {
        return (ConfiguredImageProviderDefinition) new Cloner().deepClone(subAppDescriptor.getImageProvider());
    }

    private ConfiguredWorkbenchDefinition prepareWorkbenchDefinition(BrowserSubAppDescriptor subAppDescriptor) {
        Cloner cloner = new Cloner();
        final ConfiguredWorkbenchDefinition workbenchDefinition =
                (ConfiguredWorkbenchDefinition) cloner.deepClone(subAppDescriptor.getWorkbench());

        workbenchDefinition.setDialogWorkbench(true);
        workbenchDefinition.setEditable(false);

        List<ContentPresenterDefinition> contentPresenters = workbenchDefinition.getContentViews();
        Iterator<ContentPresenterDefinition> it = contentPresenters.iterator();
        ContentPresenterDefinition treePresenterDef = null;
        while (it.hasNext() && treePresenterDef == null) {
            ContentPresenterDefinition contentPresenterDef = it.next();
            if (TreePresenter.class.isAssignableFrom(contentPresenterDef.getImplementationClass())) {
                treePresenterDef = prepareTreePresenter(contentPresenterDef);
            }
        }

        if (treePresenterDef != null) {
            contentPresenters.clear();
            contentPresenters.add(treePresenterDef);
        }

        return workbenchDefinition;
    }

    private ContentPresenterDefinition prepareTreePresenter(ContentPresenterDefinition treePresenter) {
        Cloner cloner = new Cloner();
        ContentPresenterDefinition def = cloner.deepClone(treePresenter);
        if (!def.getColumns().isEmpty()) {
            ColumnDefinition column = def.getColumns().get(0);
            def.getColumns().clear();
            def.getColumns().add(column);
        }
        return def;
    }

    protected void initMovePossibilityPredicates() {
        possibilityPredicates.put(MoveLocation.AFTER, new MoveAfterPossibilityPredicate(constraint, nodesToMove));
        possibilityPredicates.put(MoveLocation.BEFORE, new MoveBeforePossibilityPredicate(constraint, nodesToMove));
        possibilityPredicates.put(MoveLocation.INSIDE, new MoveInsidePossibilityPredicate(constraint, nodesToMove));
    }

    protected void updatePossibleMoveLocations(Item possibleHost) {
        Set<MoveLocation> possibleLocations = new HashSet<MoveLocation>();
        if (possibleHost == null) {
            possibleHost = new NullItem();
        }

        Iterator<Entry<MoveLocation, MovePossibilityPredicate>> it = possibilityPredicates.entrySet().iterator();
        while (it.hasNext()) {
            Entry<MoveLocation, MovePossibilityPredicate> entry = it.next();
            if (entry.getValue().isMovePossible(possibleHost)) {
                possibleLocations.add(entry.getKey());
            }
        }

        getActionArea().setPossibleMoveLocations(possibleLocations);
    }

    private void initActions() {
        for (MoveLocation location : MoveLocation.values()) {
            ConfiguredActionDefinition definition = i18nizer.decorate(new MoveNodeActionDefinition(location));
            definition.setName(location.name());
            actionMap.put(location, definition);
        }
    }

    private DialogDefinition prepareDialogDefinition() {
        ConfiguredDialogDefinition def = new ConfiguredDialogDefinition();
        def = i18nizer.decorate(def);
        def.setId("ui-contentapp:code:MoveDialogPresenterImpl.moveDialog");

        for (MoveLocation location : MoveLocation.values()) {
            def.addAction(actionMap.get(location));
        }

        ConfiguredActionDefinition cancelDef = new ConfiguredActionDefinition();
        cancelDef.setName("cancel");
        cancelDef.setImplementationClass(MoveCancelledAction.class);
        def.addAction(cancelDef);

        ConfiguredEditorActionAreaDefinition actionAreaDefinition = new ConfiguredEditorActionAreaDefinition();
        actionAreaDefinition.setPresenterClass(MoveDialogActionAreaPresenter.class);

        List<SecondaryActionDefinition> secondaryActions = new LinkedList<SecondaryActionDefinition>();
        secondaryActions.add(new SecondaryActionDefinition(MoveLocation.AFTER.name()));
        secondaryActions.add(new SecondaryActionDefinition(MoveLocation.BEFORE.name()));
        actionAreaDefinition.setSecondaryActions(secondaryActions);

        def.setActionArea(actionAreaDefinition);
        def.setModalityLevel(ModalityLevel.LIGHT);
        return def;
    }

    @Override
    public MoveDialogActionAreaPresenter getActionArea() {
        return (MoveDialogActionAreaPresenter) super.getActionArea();
    }

    @Override
    protected DialogActionExecutor getExecutor() {
        return (DialogActionExecutor) super.getExecutor();
    }

    @Override
    protected void executeAction(String actionName, Object[] actionContextParams) {
        actionName = getCorrectActionName(actionName);
        if (actionName != null) {
            super.executeAction(actionName, actionContextParams);
        }
    }

    public String getCorrectActionName(String actionName) {
        if (actionName.equals(BaseDialog.COMMIT_ACTION_NAME)) {
            boolean moveInsideActionEnabled = getView().getActionAreaView().getViewForAction(MoveLocation.INSIDE.name()).asVaadinComponent().isEnabled();
            actionName = moveInsideActionEnabled ? MoveLocation.INSIDE.name() : null;
        }
        return actionName;
    }

    private Item getHostCandidate() {
        if (currentHostCandidate != null) {
            return currentHostCandidate;
        } else {
            try {
                String workspace = ((JcrContentConnector) contentConnector).getContentConnectorDefinition().getWorkspace();
                return new JcrNodeAdapter(MgnlContext.getJCRSession(workspace).getRootNode());
            } catch (RepositoryException e) {
                return null;
            }
        }
    }
}
