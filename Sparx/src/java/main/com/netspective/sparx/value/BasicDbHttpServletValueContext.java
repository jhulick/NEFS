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
 * $Id: BasicDbHttpServletValueContext.java,v 1.15 2003-06-06 23:10:54 shahid.shah Exp $
 */

package com.netspective.sparx.value;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.apache.commons.lang.exception.NestableRuntimeException;

import freemarker.template.Configuration;
import freemarker.cache.WebappTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;

import com.netspective.axiom.value.BasicDatabaseConnValueContext;
import com.netspective.axiom.SqlManager;
import com.netspective.sparx.ApplicationManager;
import com.netspective.sparx.ApplicationManagerComponent;
import com.netspective.sparx.template.freemarker.FreeMarkerConfigurationAdapters;
import com.netspective.sparx.console.ConsoleServlet;
import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.form.DialogsManager;
import com.netspective.sparx.form.DialogContext;
import com.netspective.sparx.theme.Theme;
import com.netspective.commons.xdm.XdmComponentFactory;
import com.netspective.commons.security.AuthenticatedUser;
import com.netspective.commons.config.ConfigurationsManager;
import com.netspective.commons.acl.AccessControlListsManager;
import com.netspective.commons.text.TextUtils;
import com.netspective.commons.RuntimeEnvironmentFlags;

