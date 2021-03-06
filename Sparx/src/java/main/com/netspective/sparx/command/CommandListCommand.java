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
package com.netspective.sparx.command;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netspective.commons.command.CommandDocumentation;
import com.netspective.sparx.form.DialogContext;
import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.panel.BasicHtmlPanelValueContext;
import com.netspective.sparx.panel.HtmlCommandPanel;
import com.netspective.sparx.panel.HtmlPanel;
import com.netspective.sparx.panel.HtmlPanelValueContext;
import com.netspective.sparx.theme.Theme;
import com.netspective.sparx.theme.basic.HtmlListPanelSkin;
import com.netspective.sparx.value.HttpServletValueContext;

/**
 * Command class for handling a special command that displays a list of command links
 */
public class CommandListCommand extends AbstractListCommand
{
    private static final Log log = LogFactory.getLog(DialogCommand.class);
    public static final String[] IDENTIFIERS = new String[]{"command-list", "cmd-list"};
    public static final String[] DIALOG_COMMAND_RETAIN_PARAMS =
            {
                PAGE_COMMAND_REQUEST_PARAM_NAME
            };
    public static final CommandDocumentation DOCUMENTATION = new CommandDocumentation("Displays a list of command items such as dialogs, queries, and query definitions to execute.",
                                                                                      new CommandDocumentation.Parameter[]
                                                                                      {
                                                                                      });

    /* command list object */
    private CommandList list;
    private HtmlPanel panel;


    public CommandListCommand()
    {
        super();
    }

    /**
     * Sets the panel to present the list items in
     */
    public void setPanel(HtmlPanel panel)
    {
        this.panel = panel;
    }

    /**
     * Gets the panel to present the list in
     */
    public HtmlPanel getPanel()
    {
        return this.panel;
    }

    /**
     * Sets the command list
     */
    public void setCommandList(CommandList list)
    {
        this.list = list;
    }

    /**
     * Gets the identifiers (aliases) available for the command
     */
    public static String[] getIdentifiers()
    {
        return IDENTIFIERS;
    }

    /**
     * Returns the command documentation
     *
     * @return CommandDocumentation object
     */
    public static CommandDocumentation getDocumentation()
    {
        return DOCUMENTATION;
    }

    /**
     * Gets the parameters for the command.
     * <p/>
     * <i>Currently command-list command does not have any parameters
     * passed to it since it is dependent upon the panel to set the command list object.
     * </i>
     */
    public String getParameters()
    {
        return null;  //To change body of implemented methods use Options | File Templates.
    }

    /**
     * Sets the parameters for the command.
     * <p/>
     * <i>Currently command-list command does not have any parameters
     * passed to it since it is dependent upon the panel to set the command list object.
     * </i>
     */
    public void setParameters(StringTokenizer params)
    {

    }

    /**
     * Gets the list of commands
     */
    public List getItems(HttpServletValueContext vc)
    {
        return list.getItems();
    }

    public HtmlPanelValueContext getPanelContext(HttpServletValueContext vc)
    {
        return new BasicHtmlPanelValueContext(vc.getServlet(), vc.getRequest(),
                                              vc.getResponse(), getPanel());
    }

    /**
     * Renders the list for selection
     * <p/>
     * <i>
     * If this method is modified, the sibling renderListItem(Writer, DialogContext, String)
     * also needs to be modified.
     * </i>
     */
    protected void renderList(Writer writer, NavigationContext nc) throws IOException
    {
        Theme theme = nc.getActiveTheme();
        HtmlListPanelSkin skin = theme.getListPanelSkin();
        skin.renderHtml(writer, getPanelContext(nc), getItems(nc));
    }

    /**
     * Render the selected item component from the list
     * <p/>
     * <i>
     * If this method is modified, the sibling renderListItem(Writer, DialogContext, String)
     * also needs to be modified.
     * </i>
     */
    protected void renderListItem(Writer writer, NavigationContext nc, String activeItem) throws IOException
    {
        Theme theme = nc.getActiveTheme();
        HtmlCommandPanel panel = new HtmlCommandPanel();
        int activeIndex = Integer.parseInt(activeItem);
        CommandListItem commandItem = (CommandListItem) getItems(nc).get(activeIndex);
        panel.setCommand(commandItem.getCommand());
        panel.render(writer, nc, theme, HtmlPanel.RENDERFLAGS_DEFAULT);
    }

    /**
     * Renders the list for selection
     * <p/>
     * <i>
     * If this method is modified, the sibling renderListItem(Writer, NavigationContext, String)
     * also needs to be modified.
     * </i>
     */
    protected void renderList(Writer writer, DialogContext nc) throws IOException
    {
        Theme theme = nc.getActiveTheme();
        HtmlListPanelSkin skin = theme.getListPanelSkin();
        skin.renderHtml(writer, getPanelContext(nc), getItems(nc));
    }

    /**
     * Renders the selected item component from the list.
     * <p/>
     * <i>
     * If this method is modified, the sibling renderListItem(Writer, NavigationContext, String)
     * also needs to be modified.
     * </i>
     */
    protected void renderListItem(Writer writer, DialogContext nc, String activeItem) throws IOException
    {
        Theme theme = nc.getActiveTheme();
        HtmlCommandPanel panel = new HtmlCommandPanel();
        int activeIndex = Integer.parseInt(activeItem);
        CommandListItem commandItem = (CommandListItem) getItems(nc).get(activeIndex);
        panel.setCommand(commandItem.getCommand());
        panel.render(writer, nc, theme, HtmlPanel.RENDERFLAGS_DEFAULT);
    }
}
