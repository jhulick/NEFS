/*
 * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
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
 *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
 *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
 *    without written consent of Netspective.
 *
 * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
 *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
 * ARE HEREBY DISCLAIMED.
 *
 * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
 * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
 * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * @author Aye Thu
 */

/**
 * $Id: AccessControlListDialog.java,v 1.1 2004-01-12 04:57:47 aye.thu Exp $
 */
package app.form.acl;

import com.netspective.sparx.form.listener.DialogPopulateListener;
import com.netspective.sparx.form.handler.DialogExecuteHandler;
import com.netspective.sparx.form.DialogContext;
import com.netspective.sparx.form.DialogExecuteException;
import com.netspective.sparx.form.field.DialogField;
import com.netspective.sparx.form.field.type.SelectFieldChoicesValueSource;
import com.netspective.commons.security.AuthenticatedUser;
import com.netspective.commons.acl.AccessControlList;
import com.netspective.commons.acl.RoleNotFoundException;

import java.io.Writer;
import java.io.IOException;

import auto.dcb.form.acl.RoleContext;

public class AccessControlListDialog implements DialogPopulateListener, DialogExecuteHandler
{
    public void populateDialogValues(DialogContext dc, int formatType)
    {
        RoleContext rc = new RoleContext(dc);

        // only put data into the fields if we're going to display the data for the first time
        // because if it's not the initial entry it means that there's an error on the screen and the user's
        // data (that they entered) should be placed into the fields
        if(dc.getDialogState().isInitialEntry() && formatType == DialogField.DISPLAY_FORMAT)
        {
            AccessControlList list = dc.getProject().getDefaultAccessControlList();

            // role qualified names are the full path names containing ancestor names
            String[] qNames = list.getRoleQualifiedNames();
            // populate the role list
            rc.getRoleListField().setChoices(new SelectFieldChoicesValueSource(qNames));
            // select the roles that are currently assigned to the user
            AuthenticatedUser user = dc.getAuthenticatedUser();
            String[] currentRoles = user.getUserRoleNames();
            rc.getRoleListState().getValue().setValue(currentRoles);
        }
    }

    public void executeDialog(Writer writer, DialogContext dc) throws IOException, DialogExecuteException
    {
        RoleContext rc = new RoleContext(dc);
        // get the newly selected roles for the user
        String[] selectedRoles = rc.getRoleList().getTextValues();
        AuthenticatedUser user = dc.getAuthenticatedUser();
        try
        {
            // change the user's assigned roles
            user.setRoles(dc.getAccessControlListsManager(), selectedRoles);
        }
        catch (RoleNotFoundException e)
        {
            throw new DialogExecuteException("Failed to assign a role to the user", e);
        }

    }
}
