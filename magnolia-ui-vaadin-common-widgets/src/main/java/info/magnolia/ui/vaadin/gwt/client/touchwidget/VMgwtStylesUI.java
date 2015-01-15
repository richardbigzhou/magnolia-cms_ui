/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.touchwidget;

import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.TextResource;
import com.googlecode.mgwt.ui.client.MGWTStyle;
import com.googlecode.mgwt.ui.client.theme.MGWTClientBundle;
import com.googlecode.mgwt.ui.client.theme.MGWTTheme;
import com.googlecode.mgwt.ui.client.theme.base.ButtonBarButtonCss;
import com.googlecode.mgwt.ui.client.theme.base.ButtonBarCss;
import com.googlecode.mgwt.ui.client.theme.base.ButtonCss;
import com.googlecode.mgwt.ui.client.theme.base.CarouselCss;
import com.googlecode.mgwt.ui.client.theme.base.CheckBoxCss;
import com.googlecode.mgwt.ui.client.theme.base.DialogCss;
import com.googlecode.mgwt.ui.client.theme.base.GroupingList;
import com.googlecode.mgwt.ui.client.theme.base.HeaderCss;
import com.googlecode.mgwt.ui.client.theme.base.InputCss;
import com.googlecode.mgwt.ui.client.theme.base.LayoutCss;
import com.googlecode.mgwt.ui.client.theme.base.ListCss;
import com.googlecode.mgwt.ui.client.theme.base.MSearchBoxCss;
import com.googlecode.mgwt.ui.client.theme.base.MainCss;
import com.googlecode.mgwt.ui.client.theme.base.PanelCss;
import com.googlecode.mgwt.ui.client.theme.base.ProgressBarCss;
import com.googlecode.mgwt.ui.client.theme.base.ProgressIndicatorCss;
import com.googlecode.mgwt.ui.client.theme.base.PullToRefreshCss;
import com.googlecode.mgwt.ui.client.theme.base.ScrollPanelCss;
import com.googlecode.mgwt.ui.client.theme.base.SliderCss;
import com.googlecode.mgwt.ui.client.theme.base.TabBarCss;
import com.googlecode.mgwt.ui.client.theme.base.UtilCss;

/**
 * Special {@link com.vaadin.client.ui.VUI} extension to prevent MGWT from injecting its theme styles, which would otherwise potentially
 * override some essential styles in AdminCentral.
 */
public class VMgwtStylesUI extends VNonScrollableUI {

    public VMgwtStylesUI() {
        MGWTStyle.setTheme(new MGWTTheme() {

            private MGWTClientBundle bundle;

            @Override
            public MGWTClientBundle getMGWTClientBundle() {
                return bundle;
            }

            {
                bundle = new MGWTDummyClientBundle();
            }

        });
    }

    /**
     * Dummy theme bundle with no style injection - especially for main.
     */
    private final class MGWTDummyClientBundle implements MGWTClientBundle {

        @Override
        public TextResource utilTextResource() {
            return null;
        }

        @Override
        public ImageResource tabBarSearchImage() {
            return null;
        }

        @Override
        public ImageResource tabBarMostViewedImage() {
            return null;
        }

        @Override
        public ImageResource tabBarMostRecentImage() {
            return null;
        }

        @Override
        public ImageResource tabBarMoreImage() {
            return null;
        }

        @Override
        public ImageResource tabBarHistoryImage() {
            return null;
        }

        @Override
        public ImageResource tabBarFeaturedImage() {
            return null;
        }

        @Override
        public ImageResource tabBarFavoritesImage() {
            return null;
        }

        @Override
        public ImageResource tabBarDownloadsImage() {
            return null;
        }

        @Override
        public ImageResource tabBarContactsImage() {
            return null;
        }

        @Override
        public ImageResource tabBarBookMarkImage() {
            return null;
        }

        @Override
        public DataResource spinnerWhiteImage() {
            return null;
        }

        @Override
        public DataResource spinnerImage() {
            return null;
        }

        @Override
        public DataResource searchSearchImage() {
            return null;
        }

        @Override
        public DataResource searchClearTouchedImage() {
            return null;
        }

        @Override
        public DataResource searchClearImage() {
            return null;
        }

        @Override
        public DataResource listArrow() {
            return null;
        }

        @Override
        public DataResource inputCheckImage() {
            return null;
        }

        @Override
        public UtilCss getUtilCss() {
            return null;
        }

        @Override
        public TabBarCss getTabBarCss() {
            return null;
        }

        @Override
        public SliderCss getSliderCss() {
            return null;
        }

        @Override
        public MSearchBoxCss getSearchBoxCss() {
            return null;
        }

        @Override
        public ScrollPanelCss getScrollPanelCss() {
            return null;
        }

        @Override
        public PullToRefreshCss getPullToRefreshCss() {
            return null;
        }

        @Override
        public ProgressIndicatorCss getProgressIndicatorCss() {
            return null;
        }

        @Override
        public ProgressBarCss getProgressBarCss() {
            return null;
        }

        @Override
        public PanelCss getPanelCss() {
            return null;
        }

        @Override
        public MainCss getMainCss() {
            return new MainCss() {

                @Override
                public boolean ensureInjected() {
                    return false;
                }

                @Override
                public String getName() {
                    return "getMainCss";
                }

                @Override
                public String getText() {
                    return "";
                }
            };
        }

        @Override
        public ListCss getListCss() {
            return null;
        }

        @Override
        public LayoutCss getLayoutCss() {
            return null;
        }

        @Override
        public InputCss getInputCss() {
            return null;
        }

        @Override
        public HeaderCss getHeaderCss() {
            return null;
        }

        @Override
        public GroupingList getGroupingList() {
            return null;
        }

        @Override
        public DialogCss getDialogCss() {
            return null;
        }

        @Override
        public CheckBoxCss getCheckBoxCss() {
            return null;
        }

        @Override
        public CarouselCss getCarouselCss() {
            return null;
        }

        @Override
        public ButtonCss getButtonCss() {
            return null;
        }

        @Override
        public ImageResource getButtonBarTrashImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarStopImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarSearchImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarRewindImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarReplyImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarRefreshImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarPreviousSlideImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarPlusImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarPlayImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarPauseImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarOrganizeImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarNextSlideImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarNewImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarMinusImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarLocateImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarInfoImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarHighlightImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarFastForwardImage() {
            return null;
        }

        @Override
        public ButtonBarCss getButtonBarCss() {
            return null;
        }

        @Override
        public ImageResource getButtonBarComposeImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarCameraImage() {
            return null;
        }

        @Override
        public ButtonBarButtonCss getButtonBarButtonCss() {
            return null;
        }

        @Override
        public ImageResource getButtonBarBookmarkImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarArrowUpImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarArrowRightImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarArrowLeftImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarArrowDownImage() {
            return null;
        }

        @Override
        public ImageResource getButtonBarActionImage() {
            return null;
        }

        @Override
        public DataResource errorImage() {
            return null;
        }

        @Override
        public DataResource android_check_not_checked() {
            return null;
        }

        @Override
        public DataResource android_check_checked() {
            return null;
        }
    }
}
