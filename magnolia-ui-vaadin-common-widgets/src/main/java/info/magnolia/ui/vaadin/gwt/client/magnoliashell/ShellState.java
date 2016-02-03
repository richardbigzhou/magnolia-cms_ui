/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton object which helps to track the state of the Magnolia Shell component.
 * The update of the state is on behalf of Shell/Viewport implementation.
 * The main classes tha update the Shell State is {@link info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.MagnoliaShellConnector}
 * and {@link info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector.AppsViewportConnector}.
 */
public class ShellState {

    private static Logger log = Logger.getLogger(ShellState.class.getName());

    private static enum StateType {

        shellAppStarting,

        appStarting,

        appStarted,

        shellAppStarted,

        appClosing,

        shellAppClosing,
    }

    private StateType type;

    public boolean isShellAppStarting() {
        return type == StateType.shellAppStarting;
    }

    private void log(StateType stateType) {
        log.log(Level.WARNING, stateType.toString());
    }

    public void setShellAppStarting() {
        log(StateType.shellAppStarting);
        this.type = StateType.shellAppStarting;
    }

    public boolean isAppStarting() {
        return type == StateType.appStarting;
    }

    public void setAppStarting() {
        log(StateType.appStarting);
        this.type = StateType.appStarting;
    }

    public boolean isAppStarted() {
        return type == StateType.appStarted;
    }

    public void setAppStarted() {
        log(StateType.appStarted);
        this.type = StateType.appStarted;
    }

    public boolean isShellAppStarted() {
        return type == StateType.shellAppStarted;
    }

    public void setShellAppStarted() {
        log(StateType.shellAppStarted);
        this.type = StateType.shellAppStarted;
    }

    public boolean isAppClosing() {
        return type == StateType.appClosing;
    }

    public void setAppClosing() {
        log(StateType.appClosing);
        this.type = StateType.appClosing;
    }

    public boolean isShellAppClosing() {
        return type == StateType.shellAppClosing;
    }

    public void setShellAppClosing() {
        log(StateType.shellAppClosing);
        this.type = StateType.shellAppClosing;
    }

    private static ShellState instance;

    private ShellState() {}

    public static ShellState get() {
        if (instance == null) {
            instance = new ShellState();
        }
        return instance;
    }
}
