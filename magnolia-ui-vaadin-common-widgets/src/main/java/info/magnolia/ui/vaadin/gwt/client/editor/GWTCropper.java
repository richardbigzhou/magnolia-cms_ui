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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Modification of GwtCropper widget.
 * @see http://code.google.com/p/gwt-cropper/.
 */
public class GWTCropper extends HTMLPanel implements MouseMoveHandler, MouseUpHandler, MouseOutHandler {

    private final IBundleResources bundleResources = GWT.create(IBundleResources.class);

    // canvas sizes
    private int nOuterWidth;
    private int nOuterHeight;

    // selection coordinates
    private int nInnerX;
    private int nInnerY;
    private int nInnerWidth;
    private int nInnerHeight;
    private boolean isDown = false;
    private byte action = Constants.DRAG_NONE;

    // initials to provide crop actions
    private int initW = -1;
    private int initH = -1;

    // X and Y coordinates of cursor before dragging
    private int initX = -1;
    private int initY = -1;

    private int offsetX = -1;
    private int offsetY = -1;

    // instances to canvas and selection area, available for the cropper
    private final AbsolutePanel container;
    private AbsolutePanel handlesContainer;
    private final AbsolutePanel selectionContainer = new AbsolutePanel();
    private HTML draggableBackground;

    private double aspectRatio = 0;

    private final int MIN_SIZE = 20;

    /**
     * Resource bundle.
     */
    public interface IBundleResources extends ClientBundle {

        /**
         * Interface for GwtCropper Css resources.
         */
        interface GWTCropperStyle extends CssResource {
            String base();

            String imageCanvas();

            String selection();

            String handlesContainer();

            String handle();
        }
        
        
        @Source("info/magnolia/ui/vaadin/gwt/public/imageeditor/GWTCropper.css")
        GWTCropperStyle css();
    }

    public GWTCropper() {
        super("");
        this.container = new AbsolutePanel();
        bundleResources.css().ensureInjected();
        addDomHandler(this, MouseMoveEvent.getType());
        addDomHandler(this, MouseUpEvent.getType());
        addDomHandler(this, MouseOutEvent.getType());
    }

    public GWTCropper(Image image) {
        this();
        cropImage(image);
    }
    
    public void cropImage(Image image) {
        addCanvas(image);
    }

    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public double getAspectRatio() {
        return this.aspectRatio;
    }

    public int getSelectionXCoordinate() {
        return this.nInnerX * 2;
    }

    public int getSelectionYCoordinate() {
        return this.nInnerY * 2;
    }

    public int getSelectionWidth() {
        return this.nInnerWidth * 2;
    }

    public int getSelectionHeight() {
        return this.nInnerHeight * 2;
    }

    public int getCanvasHeight() {
        return this.nOuterHeight;
    }

    public int getCanvasWidth() {
        return this.nOuterWidth;
    }

