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
 * @author Shahid N. Shah
 */

/**
 * $Id: SparxTask.java,v 1.6 2003-07-17 13:50:25 shahid.shah Exp $
 */

package com.netspective.sparx.ant;

import org.apache.tools.ant.BuildException;

import com.netspective.axiom.ant.AxiomTask;
import com.netspective.axiom.SqlManager;
import com.netspective.commons.xdm.XdmComponent;
import com.netspective.sparx.ProjectComponent;
import com.netspective.sparx.console.ConsoleServlet;
import com.netspective.sparx.form.DialogsManager;
import com.netspective.sparx.form.Dialogs;
import com.netspective.sparx.form.Dialog;

import java.io.IOException;
import java.io.File;

public class SparxTask extends AxiomTask
{
    private String dcbPackage;

    public void init() throws BuildException
    {
        super.init();
        dcbPackage = null;
    }

    public XdmComponent getComponent()
    {
        return getComponent(ProjectComponent.class);
    }

    public SqlManager getSqlManager() throws BuildException
    {
        return ((ProjectComponent) getComponent()).getProject();
    }

    public void setupActionHandlers()
    {
        super.setupActionHandlers();

        addActionHandler(
                new ActionHandler()
                {
                    public String getName() { return "generate-dcb"; }
                    public void execute() throws BuildException
                    {
                        generateDialogContextBeans(getDialogsManager());
                    }
                });
    }

    public String getDcbPackage()
    {
        return dcbPackage;
    }

    public void setDcbPackage(String dcbPackage)
    {
        this.dcbPackage = dcbPackage;
    }

    public boolean generateDialogContextBeans(DialogsManager dialogsManager) throws BuildException
    {
        if(getDestDir() != null || dcbPackage != null)
        {
            if(getDestDir() == null || dcbPackage == null)
                throw new BuildException("dcbPackage is required to generate dialog context beans.");

            if(isCleanFirst())
                delete(new File(getDestDir(), dcbPackage.replace('.', '/')));

            try
            {
                Dialogs dialogs = dialogsManager.getDialogs();
                for(int i = 0; i < dialogs.size(); i++)
                {
                    Dialog dialog = dialogs.get(i);
                    if(dialog.getQualifiedName().startsWith(ConsoleServlet.CONSOLE_ID))
                        continue;

                    dialog.generateDialogContextBean(getDestDir(), dcbPackage);
                }
            }
            catch (IOException e)
            {
                throw new BuildException(e);
            }
            log("Generated dialog context beans (DCBs) package '"+ dcbPackage +"' in " + getDestDir().getAbsolutePath());
            return true;
        }
        else
            return false;
    }

    public DialogsManager getDialogsManager() throws BuildException
    {
        return ((ProjectComponent) getComponent()).getProject();
    }
}
