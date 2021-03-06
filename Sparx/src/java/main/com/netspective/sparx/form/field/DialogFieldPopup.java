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
package com.netspective.sparx.form.field;

import com.netspective.commons.text.TextUtils;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.xdm.XdmEnumeratedAttribute;

/**
 * <code>DialogFieldPopup</code> class represents a dialog field with a pop up window associated with it.
 */
public class DialogFieldPopup
{
    public final String DEFAULT_WINDOW_CLASS = "default";

    private ValueSource imageSrc = ValueSource.NULL_VALUE_SOURCE;
    private String windowClass = DEFAULT_WINDOW_CLASS;
    private ValueSource action = ValueSource.NULL_VALUE_SOURCE;
    private String preActionScript;                 // javascript to execute before opening the popup window
    private String[] fill = null;                   // fields that will be filled with values from the popup
    private boolean allowMulti = false;             // flag to indicate whether or not multiple records can be selected
    private boolean closeAfterSelect = true;        // flag to indicate whether or not the popup window should be closed afterwards
    private boolean hideIfReadOnly = true;          // flag to indicate whether the popup should be shown if the field is readonly
    private String[] extract = null;                // these are the fields whose values will be appended to the popup's URL
    private Style displayStyle = new Style(Style.IMAGE); // display style for the popup action
    private ValueSource displayStyleText = null;    // text caption to display when the popup style is TEXT or BUTTON

    /**
     * A popup action associated with a dialog field can have multiple styles. By default,
     * it displays a popup icon defined in the skin.
     */
    public static class Style extends XdmEnumeratedAttribute
    {
        public static final int IMAGE = 0;
        public static final int TEXT = 1;
        public static final int BUTTON = 2;

        public static final String[] VALUES = new String[]{
            "image", "text", "button"
        };

        public Style()
        {
        }

        public Style(int valueIndex)
        {
            super(valueIndex);
        }

        public String[] getValues()
        {
            return VALUES;
        }
    }

    public ValueSource getAction()
    {
        return action;
    }

    public String getPreActionScript()
    {
        return preActionScript;
    }

    /**
     * Sets the  javascript expression that is evaluated before allowing the action
     */
    public void setPreActionScript(String preActionScript)
    {
        this.preActionScript = preActionScript;
    }

    /**
     * Sets the value source object containing the action to be performed when
     * popup link is clicked.
     *
     * @param action value source object containing the action to be performed
     */
    public void setAction(ValueSource action)
    {
        this.action = action;
    }

    public boolean isAllowMulti()
    {
        return allowMulti;
    }

    public void setAllowMulti(boolean allowMulti)
    {
        this.allowMulti = allowMulti;
    }

    public boolean isCloseAfterSelect()
    {
        return closeAfterSelect;
    }

    /**
     * Is set, the popup window closes automatically after the user selects a value.
     *
     * @param closeAfterSelect <code>true</code> if popup window should be closed automatically,
     *                         after selection of value by the user; <code>false</code> otherwise
     */
    public void setCloseAfterSelect(boolean closeAfterSelect)
    {
        this.closeAfterSelect = closeAfterSelect;
    }

    public boolean isHideIfReadOnly()
    {
        return hideIfReadOnly;
    }

    public void setHideIfReadOnly(boolean hideIfReadOnly)
    {
        this.hideIfReadOnly = hideIfReadOnly;
    }

    public ValueSource getImageSrc()
    {
        return imageSrc;
    }

    /**
     * Sets the source path for the image that is displayed with the
     * popup's link.
     */
    public void setImageSrc(ValueSource imageSrc)
    {
        this.imageSrc = imageSrc;
    }

    public String getWindowClass()
    {
        return windowClass;
    }

    /**
     * Sets the name of the class used for displaying and handling the popup window.
     * The window class contains functionalities such as setting the popup window size
     * and display style.
     *
     * @param windowClass name of the class handling popup window
     */
    public void setWindowClass(String windowClass)
    {
        this.windowClass = windowClass;
    }

    public String[] getExtract()
    {
        return extract;
    }

    public void setExtract(String value)
    {
        extract = TextUtils.getInstance().split(value, ",", true);
    }

    public final String[] getFill()
    {
        return fill;
    }

    /**
     * Dialog field(s) to be populated with the value(s) obtained from the popup.
     *
     * @param fields field(s) to be populated
     */
    public void setFill(String fields)
    {
        fill = TextUtils.getInstance().split(fields, ",", true);
    }

    /**
     * Sets the display style of the popup action
     */
    public void setStyle(Style style)
    {
        this.displayStyle = style;
    }

    public Style getStyle()
    {
        return displayStyle;
    }

    public ValueSource getStyleText()
    {
        return displayStyleText;
    }

    /**
     * Sets the text associated with the display style
     */
    public void setStyleText(ValueSource displayText)
    {
        this.displayStyleText = displayText;
    }

}