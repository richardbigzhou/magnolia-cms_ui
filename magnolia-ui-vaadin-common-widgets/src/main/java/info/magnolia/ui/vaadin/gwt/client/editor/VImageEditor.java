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
package info.magnolia.ui.vaadin.gwt.client.editor;

import org.vaadin.csstools.client.ComputedStyle;
import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * Client side implementtaion for ImageEditor widget.
 */
public class VImageEditor extends VerticalPanel implements Paintable, ClientSideHandler {

    public static final String CLASSNAME = "v-image-editor";

    private int margins = 0;

    private int nativeImageWidth;

    private int nativeImageHeight;

    private int explicitWidth = 0;

    private int explicitHeight = 0;

    private double scaleRatio = 1d;

    private Image img = null;

    private final GWTSelector selector = new GWTSelector();

    protected ApplicationConnection client;

    private final Label scaleLabel = new Label();

    private final Label fileNameLabel = new Label();

    private final Label sizeLabel = new Label();

    private final Label mimeLabel = new Label();

    private final ClientSideProxy proxy = new ClientSideProxy(this) {
        {
            register("setFileName", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    fileNameLabel.setText(String.valueOf(params[0]));
                }
            });

            register("setMimeType", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    mimeLabel.setText(String.valueOf(params[0]));
                }
            });

            register("setSource", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    if (img != null) {
                        System.out.println("Remving old img " + img);
                        remove(img);
                    }
                    img = new Image(String.valueOf(params[0]));
                    img.addLoadHandler(new LoadHandler() {
                        @Override
                        public void onLoad(LoadEvent event) {
                            nativeImageWidth = img.getOffsetWidth();
                            nativeImageHeight = img.getOffsetHeight();
                            sizeLabel.setText(nativeImageWidth + " x " + nativeImageHeight);
                            updateImage();
                        }
                    });
                    insert(img, 1);
                }
            });

            register("setCropping", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    setCropping((Boolean) params[0]);
                }
            });

            register("setMarginsPx", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    margins = (Integer) params[0];
                    updateImage();
                }
            });

            register("fetchCropArea", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    int x = (int) (selector.getSelectionXCoordinate() / scaleRatio);
                    int y = (int) (selector.getSelectionYCoordinate() / scaleRatio);
                    int w = (int) (selector.getSelectionWidth() / scaleRatio);
                    int h = (int) (selector.getSelectionHeight() / scaleRatio);
                    call("croppedAreaReady", x, y, w, h);
                }
            });

            register("lockAspectRatio", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    boolean isAspectRatioLocked = (Boolean) params[0];
                    if (selector != null) {
                        selector.setAspectRatio(isAspectRatioLocked ? img.getOffsetWidth() * 1d / img.getOffsetHeight() : -1);
                    }
                }
            });

            register("setMinDimension", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    int minDimension = (Integer) params[0];
                }
            });
        }
    };

    public VImageEditor() {
        setStyleName(CLASSNAME);
        getElement().getStyle().setBackgroundColor("rgba(51,51,51,1)");
        setHorizontalAlignment(ALIGN_CENTER);
        setVerticalAlignment(ALIGN_MIDDLE);

        scaleLabel.getElement().getStyle().setColor("#FFFFFF");
        fileNameLabel.getElement().getStyle().setColor("#FFFFFF");

        final HorizontalPanel details = new HorizontalPanel();
        details.getElement().getStyle().setColor("#FFFFFF");
        details.setWidth("360px");

        details.add(fileNameLabel);
        details.add(sizeLabel);
        details.add(mimeLabel);
        add(details);
        add(scaleLabel);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        proxy.update(this, uidl, client);
        this.client = client;
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        if (width != null && !width.isEmpty()) {
            explicitWidth = ComputedStyle.parseInt(width);
        }
        if (selector.isAttached()) {
            selector.setWidth(img.getWidth() + "px");
        }
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        if (height != null && !height.isEmpty()) {
            explicitHeight = ComputedStyle.parseInt(height);
        }
        if (selector.isAttached()) {
            selector.setHeight(img.getHeight() + "px");
        }
    }

    public void scale(double ratio) {
        if (selector.isAttached()) {
            selector.scale(ratio);
        }
    }

    private void setCropping(Boolean isCropping) {
        if (isCropping) {
            remove(img);
            selector.cropImage(img);
            insert(selector, 1);
        } else {
            remove(selector);
            img.setStyleName("");
            insert(img, 1);
        }

    }

    private void updateImage() {
        if (nativeImageHeight > 0 && nativeImageWidth > 0) {
            int width = explicitWidth == 0 ? nativeImageWidth : explicitWidth - 2 * margins;
            int height = explicitHeight == 0 ? nativeImageHeight : explicitHeight - 2 * margins;

            double heightRatio = height * 1d / nativeImageHeight;
            double widthRatio = width * 1d / nativeImageWidth;
            scaleRatio = Math.min(heightRatio, widthRatio);

            img.setWidth((int) (nativeImageWidth * scaleRatio) + "px");
            img.setHeight((int) (nativeImageHeight * scaleRatio) + "px");

            scaleLabel.setText("Showing " + (int) (width * 1d / nativeImageWidth * 100) + "% of original size");
        }
    }

    @Override
    public boolean initWidget(Object[] params) {
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        VConsole.error("Unhandled server call: " + method);
    }
}
