/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.editor.dom.processor;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.Comment;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlArea;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlPage;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;
import info.magnolia.ui.vaadin.gwt.client.shared.ErrorType;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Processor for comment elements. This processor builds a {@link info.magnolia.ui.vaadin.gwt.client.editor.dom.CmsNode}-tree
 * based on {@link Comment} elements in the DOM structure. <br />
 * The nesting of elements is given by the opening and closing of the {@link Comment} tags wrapped by {@link CMSComment}s.
 * In case it processes a starting comment-tag a new {@link MgnlElement} is created and returned.
 * In case of a closing comment-tag it will return the parent.
 */
public class CommentProcessor {

    public MgnlElement process(Model model, EventBus eventBus, Node node, MgnlElement currentElement) throws ProcessException {

        CMSComment comment = getCmsComment(node);

        // in case we fail, we want to keep the currentElement as current.
        MgnlElement mgnlElement = currentElement;

        if (!comment.isClosing()) {
            // just validate the open comment of area and component
            validateAreaAndComponentComments(node, comment);

            mgnlElement = createMgnlElement(comment, currentElement, eventBus);
            mgnlElement.setStartComment((Element) node.cast());

            // validate and set page element
            if (mgnlElement.getParent() == null) {
                if (!(mgnlElement instanceof MgnlPage)) {
                    throw new ProcessException(ErrorType.EXPECTED_PAGE_TAG, comment.getTagName());
                }
                model.setRootPage((MgnlPage) mgnlElement);

            } else {
                // validate and add area and component elements
                if (mgnlElement.getParent().asMgnlElement().isPage()) {
                    if (!(mgnlElement instanceof MgnlArea)) {
                        throw new ProcessException(ErrorType.EXPECTED_AREA_TAG, comment.getTagName());
                    }
                    model.addRootArea((MgnlArea) mgnlElement);
                }
                mgnlElement.getParent().getChildren().add(mgnlElement);
            }
        }
        // the cms:page tag should span throughout the page, but doesn't: kind of a hack.
        else if (!comment.getTagName().equals(Model.CMS_PAGE)) {
            if (currentElement != null && !currentElement.isPage()) {
                currentElement.setEndComment((Element) node.cast());
                mgnlElement = currentElement.getParent().asMgnlElement();
            } else {
                throw new ProcessException(ErrorType.UNMATCHED_CLOSING_TAG, comment.getTagName());
            }
        }

        return mgnlElement;

    }

    private CMSComment getCmsComment(Node node) throws IllegalArgumentException {

        CMSComment cmsComment = new CMSComment();

        Comment domComment = node.cast();
        String comment = domComment.getData().trim();

        GWT.log("processing comment " + comment);

        String tagName = "";
        boolean isClosing = false;

        int delimiter = comment.indexOf(" ");
        String attributeString = "";

        if (delimiter < 0) {
            tagName = comment;
        } else {
            tagName = comment.substring(0, delimiter);
            attributeString = comment.substring(delimiter + 1);
        }

        if (tagName.startsWith("/")) {
            isClosing = true;
            tagName = tagName.substring(1);
        }

        if (tagName.startsWith(Model.CMS_TAG)) {
            cmsComment.setTagName(tagName);
            cmsComment.setAttribute(attributeString);
            cmsComment.setClosing(isClosing);

        } else {
            throw new IllegalArgumentException("Tagname must start with +'" + Model.CMS_TAG + "'.");
        }
        return cmsComment;
    }

    /**
     * Check if the given <code>cms.area, cms.component</code> is in <code>head/body</code> tag or not.
     */
    private void validateAreaAndComponentComments(Node node, CMSComment comment) throws ProcessException {
        String tagName = comment.getTagName();
        boolean isAreaOrComponentTag = tagName.startsWith(Model.CMS_AREA) || tagName.startsWith(Model.CMS_COMPONENT);
        BodyElement body = BodyElement.as(node.getOwnerDocument().getElementsByTagName("body").getItem(0));
        HeadElement head = HeadElement.as(node.getOwnerDocument().getElementsByTagName("head").getItem(0));
        if (isAreaOrComponentTag && !(head.isOrHasChild(node) || body.isOrHasChild(node))) {
            throw new ProcessException(ErrorType.TAG_OUTSIDE_DOCUMENT, tagName);
        }
    }

    /**
     * Creates an attributes map by parsing the comment string for all relevant data.
     * Overrides or adds attributes defined in {@link Model#INHERITED_ATTRIBUTES} from the parent element.
     * @param attributeString a string containing all data associated with a {@link CMSComment}
     * @param parent the parent element
     */
    private Map<String, String> getAttributes(String attributeString, MgnlElement parent) {
        String[] keyValue;
        Map<String, String> attributes = new HashMap<String, String>();

        RegExp regExp = RegExp.compile("(\\S+=[\"'][^\"]*[\"'])", "g");
        for (MatchResult matcher = regExp.exec(attributeString); matcher != null; matcher = regExp.exec(attributeString)) {
            keyValue = matcher.getGroup(0).split("=");
            if (keyValue[0].equals("content")) {
                String content = keyValue[1].replace("\"", "");
                int i = content.indexOf(':');
                attributes.put("workspace", content.substring(0, i));
                attributes.put("path", content.substring(i + 1));
            } else {
                attributes.put(keyValue[0], keyValue[1].replace("\"", ""));
            }
        }
        if (parent != null) {
            for (String inheritedAttribute : Model.INHERITED_ATTRIBUTES) {
                if (parent.containsAttribute(inheritedAttribute)) {
                    attributes.put(inheritedAttribute, parent.getAttribute(inheritedAttribute));
                }
            }
        }
        return attributes;
    }

    private MgnlElement createMgnlElement(CMSComment comment, MgnlElement parent, EventBus eventBus) throws IllegalArgumentException {
        String tagName = comment.getTagName();
        MgnlElement mgnlElement;
        if (Model.CMS_PAGE.equals(tagName)) {
            mgnlElement = new MgnlPage(parent);
        } else if (Model.CMS_AREA.equals(tagName)) {
            mgnlElement = new MgnlArea(parent, eventBus);
        } else if (Model.CMS_COMPONENT.equals(tagName)) {
            mgnlElement = new MgnlComponent(parent, eventBus);
        } else {
            throw new IllegalArgumentException("The tagname must be one of the defined marker Strings.");
        }

        mgnlElement.setAttributes(getAttributes(comment.getAttributes(), parent));

        return mgnlElement;
    }

    /**
     * Wrapper element for {@link Comment}s.
     */
    private class CMSComment {

        private String tagName;
        private String attributes;
        private boolean isClosing = false;

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public boolean isClosing() {
            return isClosing;
        }

        public void setClosing(boolean isClosing) {
            this.isClosing = isClosing;
        }

        public void setAttribute(String attributes) {
            this.attributes = attributes;
        }

        public String getAttributes() {
            return attributes;
        }
    }
}
