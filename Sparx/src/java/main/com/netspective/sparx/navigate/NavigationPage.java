/*
 * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
 *
 * Netspective Corporation permits redistribution, modification and use
 * of this file in source and binary form ("The Software") under the
 * Netspective Source License ("NSL" or "The License"). The following
 * conditions are provided as a summary of the NSL but the NSL remains the
 * canonical license and must be accepted before using The Software. Any use of
 * The Software indicates agreement with the NSL.
 *
 * 1. Each copy or derived work of The Software must preserve the copyright
 *    notice and this notice unmodified.
 *
 * 2. Redistribution of The Software is allowed in object code form only
 *    (as Java .class files or a .jar file containing the .class files) and only
 *    as part of an application that uses The Software as part of its primary
 *    functionality. No distribution of the package is allowed as part of a software
 *    development kit, other library, or development tool without written consent of
 *    Netspective Corporation. Any modified form of The Software is bound by
 *    these same restrictions.
 *
 * 3. Redistributions of The Software in any form must include an unmodified copy of
 *    The License, normally in a plain ASCII text file unless otherwise agreed to,
 *    in writing, by Netspective Corporation.
 *
 * 4. The names "Sparx" and "Netspective" are trademarks of Netspective
 *    Corporation and may not be used to endorse products derived from The
 *    Software without without written consent of Netspective Corporation. "Sparx"
 *    and "Netspective" may not appear in the names of products derived from The
 *    Software without written consent of Netspective Corporation.
 *
 * 5. Please attribute functionality to Sparx where possible. We suggest using the
 *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
 *    http://www.netspective.com for each application using Sparx.
 *
 * The Software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
 *
 * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING
 * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES.
 *
 * @author Shahid N. Shah
 */

/**
 * $Id: NavigationPage.java,v 1.44 2003-10-27 18:41:52 shahid.shah Exp $
 */

package com.netspective.sparx.navigate;

import com.netspective.commons.value.ValueContext;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.xdm.XdmBitmaskedFlagsAttribute;
import com.netspective.commons.xdm.XmlDataModelSchema;
import com.netspective.commons.xml.template.TemplateConsumer;
import com.netspective.commons.xml.template.TemplateConsumerDefn;
import com.netspective.commons.xml.template.Template;
import com.netspective.commons.command.Commands;
import com.netspective.commons.command.CommandException;
import com.netspective.commons.command.Command;
import com.netspective.commons.text.TextUtils;
import com.netspective.sparx.value.HttpServletValueContext;
import com.netspective.sparx.panel.HtmlLayoutPanel;
import com.netspective.sparx.panel.HtmlPanel;
import com.netspective.sparx.util.HttpUtils;
import com.netspective.commons.template.TemplateProcessor;
import com.netspective.commons.io.InputSourceLocator;
import com.netspective.sparx.command.AbstractHttpServletCommand;
import com.netspective.sparx.command.HttpServletCommand;
import com.netspective.sparx.navigate.listener.NavigationPathListener;
import com.netspective.sparx.navigate.listener.NavigationPageEnterListener;
import com.netspective.sparx.navigate.listener.NavigationPageExitListener;
import com.netspective.sparx.navigate.handler.NavigationPageBodyDefaultHandler;
import com.netspective.sparx.template.freemarker.FreeMarkerTemplateProcessor;

