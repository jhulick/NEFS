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
package com.netspective.axiom.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netspective.axiom.ConnectionProvider;
import com.netspective.axiom.ConnectionProviderEntries;
import com.netspective.axiom.ConnectionProviderEntry;
import com.netspective.commons.value.ValueContext;

public class JndiConnectionProvider implements ConnectionProvider
{
    private static final Log log = LogFactory.getLog(JndiConnectionProvider.class);

    private final InitialContext initialContext;
    private Context rootContext;
    private String rootContextName;

    public JndiConnectionProvider() throws NamingException
    {
        initialContext = new InitialContext();
        rootContext = initialContext;
    }

    public InitialContext getInitialContext()
    {
        return initialContext;
    }

    public Context getRootContext()
    {
        return rootContext;
    }

    public String getRootContextName()
    {
        return rootContextName;
    }

    public void setRootContextName(String rootContextName) throws NamingException
    {
        this.rootContextName = rootContextName;
        rootContext = (Context) initialContext.lookup(rootContextName);
    }

    public Class getUnderlyingImplementationClass()
    {
        Context jndiJdbcContext = getRootContext();
        if(jndiJdbcContext != null)
            return jndiJdbcContext.getClass();
        else
            return null;
    }

    public final Connection getConnection(ValueContext vc, String dataSourceId) throws NamingException, SQLException
    {
        if(dataSourceId == null)
            throw new NamingException("dataSourceId is NULL in " + this.getClass().getName() + ".getConnection(String)");

        Context env = getRootContext();
        DataSource source = (DataSource) env.lookup(dataSourceId);
        if(source == null)
        {
            if(log.isDebugEnabled())
                log.debug("dataSourceId not found in " + JndiConnectionProvider.class.getName() + ".getConnection('" + dataSourceId + "'). Available: " + getAvailableDataSources());
            throw new NamingException("Data source '" + dataSourceId + "' not found in JNDI provider " + env);
        }

        return source.getConnection();
    }

    public ConnectionProviderEntry getDataSourceEntry(String dataSourceId, DataSource source) throws SQLException
    {
        BasicConnectionProviderEntry result = new BasicConnectionProviderEntry();
        result.init(dataSourceId, source);
        return result;
    }

    public ConnectionProviderEntry getDataSourceEntry(ValueContext vc, String dataSourceId)
    {
        try
        {
            DataSource source = (DataSource) getRootContext().lookup(dataSourceId);
            if(source == null)
                return null;

            return getDataSourceEntry(dataSourceId, source);
        }
        catch(Exception ex)
        {
            log.debug(JndiConnectionProvider.class.getName() + ".getDataSourceEntry('" + dataSourceId + "')", ex);
            return null;
        }
    }

    public Set getAvailableDataSources()
    {
        Set result = new HashSet();
        try
        {
            String envPath = getRootContextName();
            Context env = getRootContext();
            if(env != null)
            {
                for(NamingEnumeration e = env.list(""); e.hasMore();)
                {
                    NameClassPair entry = (NameClassPair) e.nextElement();
                    result.add(envPath != null ? (envPath + "/" + entry.getName()) : entry.getName());
                }
            }
        }
        catch(NamingException e)
        {
            log.debug(JndiConnectionProvider.class.getName() + ".getAvailableDataSources()", e);
        }
        return result;
    }

    public ConnectionProviderEntries getDataSourceEntries(ValueContext vc)
    {
        ConnectionProviderEntries entries = new BasicConnectionProviderEntries();

        try
        {
            String envPath = getRootContextName();
            Context env = getRootContext();
            if(env != null)
            {
                for(NamingEnumeration e = env.list(""); e.hasMore();)
                {
                    NameClassPair entry = (NameClassPair) e.nextElement();
                    String dataSourceId = envPath != null ? (envPath + "/" + entry.getName()) : entry.getName();
                    try
                    {
                        DataSource source = (DataSource) env.lookup(entry.getName());
                        entries.add(getDataSourceEntry(dataSourceId, source));
                    }
                    catch(NamingException ex)
                    {
                        log.debug(JndiConnectionProvider.class.getName() + ".getDataSourceEntries()", ex);
                    }
                    catch(SQLException ex)
                    {
                        log.debug(JndiConnectionProvider.class.getName() + ".getDataSourceEntries()", ex);
                    }
                }
            }
        }
        catch(NamingException e)
        {
            log.error("Errorw in getDataSourceEntries()", e);
            throw new NestableRuntimeException(e);
        }

        return entries;
    }
}