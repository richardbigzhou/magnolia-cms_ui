/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.workbench.beanworkbench;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.workbench.WorkbenchPresenterBase;
import info.magnolia.ui.workbench.WorkbenchStatusBarPresenter;
import info.magnolia.ui.workbench.WorkbenchView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.FilesystemContainer;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 04/02/14
 * Time: 22:36
 * To change this template use File | Settings | File Templates.
 */
public class BeanWorkbenchPresenter extends WorkbenchPresenterBase<Object> {

    private FilesystemContainer container = new FilesystemContainer(new File("/Users/sasha/test"));

    @Inject
    public BeanWorkbenchPresenter(WorkbenchView view, ComponentProvider componentProvider, WorkbenchStatusBarPresenter statusBarPresenter) {
        super(view, componentProvider, statusBarPresenter);
    }

    @Override
    public Object resolveWorkbenchRoot() {
        return new File("/Users/sasha/test");
    }

    @Override
    public Item getItemFor(Object itemId) {
        return container.getItem(itemId);
    }

    @Override
    protected List<Object> filterExistingItems(List<Object> itemIds) {
        List<Object> result = new ArrayList<Object>();
        for (Object id : itemIds) {
            if (container.containsId(id)) {
                result.add(id);
            }
        }
        return result;
    }

    @Override
    protected Container getContainer() {
        return container;
    }
}
