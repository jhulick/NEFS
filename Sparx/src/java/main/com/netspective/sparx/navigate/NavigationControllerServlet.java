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
 * $Id: NavigationControllerServlet.java,v 1.17 2003-08-22 14:34:07 shahid.shah Exp $
 */

package com.netspective.sparx.navigate;

import java.io.IOException;
import java.io.Writer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.navigate.NavigationSkin;
import com.netspective.sparx.navigate.NavigationPage;
import com.netspective.sparx.Project;
import com.netspective.sparx.util.HttpUtils;
import com.netspective.sparx.util.MultiWebResourceLocator;
import com.netspective.sparx.util.WebResourceLocator;
import com.netspective.sparx.util.InheritableFileWebResourceLocator;
import com.netspective.sparx.security.HttpLoginManager;
import com.netspective.sparx.value.BasicDbHttpServletValueContext;
import com.netspective.sparx.theme.Theme;
import com.netspective.sparx.theme.Themes;
import com.netspective.commons.RuntimeEnvironmentFlags;
import com.netspective.commons.io.FileFind;
import com.netspective.commons.text.TextUtils;

public class NavigationControllerServlet extends HttpServlet
{
    private static final Log log = LogFactory.getLog(NavigationControllerServlet.class);
    public static final String REQATTRNAME_RENDER_START_TIME = NavigationControllerServlet.class.getName() + ".START_TIME";
    public static final String INITPARAMNAME_NAVIGATION_TREE_NAME = "com.netspective.sparx.navigate.NAVIGATION_TREE_NAME";
    public static final String INITPARAMNAME_THEME_NAME = "com.netspective.sparx.theme.THEME_NAME";
    public static final String INITPARAMNAME_LOGIN_MANAGER_NAME = "com.netspective.sparx.security.LOGIN_MANAGER_NAME";
    public static final String INITPARAMNAME_LOGOUT_ACTION_REQ_PARAM_NAME = "com.netspective.sparx.security.LOGOUT_ACTION_REQ_PARAM_NAME";
    public static final String DEFAULT_LOGOUT_ACTION_REQ_PARAM_NAME = "_logout";

    private String loginManagerName;
    private String themeName;
    private String navigationTreeName;
    private String logoutActionReqParamName = DEFAULT_LOGOUT_ACTION_REQ_PARAM_NAME;

    private Project project;
    private HttpLoginManager loginManager;
    private Theme theme;
    private NavigationTree navigationTree;
    private RuntimeEnvironmentFlags runtimeEnvironmentFlags;
    private WebResourceLocator resourceLocator;
    private boolean cacheComponents;

    public void init(ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);

        ServletContext servletContext = servletConfig.getServletContext();

        loginManagerName = servletConfig.getInitParameter(INITPARAMNAME_LOGIN_MANAGER_NAME);
        logoutActionReqParamName = servletConfig.getInitParameter(INITPARAMNAME_LOGOUT_ACTION_REQ_PARAM_NAME);
        if(logoutActionReqParamName == null)
            logoutActionReqParamName = DEFAULT_LOGOUT_ACTION_REQ_PARAM_NAME;

        themeName = servletConfig.getInitParameter(INITPARAMNAME_THEME_NAME);
        navigationTreeName = servletConfig.getInitParameter(INITPARAMNAME_NAVIGATION_TREE_NAME);

        File xdmSourceFile = new File(BasicDbHttpServletValueContext.getProjectFileName(servletContext));
        if(! xdmSourceFile.exists())
            throw new ServletException("Sparx XDM source file '"+ xdmSourceFile.getAbsolutePath() +"' does not exist. Please " +
                    "correct the context-param called '"+ BasicDbHttpServletValueContext.INITPARAMNAME_PROJECT_FILE +"' in your WEB-INF/web.xml file.");

