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

import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
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

    private int nativeImageWidth;

    private int nativeImageHeight;

    private double aspectRatio = 0d;

    private Image img;

    protected ApplicationConnection client;

    private final GWTCropper cropper = new GWTCropper();

    private final ClientSideProxy proxy = new ClientSideProxy(this) {
        {
            register("setSource", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    img = new Image(String.valueOf(params[0]));
                    img.addLoadHandler(new LoadHandler() {
                        @Override
                        public void onLoad(LoadEvent event) {
                            nativeImageWidth = img.getOffsetWidth();
                            nativeImageHeight = img.getOffsetHeight();
                            aspectRatio = (double) nativeImageWidth / nativeImageHeight;
                            int w = Math.min(getOffsetWidth(), nativeImageWidth);
                            img.setWidth(w / 2 + "px");
                            img.setHeight(w / aspectRatio / 2 + "px");
                        }
                    });
                    add(img);
                }
            });

            register("setCropping", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    setCropping((Boolean) params[0]);
                }
            });

            register("fetchCropArea", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    call("croppedAreaReady", 
                            cropper.getSelectionXCoordinate(), 
                            cropper.getSelectionYCoordinate(), 
                            cropper.getSelectionWidth(),
                            cropper.getSelectionHeight());
                }
            });

            register("lockAspectRatio", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    double aspectRatio = (Double) params[0];
                    if (cropper != null) {
                        cropper.setAspectRatio(aspectRatio);
                    }
                }
            });
        }
    };

    public VImageEditor() {
        setStyleName(CLASSNAME);
        getElement().getStyle().setBackgroundColor("rgba(0,0,0, 0.5)");
        setHorizontalAlignment(ALIGN_CENTER);
        setVerticalAlignment(ALIGN_MIDDLE);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        proxy.update(this, uidl, client);
        this.client = client;
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        if (cropper.isAttached()) {
            cropper.setWidth(img.getWidth() + "px");
        }
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        if (cropper.isAttached()) {
            cropper.setHeight(img.getHeight() + "px");
        }
    }

    private void setCropping(Boolean isCropping) {
        if (isCropping) {
            remove(img);
            cropper.cropImage(img);
            add(cropper);
        } else {
            remove(cropper);
            img.setStyleName("");
            add(img);
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