public class BasicDbHttpServletValueContext extends BasicDatabaseConnValueContext
                                      implements ServletValueContext, HttpServletValueContext,
                                                 DatabaseServletValueContext, DatabaseHttpServletValueContext
{
    public static final String INITPARAMNAME_RUNTIME_ENVIRONMENT_FLAGS = "com.netspective.sparx.RUNTIME_ENVIRONMENT_FLAGS";
    public static final String CONTEXTATTRNAME_RUNTIME_ENVIRONMENT_FLAGS = INITPARAMNAME_RUNTIME_ENVIRONMENT_FLAGS;

    public static final String INITPARAMNAME_ROOT_CONF_FILE = "com.netspective.sparx.CONF_FILE_NAME";
    public static final String CONTEXTATTRNAME_ROOT_CONF_FILE = INITPARAMNAME_ROOT_CONF_FILE;

    public static final String CONTEXTATTRNAME_FREEMARKER_CONFIG = "freemarker-config";
    public static final String INITPARAMNAME_DEFAULT_DATA_SRC_ID = "com.netspective.sparx.DEFAULT_DATA_SOURCE";
    public static final String REQATTRNAME_ACTIVE_THEME = "sparx-active-theme";

    private NavigationContext navigationContext;
    private DialogContext dialogContext;
    private ServletContext context;
    private Servlet servlet;
    private ServletRequest request;
    private ServletResponse response;
    private boolean isPopup;
    private boolean isPrint;
    private String rootUrl;
    private RuntimeEnvironmentFlags contextEnvFlags;

    public BasicDbHttpServletValueContext()
    {
    }

    public BasicDbHttpServletValueContext(ServletContext context, Servlet servlet, ServletRequest request, ServletResponse response)
    {
        initialize(context, servlet, request, response);
    }

    public void initialize(ServletContext context, Servlet servlet, ServletRequest request, ServletResponse response)
    {
        contextNum++;
        this.context = context;
        this.request = request;
        this.response = response;
        this.servlet = servlet;
        rootUrl = ((HttpServletRequest) request).getContextPath();
    }

    public RuntimeEnvironmentFlags getEnvironmentFlags()
    {
        if(contextEnvFlags == null)
        {
            contextEnvFlags = (RuntimeEnvironmentFlags) context.getAttribute(CONTEXTATTRNAME_RUNTIME_ENVIRONMENT_FLAGS);
            String envFlagsText = context.getInitParameter(INITPARAMNAME_RUNTIME_ENVIRONMENT_FLAGS);
            if(envFlagsText == null)
                throw new RuntimeException("No environment flags specified. Check '"+ INITPARAMNAME_RUNTIME_ENVIRONMENT_FLAGS +"' servlet context init parameter.");
            contextEnvFlags = constructEnvironmentFlags();
            contextEnvFlags.setValue(envFlagsText);
            context.setAttribute(CONTEXTATTRNAME_RUNTIME_ENVIRONMENT_FLAGS, contextEnvFlags);

            if(request.getAttribute(ConsoleServlet.REQATTRNAME_INCONSOLE) != null)
                contextEnvFlags.setFlag(RuntimeEnvironmentFlags.CONSOLE_MODE);
        }
        return contextEnvFlags;
    }

    public void initialize(NavigationContext nc)
    {
        initialize(nc.getServletContext(), nc.getServlet(), nc.getRequest(), nc.getResponse());
        setNavigationContext(nc);
    }

    public String getDefaultDataSource()
    {
        String dataSourceId = super.getDefaultDataSource();
        if(dataSourceId != null && dataSourceId.length() > 0)
            return dataSourceId;

        dataSourceId = context.getInitParameter(INITPARAMNAME_DEFAULT_DATA_SRC_ID);
        if(dataSourceId == null)
            throw new RuntimeException("No default data source available. Check '"+ INITPARAMNAME_DEFAULT_DATA_SRC_ID +"' servlet context init parameter.");

        return dataSourceId;
    }

    public NavigationContext getNavigationContext()
    {
        return navigationContext;
    }

    public void setNavigationContext(NavigationContext navigationContext)
    {
        this.navigationContext = navigationContext;
    }

    public DialogContext getDialogContext()
    {
        return dialogContext;
    }

    public void setDialogContext(DialogContext dialogContext)
    {
        this.dialogContext = dialogContext;
    }

    public Object getContextLocation()
    {
        return getHttpRequest().getRequestURI();
    }

    public Object getAttribute(String attributeId)
    {
        return request.getAttribute(attributeId);
    }

    public void setAttribute(String attributeId, Object attributeValue)
    {
        request.setAttribute(attributeId, attributeValue);
    }

    public HttpServletRequest getHttpRequest()
    {
        return (HttpServletRequest) request;
    }

    public HttpServletResponse getHttpResponse()
    {
        return (HttpServletResponse) response;
    }

    public HttpServlet getHttpServlet()
    {
        return (HttpServlet) servlet;
    }

    public ServletRequest getRequest()
    {
        return request;
    }

    public ServletResponse getResponse()
    {
        return response;
    }

    public Servlet getServlet()
    {
        return servlet;
    }

    public ServletContext getServletContext()
    {
        return context;
    }

    public boolean isPopup()
    {
        return isPopup;
    }

    public void setIsPopup(boolean popup)
    {
        this.isPopup = popup;
    }

    public boolean isPrint()
    {
        return isPrint;
    }

    public void setIsPrint(boolean print)
    {
        this.isPrint = print;
    }

    public AuthenticatedUser getAuthenticatedUser()
    {
        return (AuthenticatedUser) getHttpRequest().getSession(true).getAttribute("authenticated-user");
    }

    public void setAuthenticatedUser(AuthenticatedUser user)
    {
        getHttpRequest().getSession(true).setAttribute("authenticated-user", user);
    }

    public ConfigurationsManager getConfigurationsManager()
    {
        return getApplicationManager();
    }

    public AccessControlListsManager getAccessControlListsManager()
    {
        return getApplicationManager();
    }

    public SqlManager getSqlManager()
    {
        return getApplicationManager();
    }

    public DialogsManager getDialogsManager()
    {
        return getApplicationManager();
    }

    public static final String getRootConfFileName(ServletContext context)
    {
        String result = (String) context.getAttribute(CONTEXTATTRNAME_ROOT_CONF_FILE);
        if(result == null)
        {
            result = context.getInitParameter(INITPARAMNAME_ROOT_CONF_FILE);
            if(result == null)
                result = "/WEB-INF/sparx/components.xml";
            if(result.startsWith("/WEB-INF"))
                result = context.getRealPath(result);
            context.setAttribute(CONTEXTATTRNAME_ROOT_CONF_FILE, result);
        }
        return result;
    }

    public ApplicationManagerComponent getApplicationManagerComponent()
    {
        try
        {
            // never store the ApplicationManagerComponent instance since it may change if it needs to be reloaded
            // (always use the factory get() method)
            ApplicationManagerComponent amComponent =
                (ApplicationManagerComponent) XdmComponentFactory.get(
                        ApplicationManagerComponent.class, getRootConfFileName(context),
                        XdmComponentFactory.XDMCOMPFLAGS_DEFAULT);

            for(int i = 0; i < amComponent.getErrors().size(); i++)
                System.err.println(amComponent.getErrors().get(i));
            for(int i = 0; i < amComponent.getWarnings().size(); i++)
                System.out.println(amComponent.getWarnings().get(i));

            return amComponent;
        }
        catch(Exception e)
        {
            throw new NestableRuntimeException(e);
        }
    }

    public ApplicationManager getApplicationManager()
    {
        return getApplicationManagerComponent().getManager();
    }

    public String getApplicationName()
    {
        String servletContextName = context.getServletContextName();

        if (servletContextName != null && servletContextName.length() > 1)
        {
            return TextUtils.sqlIdentifierToText(context.getServletContextName().substring(1), true);
        }
        else
        {
            return null;
        }
    }

    public final String getRootUrl()
    {
        return rootUrl;
    }

    public final String getThemeResourcesRootUrl(Theme theme)
    {
        return rootUrl + "/sparx/theme/" + theme.getName();
    }

    public final String getThemeImagesRootUrl(Theme theme)
    {
        return rootUrl + "/sparx/theme/" + theme.getName() + "/images";
    }

    public final String getServletRootUrl()
    {
        return rootUrl + "/" + getHttpRequest().getServletPath();
    }

    public Theme getActiveTheme()
    {
        return (Theme) request.getAttribute(REQATTRNAME_ACTIVE_THEME);
    }

    public Configuration getFreeMarkerConfiguration()
    {
        Configuration result = (Configuration) context.getAttribute(CONTEXTATTRNAME_FREEMARKER_CONFIG);
        if(result == null)
        {
            result = new Configuration();
            result.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[] {
                    FreeMarkerConfigurationAdapters.getInstance().getStringTemplateLoader(),
                    new WebappTemplateLoader(context)
                }));
            context.setAttribute(CONTEXTATTRNAME_FREEMARKER_CONFIG, result);
        }
        return result;
    }
}
