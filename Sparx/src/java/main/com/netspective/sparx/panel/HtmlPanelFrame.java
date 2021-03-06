/*
 * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
 *
 * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
 * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
 * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
 * before using The Software. Any use of The Software indicates agreement with the NSL.
 *
 * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
 *
 * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
 *    containing the .class files) and only as part of an application that uses The Software as part of its primary
 *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
 *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
 *    same restrictions.
 *
 * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
 *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
 *
 * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
 *    used to endorse or appear in products derived from The Software without written consent of Netspective.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
 * ARE HEREBY DISCLAIMED.
 *
 * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
 * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
 * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package com.netspective.sparx.panel;

import com.netspective.commons.value.ValueSource;
import com.netspective.commons.xdm.XdmBitmaskedFlagsAttribute;

public class HtmlPanelFrame
{
    public static class Flags extends XdmBitmaskedFlagsAttribute
    {
        public static final int HAS_HEADING = 1;
        public static final int HAS_HEADING_EXTRA = HAS_HEADING * 2;
        public static final int HAS_FOOTING = HAS_HEADING_EXTRA * 2;
        public static final int IS_SELECTABLE = HAS_FOOTING * 2;
        public static final int ALLOW_COLLAPSE = IS_SELECTABLE * 2;
        public static final int IS_COLLAPSED = ALLOW_COLLAPSE * 2;
        public static final int HIDE_HEADING = IS_COLLAPSED * 2;


        public static final XdmBitmaskedFlagsAttribute.FlagDefn[] FLAGDEFNS = new XdmBitmaskedFlagsAttribute.FlagDefn[]
        {
            new XdmBitmaskedFlagsAttribute.FlagDefn(ACCESS_PRIVATE, "HAS_HEADING", HAS_HEADING, "If set, the frame has a heading."),
            new XdmBitmaskedFlagsAttribute.FlagDefn(ACCESS_PRIVATE, "HAS_HEADING_EXTRA", HAS_HEADING_EXTRA),
            new XdmBitmaskedFlagsAttribute.FlagDefn(ACCESS_PRIVATE, "HAS_FOOTING", HAS_FOOTING, "If set, the frame has a footer"),
            new XdmBitmaskedFlagsAttribute.FlagDefn(ACCESS_XDM, "IS_SELECTABLE", IS_SELECTABLE, "If set, it enables selection of multiple rows from the report for passing onto another page for further processing.  Used internally by a report skin called selectable-report.  When this skin is used, the displayed report contains an additional column prepended to each row. This column contains a checkbox for selection of the whole row. The report also contains a link (or button) to navigate to another page/servlet.  All the selected row(s) are passed along with the request."),
            new XdmBitmaskedFlagsAttribute.FlagDefn(ACCESS_XDM, "ALLOW_COLLAPSE", ALLOW_COLLAPSE, "If set, the frame is collapsable.  The user may collapse/restore the frame by using a collapse/restore button provided in the frame."),
            new XdmBitmaskedFlagsAttribute.FlagDefn(ACCESS_XDM, "IS_COLLAPSED", IS_COLLAPSED, "If set, the frame is collapsed.  Otherwise, the frame and its contents are displayed."),
            new XdmBitmaskedFlagsAttribute.FlagDefn(ACCESS_XDM, "HIDE_HEADING", HIDE_HEADING, "If set, the frame heading is not displayer.")
        };

        public XdmBitmaskedFlagsAttribute.FlagDefn[] getFlagsDefns()
        {
            return FLAGDEFNS;
        }
    }

    private ValueSource heading;
    private ValueSource footing;
    private ValueSource allowSelect;
    private Flags flags = new Flags();
    private HtmlPanelActions actions = new HtmlPanelActions();

    public HtmlPanelFrame()
    {
    }

    public Flags getFlags()
    {
        return flags;
    }

    public void setFlags(Flags flags)
    {
        this.flags.copy(flags);
    }

    public ValueSource getAllowSelect()
    {
        return allowSelect;
    }

    /**
     * Used by <code>selectable-report</code> skin to allow selection of multiple
     * rows from the report for passing it onto another page for further processing.
     *
     * @param vs value source object containing the value for allow-select option
     */
    public void setAllowSelect(ValueSource vs)
    {
        allowSelect = vs;
        flags.updateFlag(Flags.IS_SELECTABLE, allowSelect != null);
    }

    public boolean hasHeadingOrFooting()
    {
        return heading != null || footing != null;
    }

    public ValueSource getHeading()
    {
        return heading;
    }

    /**
     * Sets the heading for the frame from a value source.
     *
     * @param vs value source object containing frame heading
     */
    public void setHeading(ValueSource vs)
    {
        heading = vs;
        flags.updateFlag(Flags.HAS_HEADING, heading != null);
    }

    /**
     * Checks to see if the frame heading should be hidden
     *
     * @return True if the frame is configured to hide its heading or if the parent panel has the 'hide frame heading' flag set
     */
    public boolean isHideHeading(HtmlPanelValueContext vc)
    {
        return ((vc.getPanelRenderFlags() & HtmlPanel.RENDERFLAG_HIDE_FRAME_HEADING) != 0) || flags.flagIsSet(Flags.HIDE_HEADING);
    }

    /**
     * Set or clear the hide status of the frame heading
     *
     * @param hide flag for heading visiblity
     */
    public void setHideHeading(boolean hide)
    {
        flags.updateFlag(Flags.HIDE_HEADING, hide);
    }

    public ValueSource getFooting()
    {
        return footing;
    }

    /**
     * Sets the footer for the frame from value source.
     *
     * @param footing footer value source object
     */
    public void setFooting(ValueSource footing)
    {
        this.footing = footing;
        flags.updateFlag(Flags.HAS_FOOTING, footing != null);
    }

    public HtmlPanelActions getActions()
    {
        return actions;
    }

    public void setActions(HtmlPanelActions actions)
    {
        this.actions = actions;
    }

    public HtmlPanelAction createAction()
    {
        return new HtmlPanelAction();
    }

    public void addAction(HtmlPanelAction item)
    {
        actions.add(item);
    }
}