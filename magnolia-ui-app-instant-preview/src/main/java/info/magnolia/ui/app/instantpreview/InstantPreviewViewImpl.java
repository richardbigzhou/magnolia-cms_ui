/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.instantpreview;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * View implementation for the InstantPreview app.
 */
@SuppressWarnings("serial")
public class InstantPreviewViewImpl implements InstantPreviewView {

    private static final Logger log = LoggerFactory.getLogger(InstantPreviewViewImpl.class);

    private Listener listener;
    private final VerticalLayout layout = new VerticalLayout();
    private String hostId;
    private Button shareButton;
    private Button joinButton;
    private TextField inputCode;
    private Button hostIdLink;
    /**
     * InstantPreviewActionType.
     */
    public enum InstantPreviewActionType {
        SHARE, UNSHARE, JOIN, LEAVE
    }

    public InstantPreviewViewImpl() {
        layout.setSpacing(true);
        layout.setMargin(true);

        shareButton = constructShareButton();
        shareButton.focus();

        joinButton = constructJoinButton();

        inputCode = buildInputCode();

        layout.addComponent(shareButton);
        hostIdLink = new Button(hostId != null ? hostId: "");
        hostIdLink.setImmediate(true);
        hostIdLink.setStyleName(BaseTheme.BUTTON_LINK);

        layout.addComponent(hostIdLink);
        layout.addComponent(joinButton);
        layout.addComponent(inputCode);

    }

    protected TextField buildInputCode() {
        final TextField inputCode = new TextField();
        inputCode.setInputPrompt("Enter host id");
        inputCode.setMaxLength(11);
        inputCode.setImmediate(true);
        inputCode.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                hostId = event.getProperty().toString();
            }

        });
        //TODO add validator
        return inputCode;
    }

    protected Button constructJoinButton() {
        final Button joinButton = new Button("Join");
        joinButton.setData(InstantPreviewActionType.JOIN);
        joinButton.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    if(joinButton.isEnabled()) {
                        if(joinButton.getData() == InstantPreviewActionType.JOIN) {
                            if(StringUtils.isNotBlank(hostId)) {
                                //join session
                                hostId = (String) inputCode.getValue();
                                listener.joinSession(hostId);
                                hostIdLink.setVisible(false);
                                joinButton.setCaption("Leave");
                                joinButton.setData(InstantPreviewActionType.LEAVE);
                                getShareButton().setEnabled(false);
                                getInputCode().setEnabled(false);
                            } else {
                                log.error("Host id cannot be empty or null");
                                listener.showError("Host id cannot be empty or null");
                            }
                        } else if(joinButton.getData()==InstantPreviewActionType.LEAVE) {
                            listener.leaveSession(hostId);
                            hostIdLink.setVisible(true);
                            joinButton.setCaption("Join");
                            joinButton.setData(InstantPreviewActionType.JOIN);
                            getShareButton().setEnabled(true);
                            getInputCode().setEnabled(true);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.error("", e);
                    listener.showError(e.getMessage() + "\nIs the code correct? It is also possible that the host stopped sharing the session.");
                }
            }
        });
        return joinButton;
    }

    protected Button constructShareButton() {
        final Button shareButton = new Button("Share");
        shareButton.setData(InstantPreviewActionType.SHARE);
        shareButton.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if(shareButton.isEnabled()) {
                    if(shareButton.getData()==InstantPreviewActionType.SHARE) {
                        //generate code and start session
                        hostId = listener.shareSession();
                        hostIdLink.setCaption(hostId);
                        hostIdLink.setVisible(true);
                        shareButton.setCaption("Unshare");
                        shareButton.setData(InstantPreviewActionType.UNSHARE);
                        getJoinButton().setEnabled(false);
                        getInputCode().setEnabled(false);
                    } else if(shareButton.getData()==InstantPreviewActionType.UNSHARE) {
                        listener.unshareSession(hostId);
                        hostId = null;
                        hostIdLink.setCaption("");
                        hostIdLink.setVisible(false);
                        shareButton.setCaption("Share");
                        shareButton.setData(InstantPreviewActionType.SHARE);
                        getJoinButton().setEnabled(true);
                        getInputCode().setEnabled(true);
                    }
                }
            }
        });
        return shareButton;
    }

    public Button getShareButton() {
        return shareButton;
    }

    public Button getJoinButton() {
        return joinButton;
    }

    public TextField getInputCode() {
        return inputCode;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }
}
