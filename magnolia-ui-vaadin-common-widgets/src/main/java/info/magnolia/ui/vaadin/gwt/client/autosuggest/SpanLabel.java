/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.autosuggest;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.user.client.ui.LabelBase;

/**
 * SpanLabel.
 */
public class SpanLabel extends LabelBase<String> {

    private static Set<SpanLabel> availableCache = new HashSet<SpanLabel>();

    public static SpanLabel getAvailableInstance() {
        Iterator<SpanLabel> iter = availableCache.iterator();
        if (iter.hasNext()) {
            SpanLabel label = iter.next();
            label.setText(null);
            iter.remove();
            return label;
        }
        return new SpanLabel();
    }

    private SpanLabel() {
        super(true);
    }

    public String getText() {
        return getElement().getInnerText();
    }

    public void setText(String text) {
        this.getElement().setInnerText(text);
    }

    public void setText(String text, String matchStr, boolean startsWith) {
        String boldStr = SuggestionUtil.boldMatchStr(text, matchStr, startsWith);
        if (boldStr != null) {
            this.getElement().setInnerHTML(boldStr);
        } else {
            setText(text);
        }
    }

    public String getHtml() {
        return getElement().getInnerHTML();
    }

    public void setHtml(String html) {
        getElement().setInnerHTML(html);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        availableCache.remove(this);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        availableCache.add(this);
    }
}
