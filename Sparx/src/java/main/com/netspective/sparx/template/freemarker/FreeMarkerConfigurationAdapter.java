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
package com.netspective.sparx.template.freemarker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

public class FreeMarkerConfigurationAdapter
{
    private static final Log log = LogFactory.getLog(FreeMarkerConfigurationAdapter.class);

    private Configuration configuration;
    private boolean defaultAdapter;
    private String name;
    private File baseDir;
    private Class baseClass;
    private String baseClassPath;
    private int templateUpdateDelay = 0;

    public FreeMarkerConfigurationAdapter()
    {
        configuration = new Configuration();
        configuration.setTemplateUpdateDelay(templateUpdateDelay);
        configuration.setTemplateLoader(FreeMarkerConfigurationAdapters.getInstance().getStringTemplateLoader());
        FreeMarkerConfigurationAdapters.getInstance().configureSharedVariables(configuration);
    }

    public boolean isDefault()
    {
        return defaultAdapter;
    }

    public void setDefault(boolean defaultAdapter)
    {
        this.defaultAdapter = defaultAdapter;
        if (defaultAdapter)
            Configuration.setDefaultConfiguration(configuration);
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    protected TemplateLoader getClassTemplateLoader()
    {
        if (baseClass == null)
            return null;

        if (baseClassPath == null)
            return new ClassTemplateLoader(baseClass);

        return new ClassTemplateLoader(baseClass, baseClassPath);
    }

    protected void updateConfiguration()
    {
        List tmplLoaders = new ArrayList();
        tmplLoaders.add(FreeMarkerConfigurationAdapters.getInstance().getStringTemplateLoader());

        try
        {
            if (baseDir != null)
                tmplLoaders.add(new FileTemplateLoader(baseDir));
        }
        catch (IOException e)
        {
            throw new NestableRuntimeException(e);
        }

        if (baseClass != null)
            tmplLoaders.add(getClassTemplateLoader());

        TemplateLoader[] templateLoaders = (TemplateLoader[]) tmplLoaders.toArray(new TemplateLoader[tmplLoaders.size()]);
        MultiTemplateLoader multiTemplateLoader = new MultiTemplateLoader(templateLoaders);
        getConfiguration().setTemplateLoader(multiTemplateLoader);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getTemplateUpdateDelay()
    {
        return templateUpdateDelay;
    }

    public void setTemplateUpdateDelay(int templateUpdateDelay)
    {
        this.templateUpdateDelay = templateUpdateDelay;
        getConfiguration().setTemplateUpdateDelay(templateUpdateDelay);
    }

    public File getBaseDir()
    {
        return baseDir;
    }

    public void setBaseDir(File baseDir)
    {
        this.baseDir = baseDir;
        updateConfiguration();
    }

    public Class getBaseClass()
    {
        return baseClass;
    }

    public void setBaseClass(Class baseClass)
    {
        this.baseClass = baseClass;
        updateConfiguration();
    }

    public String getBaseClassPath()
    {
        return baseClassPath;
    }

    public void setBaseClassPath(String baseClassPath)
    {
        this.baseClassPath = baseClassPath;
        updateConfiguration();
    }
}