    private void addCanvas(Image image) {
        container.clear();
        super.setStyleName(bundleResources.css().base());
        image.setStyleName(bundleResources.css().imageCanvas());
        nOuterWidth = image.getWidth() / 2;
        nOuterHeight = image.getHeight() / 2;
        container.setWidth(nOuterWidth + "px");
        container.setHeight(nOuterHeight + "px");
        addSelection(image.getUrl());
        this.container.add(image, 0, 0);
        this.add(this.container);
    }
    
    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        container.setHeight(height);
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        container.setWidth(width);
    }
    
    private void addSelection(final String src) {

        selectionContainer.addStyleName(this.bundleResources.css().selection());

        // set initial coordinates
        this.nInnerX = (int) (nOuterWidth * 0.2);
        this.nInnerY = (int) (nOuterHeight * 0.2);
        this.nInnerWidth = (int) (nOuterWidth * 0.2);
        this.nInnerHeight = (int) ((this.aspectRatio == 0) ? (nOuterHeight * 0.2) : (nInnerWidth / aspectRatio));

        selectionContainer.setWidth(this.nInnerWidth + "px");
        selectionContainer.setHeight(this.nInnerHeight + "px");

        // add background image for selection
        final Image selectionImg = new Image(src);
        selectionImg.setWidth(nOuterWidth + "px");
        selectionImg.setHeight(nOuterHeight + "px");
        selectionContainer.add(selectionImg, -this.nInnerX, -this.nInnerY);
        
        this.container.add(selectionContainer, this.nInnerX, this.nInnerY);
        this.buildSelectionArea(selectionContainer);
        this.container.add(this.handlesContainer, this.nInnerX, this.nInnerY);
    }

    private AbsolutePanel buildSelectionArea(final AbsolutePanel selectionContainer) {

        // add selection handles
        handlesContainer = new AbsolutePanel();

        handlesContainer.setWidth(this.nInnerWidth + "px");
        handlesContainer.setHeight(this.nInnerHeight + "px");

        handlesContainer.setStyleName(this.bundleResources.css().handlesContainer());
        handlesContainer.getElement().getStyle().setOverflow(Overflow.VISIBLE);

        // append background
        draggableBackground = this.appendDraggableBackground();

        appendHandle(Cursor.NW_RESIZE, Constants.DRAG_TOP_LEFT_CORNER, -5, 0, 0, -5);
        appendHandle(Cursor.NE_RESIZE, Constants.DRAG_TOP_RIGHT_CORNER, -5, -5, 0, 0);
        appendHandle(Cursor.SW_RESIZE, Constants.DRAG_BOTTOM_LEFT_CORNER, 0, 0, -5, -5);
        appendHandle(Cursor.SE_RESIZE, Constants.DRAG_BOTTOM_RIGHT_CORNER, 0, -5, -5, 0);

        return handlesContainer;
    }

    private void appendHandle(Cursor cursor, final byte actionType, int top, int right, int bottom, int left) {
        HTML handle = new HTML();
        handle.setStyleName(this.bundleResources.css().handle());
        handle.getElement().getStyle().setCursor(cursor);
        handle.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                isDown = true;
                action = actionType;
            }
        });

        if (top != 0)
            handle.getElement().getStyle().setTop(top, Unit.PX);
        if (right != 0)
            handle.getElement().getStyle().setRight(right, Unit.PX);
        if (bottom != 0)
            handle.getElement().getStyle().setBottom(bottom, Unit.PX);
        if (left != 0)
            handle.getElement().getStyle().setLeft(left, Unit.PX);
        this.handlesContainer.add(handle);
    }
    
    private HTML appendDraggableBackground() {

        final HTML backgroundHandle = new HTML();
        backgroundHandle.setWidth(this.nInnerWidth + "px");
        backgroundHandle.setHeight(this.nInnerHeight + "px");
        backgroundHandle.getElement().getStyle().setCursor(Cursor.MOVE);
        backgroundHandle.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                isDown = true;
                action = Constants.DRAG_BACKGROUND;
            }
        });

        this.handlesContainer.add(backgroundHandle, 0, 0);

        return backgroundHandle;
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        this.isDown = false;
        this.reset();
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        if (this.isDown) {
            this.provideDragging(event.getRelativeX(this.container.getElement()), event.getRelativeY(this.container.getElement()));
        }
    }

    private void provideDragging(int cursorX, int cursorY) {

        Element el = null;
        Element el2 = null;
        Element elImg = null;

        int futureWidth = 0;
        int futureHeight = 0;

        switch (this.action) {

        case Constants.DRAG_BACKGROUND:

            if (offsetX == -1) {
                offsetX = cursorX - container.getWidgetLeft(this.handlesContainer);
            }
            if (offsetY == -1) {
                offsetY = cursorY - container.getWidgetTop(this.handlesContainer);
            }

            el = this.handlesContainer.getElement();

            this.nInnerX = cursorX - offsetX;
            this.nInnerY = cursorY - offsetY;

            // don't drag selection out of the canvas borders
            if (this.nInnerX < 0)
                this.nInnerX = 0;
            if (this.nInnerY < 0)
                this.nInnerY = 0;
            if (this.nInnerX + this.nInnerWidth > this.nOuterWidth)
                this.nInnerX = this.nOuterWidth - this.nInnerWidth;
            if (this.nInnerY + this.nInnerHeight > this.nOuterHeight)
                this.nInnerY = this.nOuterHeight - this.nInnerHeight;

            el.getStyle().setLeft(this.nInnerX, Unit.PX);
            el.getStyle().setTop(this.nInnerY, Unit.PX);

            el2 = this.selectionContainer.getElement();
            el2.getStyle().setLeft(this.nInnerX, Unit.PX);
            el2.getStyle().setTop(this.nInnerY, Unit.PX);

            elImg = ((Image) this.selectionContainer.getWidget(0)).getElement();
            elImg.getStyle().setLeft(-this.nInnerX, Unit.PX);
            elImg.getStyle().setTop(-this.nInnerY, Unit.PX);
            break;

        case Constants.DRAG_TOP_LEFT_CORNER:

            if (initX == -1) {
                initX = container.getWidgetLeft(this.handlesContainer);
                initW = nInnerWidth;
            }
            if (initY == -1) {
                initY = container.getWidgetTop(this.handlesContainer);
                initH = nInnerHeight;
            }
            futureWidth = initW + (initX - cursorX);
            futureHeight = initH + (initY - cursorY);

            if (futureWidth < this.MIN_SIZE || futureHeight < this.MIN_SIZE) {
                return;
            }

            this.nInnerWidth = futureWidth;
            this.nInnerHeight = futureHeight;

            this.nInnerX = cursorX;
            this.nInnerY = cursorY;

            // compensation for specified aspect ratio
            if (this.aspectRatio != 0) {
                if (Math.abs(this.initX - this.nInnerX) > Math.abs(this.initY - this.nInnerY)) {
                    int newHeight = (int) (this.nInnerWidth / this.aspectRatio);
                    this.nInnerY -= newHeight - this.nInnerHeight;

                    // to prevent resizing out of the canvas on the Y axes
                    if (this.nInnerY <= 0) {
                        this.nInnerY = 0;
                        newHeight = this.initY + this.initH;
                        this.nInnerWidth = (int) (newHeight * this.aspectRatio);
                        this.nInnerX = this.initX - (int) (this.initY * this.aspectRatio);
                    }

                    this.nInnerHeight = newHeight;
                } else {
                    int newWidth = (int) (this.nInnerHeight * this.aspectRatio);
                    this.nInnerX -= newWidth - this.nInnerWidth;

                    // to prevent resizing out of the canvas on the X axis
                    if (this.nInnerX < 0) {
                        this.nInnerX = 0;
                        newWidth = this.initX + this.initW;
                        this.nInnerHeight = (int) (newWidth / this.aspectRatio);
                        this.nInnerY = this.initY - (int) (this.initX / this.aspectRatio);
                    }

                    this.nInnerWidth = newWidth;
                }
            }

            el = this.handlesContainer.getElement();

            el.getStyle().setLeft(this.nInnerX, Unit.PX);
            el.getStyle().setTop(this.nInnerY, Unit.PX);
            el.getStyle().setWidth(nInnerWidth, Unit.PX);
            el.getStyle().setHeight(nInnerHeight, Unit.PX);

            el2 = this.selectionContainer.getElement();
            el2.getStyle().setLeft(this.nInnerX, Unit.PX);
            el2.getStyle().setTop(this.nInnerY, Unit.PX);
            el2.getStyle().setWidth(nInnerWidth, Unit.PX);
            el2.getStyle().setHeight(nInnerHeight, Unit.PX);

            elImg = ((Image) this.selectionContainer.getWidget(0)).getElement();
            elImg.getStyle().setLeft(-this.nInnerX, Unit.PX);
            elImg.getStyle().setTop(-this.nInnerY, Unit.PX);

            Element el3 = this.draggableBackground.getElement();
            el3.getStyle().setWidth(nInnerWidth, Unit.PX);
            el3.getStyle().setHeight(nInnerHeight, Unit.PX);
            break;

        case Constants.DRAG_TOP_RIGHT_CORNER:

            if (initX == -1) {
                initX = container.getWidgetLeft(this.handlesContainer) + nInnerWidth;
                initW = nInnerWidth;
            }
            if (initY == -1) {
                initY = container.getWidgetTop(this.handlesContainer);
                initH = nInnerHeight;
            }

            futureWidth = initW + (cursorX - initX);
            futureHeight = initH + (initY - cursorY);

            if (futureWidth < this.MIN_SIZE || futureHeight < this.MIN_SIZE) {
                return;
            }

            nInnerWidth = futureWidth;
            nInnerHeight = futureHeight;

            // compensation for specified aspect ratio
            if (this.aspectRatio != 0) {
                if (Math.abs(initX - cursorX) > Math.abs(initY - cursorY)) {
                    // move cursor right, top side has been adjusted
                    // automatically

                    int newHeight = (int) (nInnerWidth / this.aspectRatio);
                    cursorY -= newHeight - nInnerHeight;

                    // to prevent resizing out of the canvas on the Y axes
                    if (cursorY <= 0) {
                        cursorY = 0;
                        newHeight = this.initY + this.initH;
                        this.nInnerWidth = (int) (newHeight * this.aspectRatio);
                    }

                    nInnerHeight = newHeight;
                } else {
                    // move cursor up, right side has been adjusted
                    // automatically

                    nInnerWidth = (int) (nInnerHeight * this.aspectRatio);

                    // to prevent resizing out of the canvas on the X axis
                    if ((this.nInnerWidth + this.nInnerX) >= this.nOuterWidth) {
                        this.nInnerWidth = this.nOuterWidth - this.nInnerX;
                        this.nInnerHeight = (int) (this.nInnerWidth / this.aspectRatio);
                        this.nInnerY = this.initY - (int) ((this.nOuterWidth - this.nInnerX - this.initW) / this.aspectRatio);
                        cursorY = this.nInnerY;
                    }
                }
            }

            this.nInnerY = cursorY;

            el = this.handlesContainer.getElement();

            el.getStyle().setTop(cursorY, Unit.PX);
            el.getStyle().setWidth(nInnerWidth, Unit.PX);
            el.getStyle().setHeight(nInnerHeight, Unit.PX);

            el2 = this.selectionContainer.getElement();
            el2.getStyle().setTop(cursorY, Unit.PX);
            el2.getStyle().setWidth(nInnerWidth, Unit.PX);
            el2.getStyle().setHeight(nInnerHeight, Unit.PX);

            elImg = ((Image) this.selectionContainer.getWidget(0)).getElement();
            elImg.getStyle().setTop(-cursorY, Unit.PX);

            el3 = this.draggableBackground.getElement();
            el3.getStyle().setWidth(nInnerWidth, Unit.PX);
            el3.getStyle().setHeight(nInnerHeight, Unit.PX);
            break;

        case Constants.DRAG_BOTTOM_LEFT_CORNER:

            if (initX == -1) {
                initX = container.getWidgetLeft(this.handlesContainer);
                initW = nInnerWidth;
            }
            if (initY == -1) {
                initY = container.getWidgetTop(this.handlesContainer) + nInnerHeight;
                initH = nInnerHeight;
            }

            futureWidth = initW + (initX - cursorX);
            futureHeight = initH + (cursorY - initY);

            if (futureWidth < this.MIN_SIZE || futureHeight < this.MIN_SIZE) {
                return;
            }

            nInnerWidth = futureWidth;
            nInnerHeight = futureHeight;

            // compensation for specified aspect ratio
            if (this.aspectRatio != 0) {
                if (Math.abs(initX - cursorX) > Math.abs(initY - cursorY)) {
                    // cursor goes left, bottom side goes down...

                    nInnerHeight = (int) (nInnerWidth / this.aspectRatio);

                    // to prevent resizing out of the canvas on the Y axis
                    if ((this.nInnerHeight + this.nInnerY) >= this.nOuterHeight) {
                        this.nInnerHeight = this.nOuterHeight - this.nInnerY;
                        this.nInnerWidth = (int) (this.nInnerHeight * this.aspectRatio);
                        this.nInnerY = this.nOuterHeight - this.nInnerHeight;
                        cursorX = this.initX - (int) ((this.nOuterHeight - this.initY) * this.aspectRatio);
                    }

                } else {
                    // cursor goes down, left side goes to left

                    int newWidth = (int) (nInnerHeight * this.aspectRatio);
                    cursorX -= newWidth - nInnerWidth;

                    // to prevent resizing out of the canvas on the X axis
                    if (cursorX <= 0) {
                        newWidth = this.nInnerWidth + this.initX;
                        this.nInnerHeight = (int) (newWidth / this.aspectRatio);
                        cursorX = 0;
                    }

                    nInnerWidth = newWidth;
                }
            }

            this.nInnerX = cursorX;

            el = this.handlesContainer.getElement();

            el.getStyle().setLeft(cursorX, Unit.PX);
            el.getStyle().setWidth(nInnerWidth, Unit.PX);
            el.getStyle().setHeight(nInnerHeight, Unit.PX);

            el2 = this.selectionContainer.getElement();
            el2.getStyle().setLeft(cursorX, Unit.PX);
            el2.getStyle().setWidth(nInnerWidth, Unit.PX);
            el2.getStyle().setHeight(nInnerHeight, Unit.PX);

            elImg = ((Image) this.selectionContainer.getWidget(0)).getElement();
            elImg.getStyle().setLeft(-cursorX, Unit.PX);

            el3 = this.draggableBackground.getElement();
            el3.getStyle().setWidth(nInnerWidth, Unit.PX);
            el3.getStyle().setHeight(nInnerHeight, Unit.PX);
            break;

        case Constants.DRAG_BOTTOM_RIGHT_CORNER:

            if (initX == -1) {
                initX = container.getWidgetLeft(this.handlesContainer) + nInnerWidth;
                initW = nInnerWidth;
            }
            if (initY == -1) {
                initY = container.getWidgetTop(this.handlesContainer) + nInnerHeight;
                initH = nInnerHeight;
            }

            futureWidth = initW + (cursorX - initX);
            futureHeight = initH + (cursorY - initY);

            if (futureWidth < this.MIN_SIZE || futureHeight < this.MIN_SIZE) {
                return;
            }

            nInnerWidth = futureWidth;
            nInnerHeight = futureHeight;

            // compensation for specified aspect ratio
            if (this.aspectRatio != 0) {
                if (Math.abs(initX - cursorX) > Math.abs(initY - cursorY)) {
                    // cursor goes right, bottom side goes down...

                    nInnerHeight = (int) (nInnerWidth / this.aspectRatio);

                    // to prevent resizing out of the canvas on the Y axis
                    if ((this.nInnerHeight + this.nInnerY) >= this.nOuterHeight) {
                        this.nInnerHeight = this.nOuterHeight - this.nInnerY;
                        this.nInnerWidth = (int) (this.nInnerHeight * this.aspectRatio);
                        this.nInnerY = this.nOuterHeight - this.nInnerHeight;
                        cursorX = this.nOuterWidth;
                    }

                } else {
                    // cursor goes down, right side goes to right
                    nInnerWidth = (int) (nInnerHeight * this.aspectRatio);

                    // to prevent resizing out of the canvas on the X axis
                    if (this.nInnerWidth + this.nInnerX >= this.nOuterWidth) {
                        this.nInnerWidth = this.nOuterWidth - this.nInnerX;
                        this.nInnerHeight = (int) (this.nInnerWidth / this.aspectRatio);
                        cursorX = this.nOuterHeight;
                    }
                }
            }

            el = this.handlesContainer.getElement();
            el.getStyle().setWidth(nInnerWidth, Unit.PX);
            el.getStyle().setHeight(nInnerHeight, Unit.PX);

            el2 = this.selectionContainer.getElement();
            el2.getStyle().setWidth(nInnerWidth, Unit.PX);
            el2.getStyle().setHeight(nInnerHeight, Unit.PX);

            el3 = this.draggableBackground.getElement();
            el3.getStyle().setWidth(nInnerWidth, Unit.PX);
            el3.getStyle().setHeight(nInnerHeight, Unit.PX);
            break;

        default:
            break;
        }

    }

    private void reset() {
        this.initX = -1;
        this.initY = -1;
        this.initW = -1;
        this.initH = -1;
        this.offsetX = -1;
        this.offsetY = -1;
        this.action = Constants.DRAG_NONE;
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        if (this.isDown) {
            this.isDown = false;
            this.reset();
        }
    }
}
