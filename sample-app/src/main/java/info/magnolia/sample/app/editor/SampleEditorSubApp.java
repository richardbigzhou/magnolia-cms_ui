/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.sample.app.editor;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.sample.app.editor.location.EditorLocation;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.app.BaseSubApp;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * SubApp for editor tabs in sample app.
 */
public class SampleEditorSubApp extends BaseSubApp implements SampleEditorView.Listener {

    private final SampleEditorView view;
    private final SimpleTranslator i18n;

    @Inject
    public SampleEditorSubApp(final SubAppContext subAppContext, final SampleEditorView view, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus, SimpleTranslator i18n) {
        super(subAppContext, view);
        this.view = view;
        this.i18n = i18n;
    }

    @Override
    public String getCaption() {
        return i18n.translate("sample.app.editor.label") + " " + view.getName();
    }

    @Override
    public View start(Location location) {
        super.start(location);
        this.view.setName(location.getParameter());
        this.view.setListener(this);
        return view;
    }

    /**
     * Overwrite supportsLocation to implement custom handling of subApp opening.
     * Will take care of the location change in case the current view name equals the new view name.
     *
     * @param location the new location
     * @return true if current SubApp should handle the location update
     */
    @Override
    public boolean supportsLocation(Location location) {
        EditorLocation newLocation = EditorLocation.wrap(location);
        return view.getName().equals(newLocation.getViewName());
    }
}
