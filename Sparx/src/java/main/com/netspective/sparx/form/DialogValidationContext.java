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
package com.netspective.sparx.form;

import com.netspective.commons.validate.BasicValidationContext;
import com.netspective.commons.value.ValueContext;
import com.netspective.sparx.form.field.DialogField;
import com.netspective.sparx.form.field.DialogFieldValue;

public class DialogValidationContext extends BasicValidationContext
{
    /**
     * Flag value indicating that the dialog validatation has not been performed
     */
    public static final int VALSTAGE_NOT_PERFORMED = 0;
    /**
     * Flag value indicating that the dialog validation failed
     */
    public static final int VALSTAGE_PERFORMED_FAILED = 1;
    /**
     * Flag value indicating that dialog validation succeeded
     */
    public static final int VALSTAGE_PERFORMED_SUCCEEDED = 2;
    public static final int VALSTAGE_IGNORE = 3;

    private int validationStage = VALSTAGE_NOT_PERFORMED;

    public DialogValidationContext(ValueContext valueContext)
    {
        super(valueContext);
    }

    public DialogContext getDialogContext()
    {
        return (DialogContext) getValidationValueContext();
    }

    /**
     * Indicates whether or not validation has been performed for the dialog
     *
     * @return boolean True if validation has been done
     */
    public boolean validationPerformed()
    {
        return validationStage != VALSTAGE_NOT_PERFORMED ? true : false;
    }

    /**
     * Returns the validation stage
     *
     * @return int
     */
    public int getValidationStage()
    {
        return validationStage;
    }

    /**
     * Sets the validation stage
     *
     * @param value stage
     */
    public void setValidationStage(int value)
    {
        validationStage = value;
    }

    public void addError(Object key, String message)
    {
        DialogFieldValue dfValue = (DialogFieldValue) key;
        DialogField parent = dfValue.getField().getParent();
        if(parent != null)
        {
            DialogContext dc = getDialogContext();
            super.addError(dc.getFieldStates().getState(parent).getValue(), message);
        }
        else
            super.addError(key, message);
    }
}

