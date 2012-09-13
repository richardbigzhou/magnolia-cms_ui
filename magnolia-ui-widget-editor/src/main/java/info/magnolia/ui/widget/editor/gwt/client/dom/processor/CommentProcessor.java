/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.widget.editor.gwt.client.dom.processor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
import info.magnolia.ui.widget.editor.gwt.client.dom.CMSComment;
import info.magnolia.ui.widget.editor.gwt.client.dom.MgnlElement;
import info.magnolia.ui.widget.editor.gwt.client.model.Model;

/**
 * Processor for comment elements.
 */
public class CommentProcessor {

    private static final String CMS_PAGE = "cms:page";
    private static final String CMS_AREA = "cms:area";
    private static final String CMS_COMPONENT = "cms:component";

    public static MgnlElement process(Model model, Node node, MgnlElement currentElement) throws IllegalArgumentException {

        CMSComment comment = CMSComment.as(node);
        GWT.log("processing comment " + comment);

        // in case we fail, we want to keep the currentElement as current.
        MgnlElement mgnlElement = currentElement;

        if (!comment.isClosing()) {

            try {
                mgnlElement = createMgnlElement(comment, currentElement);

                if (mgnlElement.getParent() == null) {
                    model.setRootPage(mgnlElement);
                }
                else if (mgnlElement.getParent().isPage()) {
                    model.addRootArea(mgnlElement);
                    mgnlElement.getParent().getChildren().add(mgnlElement);
                }
                else {
                    mgnlElement.getParent().getChildren().add(mgnlElement);
                }

            } catch (IllegalArgumentException e) {
                GWT.log("Not MgnlElement, skipping: " + e.toString());
            }


        }
        // the cms:page tag should span throughout the page, but doesn't: kind of a hack.
        else if (currentElement != null && !currentElement.isPage()) {
            currentElement.setEndComment(comment);
            mgnlElement = currentElement.getParent();
        }

        return mgnlElement;

    }

    private static MgnlElement createMgnlElement(CMSComment comment, MgnlElement parent) throws IllegalArgumentException {
        String tagName = comment.getTagName();
        MgnlElement mgnlElement;
        if (CMS_PAGE.equals(tagName)) {
            mgnlElement = new MgnlElement(comment, parent);
            mgnlElement.setPage(true);
        }
        else if (CMS_AREA.equals(tagName)) {
            mgnlElement = new MgnlElement(comment, parent);
            mgnlElement.setArea(true);

        }
        else if (CMS_COMPONENT.equals(tagName)) {
            mgnlElement = new MgnlElement(comment, parent);
            mgnlElement.setComponent(true);
        }
        else {
            throw new IllegalArgumentException("The tagname must be one of the defined marker Strings.");
        }
        return mgnlElement;



    }
}