        setRuntimeEnvironmentFlags(BasicDbHttpServletValueContext.getEnvironmentFlags(servletContext));
        initResourceLocators(servletConfig);
        if(isCacheComponents())
        {
            // go ahead and grab all the components now -- so that we don't have to synchronize calls
            getProject();
            getTheme();
            getLoginManager();
            getNavigationTree();
        }
    }

    /**
     * Initializes the web resource locators to the following:
     *   - APP_ROOT/resources/sparx (will only exist if user is overriding any defaults)
     *   - APP_ROOT/sparx (will exist in ITE mode when sparx directory is inside application)
     *   - [CLASS_PATH]/Sparx/resources (only useful during development in SDE, not production since it won't be found)
     *   - TODO: allow locators to be specified in servlet init parameters?
     * @param servletConfig
     * @throws ServletException
     */
    private void initResourceLocators(ServletConfig servletConfig) throws ServletException
    {
        ServletContext servletContext = servletConfig.getServletContext();
        try
        {
            List locators = new ArrayList();
            File sparxOverrides = new File(servletConfig.getServletContext().getRealPath("/resources/sparx"));
            if(sparxOverrides.exists() && sparxOverrides.isDirectory())
                locators.add(new InheritableFileWebResourceLocator(servletContext.getServletContextName() + "/resources/sparx", sparxOverrides, isCacheComponents()));
            File sparxMain = new File(servletConfig.getServletContext().getRealPath("/sparx"));
            if(sparxMain.exists() && sparxMain.isDirectory())
                locators.add(new InheritableFileWebResourceLocator(servletContext.getServletContextName() + "/sparx", sparxMain, isCacheComponents()));

            FileFind.FileFindResults ffResults = FileFind.findInClasspath("Sparx/resources", FileFind.FINDINPATHFLAG_DEFAULT);
            if(ffResults.isFileFound() && ffResults.getFoundFile().isDirectory())
                locators.add(new InheritableFileWebResourceLocator(servletContext.getServletContextName() + "/sparx", ffResults.getFoundFile(), isCacheComponents()));

            if(log.isDebugEnabled())
            {
                for(int i = 0; i < locators.size(); i++)
                    log.debug("Registered web resources locator " + locators.get(i));
            }

            if(locators.size() == 0)
                System.err.println("Unable to register any web resource locators (/resources/sparx and /sparx were not found).");

            resourceLocator = new MultiWebResourceLocator((WebResourceLocator[]) locators.toArray(new WebResourceLocator[locators.size()]), isCacheComponents());
        }
        catch (IOException e)
        {
            log.error("error initializing resource locator", e);
            throw new ServletException(e);
        }
    }

    public RuntimeEnvironmentFlags getRuntimeEnvironmentFlags()
    {
        return runtimeEnvironmentFlags;
    }

    protected void setRuntimeEnvironmentFlags(RuntimeEnvironmentFlags runtimeEnvironmentFlags)
    {
        this.runtimeEnvironmentFlags = runtimeEnvironmentFlags;

        // don't cache if we're in development mode -- we want XML files to be automatically reloaded when they change
        setCacheComponents(! runtimeEnvironmentFlags.flagIsSet(RuntimeEnvironmentFlags.DEVELOPMENT | RuntimeEnvironmentFlags.FRAMEWORK_DEVELOPMENT));
    }

    public boolean isCacheComponents()
    {
        return cacheComponents;
    }

    protected void setCacheComponents(boolean cacheComponents)
    {
        this.cacheComponents = cacheComponents;
    }

    public boolean isSecure()
    {
        return loginManagerName != null;
    }

    public String getLoginManagerName()
    {
        return loginManagerName;
    }

    public void setLoginManagerName(String loginManagerName)
    {
        this.loginManagerName = loginManagerName;
    }

    public String getNavigationTreeName()
    {
        return navigationTreeName;
    }

    public void setNavigationTreeName(String navigationTreeName)
    {
        this.navigationTreeName = navigationTreeName;
    }

    public String getThemeName()
    {
        return themeName;
    }

    public void setThemeName(String themeName)
    {
        this.themeName = themeName;
    }

    public String getLogoutActionReqParamName()
    {
        return logoutActionReqParamName;
    }

    public void setLogoutActionReqParamName(String logoutActionReqParamName)
    {
        this.logoutActionReqParamName = logoutActionReqParamName;
    }

    public Theme getTheme() throws ServletException
    {
        if(theme == null || ! isCacheComponents())
        {
            String themeName = getThemeName();
            Themes themes = getProject().getThemes();
            theme = themeName != null ? themes.getTheme(themeName) : themes.getDefaultTheme();
            theme.setWebResourceLocator(resourceLocator);
        }

        return theme;
    }

    public NavigationTree getNavigationTree() throws ServletException
    {
        if(navigationTree == null || ! isCacheComponents())
        {
            String navTreeName = getNavigationTreeName();
            Project project = getProject();
            navigationTree = navTreeName != null ? project.getNavigationTree(navTreeName) : project.getDefaultNavigationTree();
        }

        return navigationTree;
    }

    public HttpLoginManager getLoginManager() throws ServletException
    {
        if(loginManagerName != null && (loginManager == null || ! isCacheComponents()))
            loginManager = getProject().getLoginManagers().getLoginManager(loginManagerName);
        return loginManager;
    }

    public Project getProject() throws ServletException
    {
        if(project == null || ! isCacheComponents())
            project = BasicDbHttpServletValueContext.getProjectComponent(getServletContext()).getProject();
        return project;
    }

    public NavigationContext createNavigationContext(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
        Project project = getProject();

        Theme theme = getTheme();
        httpServletRequest.setAttribute(BasicDbHttpServletValueContext.REQATTRNAME_ACTIVE_THEME, theme);

        NavigationTree tree = getNavigationTree();
        if(tree == null)
            throw new ServletException("Navigation tree not found. Available: " + project.getNavigationTrees());

        String activePageId = httpServletRequest.getPathInfo();
        if(activePageId == null)
            activePageId = "/";

        NavigationSkin skin = theme.getDefaultNavigationSkin();

        return skin.createContext(getServletContext(), this, httpServletRequest, httpServletResponse,
                                        tree, activePageId);
    }

    protected boolean loginDialogPresented(NavigationContext nc) throws ServletException, IOException
    {
        HttpLoginManager loginManager = getLoginManager();
        if(loginManager != null)
        {
            nc.getRequest().setAttribute(BasicDbHttpServletValueContext.REQATTRNAME_ACTIVE_LOGIN_MANAGER, loginManager);
            return loginManager.loginDialogPresented(nc);
        }

        return false;
    }

    protected void checkForLogout(NavigationContext nc) throws ServletException, IOException
    {
        if(isSecure())
        {
            String logoutActionReqParamValue = nc.getHttpRequest().getParameter(getLogoutActionReqParamName());
            if(logoutActionReqParamValue != null && TextUtils.toBoolean(logoutActionReqParamValue))
                getLoginManager().logout(nc);
        }
    }

    protected void renderPage(NavigationContext nc) throws ServletException, IOException
    {
        // If a login dialog was presented, we don't want to render anything because the login dialog should take over
        // the entire page
        if(isSecure() && loginDialogPresented(nc))
            return;

        NavigationPage activePage = nc.getActivePage();
        Writer writer = nc.getResponse().getWriter();

        if(activePage != null)
        {
            if(nc.isActivePageValid())
                activePage.handlePage(writer, nc);
            else
                activePage.handleInvalidPage(writer, nc);
        }
        else
        {
            NavigationSkin skin = nc.getSkin();
            NavigationTree tree = nc.getOwnerTree();

            skin.renderPageMetaData(writer, nc);
            skin.renderPageHeader(writer, nc);
            writer.write("No page located for path '"+ nc.getActivePathFindResults().getSearchedForPath() +"'.");
            if(nc.getEnvironmentFlags().flagIsSet(RuntimeEnvironmentFlags.DEVELOPMENT))
            {
                writer.write("<pre>\n");
                writer.write(tree.toString());
                writer.write("</pre>\n");
            }
            skin.renderPageFooter(writer, nc);
        }
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
        // record the starting time because it may be used by skins to show complete render times.
        httpServletRequest.setAttribute(REQATTRNAME_RENDER_START_TIME, new Long(System.currentTimeMillis()));

        NavigationContext nc = createNavigationContext(httpServletRequest, httpServletResponse);
        checkForLogout(nc);
        if(nc.isRedirectToAlternateChildRequired())
        {
            String url =nc.getActivePage().getUrl(nc);
            if(url.indexOf('?') == -1) // see if we've appened any parameters (if not, we want to include all)
                url = HttpUtils.appendParams(httpServletRequest, url, "*");
            httpServletResponse.sendRedirect(url);
            return;
        }
        else
            renderPage(nc);
    }

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
        doGet(httpServletRequest, httpServletResponse);
    }
}