import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NavigationPage extends NavigationPath implements TemplateConsumer, XmlDataModelSchema.InputSourceLocatorListener
{
    public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
    public static final Log log = LogFactory.getLog(NavigationPage.class);
    public static final XdmBitmaskedFlagsAttribute.FlagDefn[] PAGE_FLAG_DEFNS = new XdmBitmaskedFlagsAttribute.FlagDefn[NavigationPathFlags.FLAG_DEFNS.length + 15];
    public static final String ATTRNAME_TYPE = "type";
    public static final String[] ATTRNAMES_SET_BEFORE_CONSUMING = new String[] { "name" };
    public static final String PARAMNAME_PAGE_FLAGS = "page-flags";
    private static final int INHERIT_PAGE_FLAGS_FROM_PARENT = NavigationPath.INHERIT_PATH_FLAGS_FROM_PARENT | Flags.REQUIRE_LOGIN | Flags.ALLOW_PAGE_CMD_PARAM;

    static
    {
        for(int i = 0; i < NavigationPathFlags.FLAG_DEFNS.length; i++)
            PAGE_FLAG_DEFNS[i] = NavigationPathFlags.FLAG_DEFNS[i];
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 0] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_PRIVATE, "REQUIRE_LOGIN", Flags.REQUIRE_LOGIN);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 1] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "REJECT_FOCUS", Flags.REJECT_FOCUS);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 2] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "HIDDEN", Flags.HIDDEN);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 3] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "ALLOW_PAGE_CMD_PARAM", Flags.ALLOW_PAGE_CMD_PARAM);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 4] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "INHERIT_RETAIN_PARAMS", Flags.INHERIT_RETAIN_PARAMS);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 5] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "INHERIT_ASSIGN_STATE_PARAMS", Flags.INHERIT_ASSIGN_STATE_PARAMS);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 6] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "POPUP", Flags.IS_POPUP_MODE);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 7] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "PRINT", Flags.IS_PRINT_MODE);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 8] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "SERVICE", Flags.IS_SERVICE_MODE);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 9] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "SHOW_RENDER_TIME", Flags.SHOW_RENDER_TIME);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 10] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "HANDLE_META_DATA", Flags.HANDLE_META_DATA);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 11] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "HANDLE_HEADER", Flags.HANDLE_HEADER);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 12] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "HANDLE_FOOTER", Flags.HANDLE_FOOTER);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 13] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "DEBUG_REQUEST", Flags.DEBUG_REQUEST);
        PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 14] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "BODY_AFFECTS_NAVIGATION", Flags.BODY_AFFECTS_NAVIGATION);
    }

    protected class PageTypeTemplateConsumerDefn extends TemplateConsumerDefn
    {
        public PageTypeTemplateConsumerDefn()
        {
            super(null, ATTRNAME_TYPE, ATTRNAMES_SET_BEFORE_CONSUMING);
        }

        public String getNameSpaceId()
        {
            return getOwner().getPageTypesTemplatesNameSpaceId();
        }
    }

    public class Flags extends NavigationPathFlags
    {
        public static final int REQUIRE_LOGIN = NavigationPathFlags.START_CUSTOM;
        public static final int REJECT_FOCUS = REQUIRE_LOGIN * 2;
        public static final int HIDDEN = REJECT_FOCUS * 2;
        public static final int ALLOW_PAGE_CMD_PARAM = HIDDEN * 2;
        public static final int INHERIT_RETAIN_PARAMS = ALLOW_PAGE_CMD_PARAM * 2;
        public static final int INHERIT_ASSIGN_STATE_PARAMS = INHERIT_RETAIN_PARAMS * 2;
        public static final int IS_POPUP_MODE = INHERIT_ASSIGN_STATE_PARAMS * 2;
        public static final int IS_PRINT_MODE = IS_POPUP_MODE * 2;
        public static final int IS_SERVICE_MODE = IS_PRINT_MODE * 2;
        public static final int SHOW_RENDER_TIME = IS_SERVICE_MODE * 2;
        public static final int HANDLE_META_DATA = SHOW_RENDER_TIME * 2;
        public static final int HANDLE_HEADER = HANDLE_META_DATA * 2;
        public static final int HANDLE_FOOTER = HANDLE_HEADER * 2;
        public static final int DEBUG_REQUEST = HANDLE_FOOTER * 2;
        public static final int BODY_AFFECTS_NAVIGATION = DEBUG_REQUEST * 2;
        public static final int START_CUSTOM = BODY_AFFECTS_NAVIGATION * 2;

        public Flags()
        {
            setFlag(REQUIRE_LOGIN | HANDLE_META_DATA | HANDLE_HEADER | HANDLE_FOOTER | ALLOW_PAGE_CMD_PARAM | INHERIT_RETAIN_PARAMS | INHERIT_ASSIGN_STATE_PARAMS);
        }

        public FlagDefn[] getFlagsDefns()
        {
            return PAGE_FLAG_DEFNS;
        }

        public void clearFlag(long flag)
        {
            super.clearFlag(flag);
            if((flag & (REJECT_FOCUS | HIDDEN)) != 0)
                clearFlagRecursively(flag);
        }

        public void setFlag(long flag)
        {
            super.setFlag(flag);
            if((flag & (REJECT_FOCUS | HIDDEN)) != 0)
                setFlagRecursively(flag);
        }

        public boolean isPopup()
        {
            return flagIsSet(IS_POPUP_MODE);
        }

        public boolean isHidden()
        {
            return flagIsSet(HIDDEN);
        }

        public boolean isRejectFocus()
        {
            return flagIsSet(REJECT_FOCUS);
        }

        public boolean isDebuggingRequest()
        {
            return flagIsSet(DEBUG_REQUEST);
        }
    }

    public class State extends NavigationPath.State
    {
    }

    private InputSourceLocator inputSourceLocator;
    private TemplateConsumerDefn templateConsumer;
    private NavigationPageBodyType bodyType = new NavigationPageBodyType(NavigationPageBodyType.NONE);
    private String pageFlagsParamName = PARAMNAME_PAGE_FLAGS;
    private ValueSource caption;
    private ValueSource title;
    private ValueSource heading;
    private ValueSource subHeading;
    private ValueSource retainParams;
    private ValueSource assignStateParams;
    private List requireRequestParams = new ArrayList();
    private ValueSource redirect;
    private String redirectTarget;
    private ValueSource forward;
    private ValueSource include;
    private HtmlLayoutPanel bodyPanel;
    private TemplateProcessor bodyTemplate;
    private TemplateProcessor missingParamsBodyTemplate;
    private Command bodyCommand;
    private List pageTypesConsumed = new ArrayList();
    private List customHandlers = new ArrayList();
    private List enterListeners = new ArrayList();
    private List exitListeners = new ArrayList();

    public NavigationPage()
    {
        super();
    }

    public InputSourceLocator getInputSourceLocator()
    {
        return inputSourceLocator;
    }

    public void setInputSourceLocator(InputSourceLocator inputSourceLocator)
    {
        this.inputSourceLocator = inputSourceLocator;
    }

    public void addListener(NavigationPathListener listener)
    {
        super.addListener(listener);
        if(listener instanceof NavigationPageEnterListener)
            enterListeners.add(listener);
        else if(listener instanceof NavigationPageExitListener)
            exitListeners.add(listener);
    }

    /* --- Templates consumption ------------------------------------------------------------------------------------*/

    public TemplateConsumerDefn getTemplateConsumerDefn()
    {
        if(templateConsumer == null)
            templateConsumer = new PageTypeTemplateConsumerDefn();
        return templateConsumer;
    }

    public void registerTemplateConsumption(Template template)
    {
        pageTypesConsumed.add(template.getTemplateName());
    }

    /* --- XDM Callbacks --------------------------------------------------------------------------------------------*/

    /**
     * When inheriting pages, we want our child pages to be the same class as us
     */
    public NavigationPage createPage() throws InstantiationException, IllegalAccessException
    {
        NavigationPage result = (NavigationPage) this.getClass().newInstance();
        result.getFlags().inherit(getFlags(), INHERIT_PAGE_FLAGS_FROM_PARENT);
        return result;
    }

    public void addPage(NavigationPage page)
    {
        appendChild(page);
    }

    public NavigationPathFlags createFlags()
    {
        return new Flags();
    }

    public NavigationPath.State constructState()
    {
        return new State();
    }

    /* -------------------------------------------------------------------------------------------------------------*/

    public void setRequireRequestParams(String params)
    {
        String[] paramNames = TextUtils.split(params, ",", true);
        for(int i = 0; i < paramNames.length; i++)
            requireRequestParams.add(paramNames[i]);
    }

    public void setRequireRequestParam(String param)
    {
        requireRequestParams.add(param);
    }

    public List getRequireRequestParams()
    {
        return requireRequestParams;
    }

    /* -------------------------------------------------------------------------------------------------------------*/

    public boolean isValid(NavigationContext nc)
    {
        List reqParams = getRequireRequestParams();
        if(reqParams.size() > 0)
        {
            ServletRequest request = nc.getRequest();
            for(int i = 0; i < reqParams.size(); i++)
            {
                String name = (String) reqParams.get(i);
                if(request.getParameter(name) == null)
                {
                    nc.setMissingRequiredReqParam(name);
                    return false;
                }
            }
        }

        return true;
    }

    /* -------------------------------------------------------------------------------------------------------------*/

    /**
     * Determines whether the NavigationPath is part of the active path.
     * @param  nc  A context primarily to obtain the Active NavigationPath.
     * @return  <code>true</code> if the NavigationPath object is:
     *              1. The Active NavigationPath.
     *              2. In the ancestor list of the Active NavigationPath.
     *              3. One of the Default Children.
     */
    public boolean isInActivePath(NavigationContext nc)
    {
        //get the current NavigationPath
        NavigationPath activePath = nc.getActivePage();

        if (getQualifiedName().equals(activePath.getQualifiedName())) return true;

        //get the parents and for each set the property of current to true
        List ancestors = activePath.getAncestorsList();
        for (int i = 0; i < ancestors.size(); i++)
        {
            NavigationPath checkPath = (NavigationPath) ancestors.get(i);
            if (getQualifiedName().equals(checkPath.getQualifiedName())) return true;
        }

        /*
        TODO: [SNS] I commented this out since it was causing problems in ConsoleNavigationSkin -- need to investigate
        //get the default children if any and set the property of current to true
        Map childrenMap = activePath.getChildrenMap();
        List childrenList = activePath.getChildrenList();
        while (!childrenMap.isEmpty() && !childrenList.isEmpty())
        {
            NavigationPath defaultChildPath = (NavigationPath) childrenMap.get(activePath.getDefaultChild());
            if (defaultChildPath == null)
                defaultChildPath = (NavigationPath) childrenList.get(0);

            if (getQualifiedName().equals(defaultChildPath.getQualifiedName()))
                return true;

            childrenMap = defaultChildPath.getChildrenMap();
            childrenList = defaultChildPath.getChildrenList();
        }
        */

        return false;
    }

    public void enterPage(NavigationContext nc) throws NavigationException
    {
        ValueSource assignParamsVS = getAssignStateParams();
        if(assignParamsVS != null)
        {
            String assignParams = assignParamsVS.getTextValue(nc);
            if(assignParams != null)
            {
                NavigationPath.State state = nc.getActiveState();
                try
                {
                    HttpUtils.assignParamsToInstance(nc.getHttpRequest(), state, assignParams);
                }
                catch (Exception e)
                {
                    throw new NavigationException(e);
                }
            }
        }

        for (int i = 0; i < enterListeners.size(); i++)
            ((NavigationPageEnterListener) enterListeners.get(i)).enterNavigationPage(this, nc);
    }

    public void exitPage(NavigationContext nc)
    {
        for (int i = 0; i < exitListeners.size(); i++)
            ((NavigationPageExitListener) exitListeners.get(i)).exitNavigationPage(this, nc);
    }

    public void makeStateChanges(NavigationContext nc)
    {
        String pageFlagsParamValue = nc.getRequest().getParameter(getPageFlagsParamName());
        if(pageFlagsParamValue != null)
            nc.getActiveState().getFlags().setValue(pageFlagsParamValue, false);
        super.makeStateChanges(nc);
    }

    /* -------------------------------------------------------------------------------------------------------------*/

    /**
     *  if we have children, get the first child that does not have focus rejected
     */
    public NavigationPage getFirstFocusableChild()
    {
        List childrenList = getChildrenList();
        if(childrenList.size() > 0)
        {
            for(int i = 0; i < childrenList.size(); i++)
            {
                NavigationPage child = (NavigationPage) childrenList.get(i);
                if(! child.getFlags().flagIsSet(Flags.REJECT_FOCUS | Flags.HIDDEN))
                    return child;
                else
                    return child.getNextPath();
            }
        }

        return null;
    }

    /**
     * Return the next sibling that can be focused
     */
    public NavigationPage getNextFocusableSibling()
    {
        // if we get to here we either have no children or all our children don't allow focus
        NavigationPath parent = getParent();
        if(parent != null)
        {
            List siblings = parent.getChildrenList();
            int thisIndex = siblings.indexOf(this);
            if(thisIndex == -1)
                throw new RuntimeException("Unable to find " + this + " in siblings list.");

            // find the first sibling that allows focus
            for(int i = thisIndex + 1; i < siblings.size(); i++)
            {
                NavigationPage sibling = (NavigationPage) siblings.get(i);
                if(! sibling.getFlags().flagIsSet(Flags.REJECT_FOCUS | Flags.HIDDEN))
                    return sibling;
                else
                    return sibling.getNextPath();
            }
        }

        return null;
    }

    /**
     * Return the "next" path (the one immediately following this one). This method will try to obtain the parent node
     * of the given NavigationPath and find itself in the parent's list (its siblings).
     */
    protected NavigationPage getNextPath(boolean checkChildren)
    {
        NavigationPage parent = (NavigationPage) getParent();
        NavigationPage nextPath = checkChildren ? getFirstFocusableChild() : null;
        if(nextPath == null)
        {
            nextPath = getNextFocusableSibling();
            if(nextPath == null && parent != null)
                nextPath = parent.getNextPath(false);
        }
        return nextPath;
    }

    /**
     * Return the "next" path (the one immediately following this one). This method will try to obtain the parent node
     * of the given NavigationPath and find itself in the parent's list (its siblings).
     */
    public NavigationPage getNextPath()
    {
        return getNextPath(true);
    }

    /* -------------------------------------------------------------------------------------------------------------*/

    public ValueSource getCaption()
    {
        return caption;
    }

    public void setCaption(ValueSource caption)
    {
        this.caption = caption;
    }

    public ValueSource getHeading()
    {
        return heading;
    }

    public void setHeading(ValueSource heading)
    {
        this.heading = heading;
    }

    public ValueSource getSubHeading()
    {
        return subHeading;
    }

    public void setSubHeading(ValueSource subHeading)
    {
        this.subHeading = subHeading;
    }

    public ValueSource getTitle()
    {
        return title;
    }

    public void setTitle(ValueSource title)
    {
        this.title = title;
    }

    public ValueSource getRedirect()
    {
        return redirect;
    }

    public void setRedirect(ValueSource redirect)
    {
        this.redirect = redirect;
    }

    public String getRedirectTarget()
    {
        return redirectTarget;
    }

    public void setRedirectTarget(String redirectTarget)
    {
        this.redirectTarget = redirectTarget;
    }

    public void setTarget(String redirectTarget)
    {
        setRedirectTarget(redirectTarget);
    }

    public ValueSource getForward()
    {
        return forward;
    }

    public void setForward(ValueSource forward)
    {
        this.forward = forward;
        getBodyType().setValue(NavigationPageBodyType.FORWARD);
    }

    public ValueSource getInclude()
    {
        return include;
    }

    public void setInclude(ValueSource include)
    {
        this.include = include;
        getBodyType().setValue(NavigationPageBodyType.INCLUDE);
    }

    public String getCaption(ValueContext vc)
    {
        ValueSource vs = getCaption();
        if(vs == null)
            return getName();
        else
            return vs.getTextValue(vc);
    }

    public String getHeading(ValueContext vc)
    {
        ValueSource vs = getHeading();
        if(vs == null)
            return getCaption(vc);
        else
            return vs.getTextValue(vc);
    }

    public String getTitle(ValueContext vc)
    {
        ValueSource vs = getTitle();
        if(vs == null)
            return getHeading(vc);
        else
            return vs.getTextValue(vc);
    }

    public String getSubHeading(ValueContext vc)
    {
        ValueSource vs = getSubHeading();
        if(vs == null)
            return null;
        else
            return vs.getTextValue(vc);
    }

    public String getUrl(HttpServletValueContext vc)
    {
        String result;
        ValueSource vs = getRedirect();
        if(vs == null)
        {
            HttpServletRequest request = vc.getHttpRequest();
            result = request.getContextPath() + request.getServletPath() + getQualifiedName();
        }
        else
            result = vs.getTextValue(vc);

        ValueSource retainParamsVS = getRetainParams();
        if(retainParamsVS != null)
            result = HttpUtils.appendParams(vc.getHttpRequest(), result, retainParamsVS.getTextValue(vc));

        return result;
    }

    public String constructAnchorAttributes(HttpServletValueContext vc)
    {
        StringBuffer sb = new StringBuffer("HREF=\"" + getUrl(vc) + "\"");
        String target = getRedirectTarget();
        if(target != null)
            sb.append("TARGET=\""+ target  +"\"");
        return sb.toString();
    }

    public ValueSource getAssignStateParams()
    {
        if(assignStateParams != null)
            return assignStateParams;

        if(! getFlags().flagIsSet(Flags.INHERIT_ASSIGN_STATE_PARAMS))
            return null;

        NavigationPage parentPage = (NavigationPage) getParent();
        if(parentPage != null)
            return parentPage.getAssignStateParams();

        return null;
    }

    public void setAssignStateParams(ValueSource assignStateParams)
    {
        this.assignStateParams = assignStateParams;
    }

    public ValueSource getRetainParams()
    {
        if(retainParams != null)
            return retainParams;

        if(! getFlags().flagIsSet(Flags.INHERIT_RETAIN_PARAMS))
            return null;

        NavigationPage parentPage = (NavigationPage) getParent();
        if(parentPage != null)
            return parentPage.getRetainParams();

        return null;
    }

    public void setRetainParams(ValueSource retainParams)
    {
        this.retainParams = retainParams;
    }

    public void setAssignAndRetainParams(ValueSource params)
    {
        setAssignStateParams(params);
        setRetainParams(params);
    }

    public Command getCommand()
    {
        return bodyCommand;
    }

    public void setCommand(Command command)
    {
        this.bodyCommand = command;
        getBodyType().setValue(NavigationPageBodyType.COMMAND);
    }

    public String getPageFlagsParamName()
    {
        return pageFlagsParamName;
    }

    public void setPageFlagsParamName(String pageFlagsParamName)
    {
        this.pageFlagsParamName = pageFlagsParamName;
    }

    /* -------------------------------------------------------------------------------------------------------------*/

    public NavigationPageBodyType getBodyType()
    {
        return bodyType;
    }

    public void setBodyType(NavigationPageBodyType bodyType)
    {
        this.bodyType = bodyType;
    }

    public NavigationPageBodyHandler createBodyHandler()
    {
        return new NavigationPageBodyDefaultHandler();
    }

    public void addBodyHandler(NavigationPageBodyHandler handler)
    {
        customHandlers.add(handler);
        getBodyType().setValue(NavigationPageBodyType.CUSTOM_HANDLER);
    }

    public HtmlLayoutPanel createPanels()
    {
        bodyPanel = new HtmlLayoutPanel();
        getBodyType().setValue(NavigationPageBodyType.PANEL);
        return bodyPanel;
    }

    public HtmlLayoutPanel getBodyPanel()
    {
        return bodyPanel;
    }

    public TemplateProcessor createBody()
    {
        return new FreeMarkerTemplateProcessor();
    }

    public void addBody(TemplateProcessor templateProcessor)
    {
        bodyTemplate = templateProcessor;
        getBodyType().setValue(NavigationPageBodyType.TEMPLATE);
    }

    public TemplateProcessor getBodyTemplate()
    {
        return bodyTemplate;
    }

    public TemplateProcessor createMissingParamsBody()
    {
        return new com.netspective.sparx.template.freemarker.FreeMarkerTemplateProcessor();
    }

    public void addMissingParamsBody(TemplateProcessor templateProcessor)
    {
        missingParamsBodyTemplate = templateProcessor;
    }

    public TemplateProcessor getMissingParamsBody()
    {
        return missingParamsBodyTemplate;
    }

    public boolean canHandlePage(NavigationContext nc)
    {
        return true;
    }

    public void handlePageMetaData(Writer writer, NavigationContext nc) throws ServletException, IOException
    {
        NavigationSkin skin = nc.getSkin();
        if(skin != null) skin.renderPageMetaData(writer, nc);
    }

    public void handlePageHeader(Writer writer, NavigationContext nc) throws ServletException, IOException
    {
        NavigationSkin skin = nc.getSkin();
        if(skin != null) skin.renderPageHeader(writer, nc);
    }

    public void handlePageBody(Writer writer, NavigationContext nc) throws ServletException, IOException
    {
        // see if dynamic commands should be allowed
        if(getFlags().flagIsSet(Flags.ALLOW_PAGE_CMD_PARAM))
        {
            String commandSpec = nc.getRequest().getParameter(AbstractHttpServletCommand.PAGE_COMMAND_REQUEST_PARAM_NAME);
            if(commandSpec != null)
            {
                HttpServletCommand command = (HttpServletCommand) Commands.getInstance().getCommand(commandSpec);
                try
                {
                    command.handleCommand(writer, nc, false);
                }
                catch (CommandException e)
                {
                    log.error("Command error in body", e);
                    throw new ServletException(e);
                }
                return;
            }
        }

        switch(getBodyType().getValueIndex())
        {
            case NavigationPageBodyType.NONE:
                writer.write("Path '"+ nc.getActivePathFindResults().getSearchedForPath() +"' is a " + this.getClass().getName() + " class but has no body.");
                break;

            case NavigationPageBodyType.OVERRIDE:
                writer.write("Path '"+ nc.getActivePathFindResults().getSearchedForPath() +"' is a " + this.getClass().getName() + " class and is set as override class but does not override handlePageBody().");
                break;

            case NavigationPageBodyType.CUSTOM_HANDLER:
                for(int i = 0; i < customHandlers.size(); i++)
                    ((NavigationPageBodyHandler) customHandlers.get(i)).handleNavigationPageBody(this, writer, nc);
                break;

            case NavigationPageBodyType.COMMAND:
                try
                {
                    ((HttpServletCommand) getCommand()).handleCommand(writer, nc, false);
                }
                catch (CommandException e)
                {
                    log.error("Command error in body", e);
                    throw new ServletException(e);
                }
                break;

            case NavigationPageBodyType.PANEL:
                getBodyPanel().render(writer, nc, nc.getActiveTheme(), HtmlPanel.RENDERFLAGS_DEFAULT);
                break;

            case NavigationPageBodyType.TEMPLATE:
                getBodyTemplate().process(writer, nc, null);
                break;

            case NavigationPageBodyType.FORWARD:
                // this should never happen -- forwards should never get to this point but we'll add a sanity check
                writer.write("Path '"+ nc.getActivePathFindResults().getSearchedForPath() +"' is a " + this.getClass().getName() + " class and the body type is set to FORWARD but forwarding should happen before any response is committed.");
                break;

            case NavigationPageBodyType.INCLUDE:
                // NOTE: the tricky thing about rd.include is that it has it uses the getReponse().getWriter(), not our writer
                //       that was passed in -- that may cause some confusion in the output
                String includeUrl = getInclude().getTextValue(nc);
                RequestDispatcher rd = nc.getRequest().getRequestDispatcher(includeUrl);
                rd.include(nc.getRequest(), nc.getResponse());
                break;

            default:
                writer.write("Path '"+ nc.getActivePathFindResults().getSearchedForPath() +"' is a " + this.getClass().getName() + " but doesn't know how to handle body type " + getBodyType().getValueIndex() + ".");
        }
    }

    public void handlePageFooter(Writer writer, NavigationContext nc) throws ServletException, IOException
    {
        NavigationSkin skin = nc.getSkin();
        if(skin != null) skin.renderPageFooter(writer, nc);
    }

    public boolean bodyAffectsNavigationContext(NavigationContext nc)
    {
        if(bodyPanel != null && bodyPanel.affectsNavigationContext(nc))
            return true;
        else
            return nc.getActiveState().getFlags().flagIsSet(Flags.BODY_AFFECTS_NAVIGATION);
    }

    public void handlePage(Writer writer, NavigationContext nc) throws ServletException, IOException
    {
        Flags flags = (Flags) nc.getActiveState().getFlags();

        enterPage(nc);
        if(getBodyType().getValueIndex() == NavigationPageBodyType.FORWARD)
        {
            // if we're forwarding to another resource we don't want to put anything into the response otherwise
            // there will be an illegal state exception -- so, we don't create headers, footers, etc because that's
            // the user's responsibility in the forwarded resource.

            String forwardUrl = getForward().getTextValue(nc);
            ServletRequest req = nc.getRequest();
            RequestDispatcher rd = req.getRequestDispatcher(forwardUrl);
            rd.forward(req, nc.getResponse());
        }
        else if(bodyAffectsNavigationContext(nc))
        {
            // render the body first and let it modify the navigation context
            StringWriter body = new StringWriter();
            handlePageBody(body, nc);

            if(flags.flagIsSet(Flags.HANDLE_META_DATA))
                handlePageMetaData(writer, nc);
            if(flags.flagIsSet(Flags.HANDLE_HEADER))
                handlePageHeader(writer, nc);
            writer.write(body.getBuffer().toString());
            if(flags.flagIsSet(Flags.HANDLE_FOOTER))
                handlePageFooter(writer, nc);

            // try and do an early GC if possible
            body = null;
        }
        else
        {
            if(flags.flagIsSet(Flags.HANDLE_META_DATA))
                handlePageMetaData(writer, nc);
            if(flags.flagIsSet(Flags.HANDLE_HEADER))
                handlePageHeader(writer, nc);
            handlePageBody(writer, nc);
            if(flags.flagIsSet(Flags.HANDLE_FOOTER))
                handlePageFooter(writer, nc);
        }
        exitPage(nc);
    }

    public void handleInvalidPage(Writer writer, NavigationContext nc) throws ServletException, IOException
    {
        Flags flags = (Flags) nc.getActiveState().getFlags();

        enterPage(nc);
        if(flags.flagIsSet(Flags.HANDLE_META_DATA))
            handlePageMetaData(writer, nc);
        if(flags.flagIsSet(Flags.HANDLE_HEADER))
            handlePageHeader(writer, nc);

        TemplateProcessor templateProcessor = getMissingParamsBody();
        if(templateProcessor != null)
            templateProcessor.process(writer, nc, null);
        else
            writer.write("This page is missing some required parameters.");

        if(flags.flagIsSet(Flags.HANDLE_FOOTER))
            handlePageFooter(writer, nc);
        exitPage(nc);
    }
}