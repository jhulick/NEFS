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
 * $Id: TemplateContentPanel.java,v 1.9 2004-08-09 22:15:14 shahid.shah Exp $
 */

package com.netspective.sparx.panel;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.netspective.commons.template.TemplateProcessor;
import com.netspective.commons.text.TextUtils;
import com.netspective.commons.xdm.XdmParseContext;
import com.netspective.commons.xdm.XmlDataModelSchema;
import com.netspective.commons.xdm.exception.DataModelException;
import com.netspective.sparx.form.DialogContext;
import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.template.freemarker.FreeMarkerTemplateProcessor;
import com.netspective.sparx.theme.Theme;

public class TemplateContentPanel extends AbstractPanel implements XmlDataModelSchema.CustomElementAttributeSetter
{
    public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
    private TemplateProcessor bodyTemplate;
    private Map templateVars;

    public TemplateContentPanel()
    {
    }

    public TemplateProcessor createBody()
    {
        return new FreeMarkerTemplateProcessor();
    }

    public void addBody(TemplateProcessor templateProcessor)
    {
        bodyTemplate = templateProcessor;
    }

    public void render(Writer writer, NavigationContext nc, Theme theme, int flags) throws IOException
    {
        BasicHtmlPanelValueContext vc = new BasicHtmlPanelValueContext(nc.getServlet(), nc.getRequest(), nc.getResponse(), this);
        vc.setNavigationContext(nc);
        HtmlPanelSkin templatePanelSkin = theme.getTemplatePanelSkin();
        templatePanelSkin.renderFrameBegin(writer, vc);
        bodyTemplate.process(writer, vc, templateVars);
        templatePanelSkin.renderFrameEnd(writer, vc);
    }

    public void render(Writer writer, DialogContext dc, Theme theme, int flags) throws IOException
    {
        BasicHtmlPanelValueContext vc = new BasicHtmlPanelValueContext(dc.getServlet(), dc.getRequest(), dc.getResponse(), this);
        vc.setNavigationContext(dc.getNavigationContext());
        vc.setDialogContext(dc);
        HtmlPanelSkin templatePanelSkin = theme.getTemplatePanelSkin();
        templatePanelSkin.renderFrameBegin(writer, vc);
        bodyTemplate.process(writer, vc, templateVars);
        templatePanelSkin.renderFrameEnd(writer, vc);
    }

    public void setCustomDataModelElementAttribute(XdmParseContext pc, XmlDataModelSchema schema, Object parent, String attrName, String attrValue)
            throws DataModelException, InvocationTargetException, IllegalAccessException, DataModelException
    {
        // if we don't know something about an attribute, save it for the template (pass it in)
        if(templateVars == null)
            templateVars = new HashMap();

        templateVars.put(TextUtils.getInstance().xmlTextToJavaIdentifier(attrName, false), attrValue);
    }
}
