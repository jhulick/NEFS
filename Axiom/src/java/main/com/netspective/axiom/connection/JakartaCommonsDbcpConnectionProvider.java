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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.netspective.axiom.ConnectionProvider;
import com.netspective.axiom.ConnectionProviderEntries;
import com.netspective.axiom.ConnectionProviderEntry;
import com.netspective.commons.value.ValueContext;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.value.source.StaticValueSource;
import com.netspective.commons.xdm.XdmBitmaskedFlagsAttribute;
import com.netspective.commons.xdm.XdmEnumeratedAttribute;
import com.netspective.commons.xdm.XmlDataModelSchema;

public class JakartaCommonsDbcpConnectionProvider implements ConnectionProvider
{
    private static final Log log = LogFactory.getLog(JakartaCommonsDbcpConnectionProvider.class);
    private Map dataSourcesInfo = Collections.synchronizedMap(new HashMap());
    private Map dataSources = Collections.synchronizedMap(new HashMap());
    private int dataSourceInfoIndex = 0;
    private String name = "jakarta-dbcp";

    public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);

    public static class DataSourceInfo
    {
        public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);

        private String name;
        private ValueSource driverClass = new StaticValueSource("");
        private ValueSource url = new StaticValueSource("");
        private ValueSource user = new StaticValueSource("");
        private ValueSource password = new StaticValueSource("");
        private GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        private TestOnActionsFlags testFlags = createTestFlags();

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public ValueSource getDriverClass()
        {
            return driverClass;
        }

        public void setDriverClass(ValueSource driverClass)
        {
            this.driverClass = driverClass;
        }

        public ValueSource getUrl()
        {
            return url;
        }

        public void setUrl(ValueSource url)
        {
            this.url = url;
        }

        public ValueSource getUser()
        {
            return user;
        }

        public void setUser(ValueSource user)
        {
            this.user = user;
        }

        public ValueSource getPassword()
        {
            return password;
        }

        public void setPassword(ValueSource password)
        {
            this.password = password;
        }

        public void setMaxConnections(int maxConns)
        {
            this.poolConfig.maxActive = maxConns;
        }

        public void setMaxIdleConnections(int maxIdleConns)
        {
            this.poolConfig.maxIdle = maxIdleConns;
        }

        public void setMaxWait(long maxWait)
        {
            this.poolConfig.maxWait = maxWait;
        }

        public void setTimeBetweenEvictions(long timeBetweenEvictions)
        {
            this.poolConfig.timeBetweenEvictionRunsMillis = timeBetweenEvictions;
        }

        public void setMinEvictableIdleTime(long minEvictableIdleTime)
        {
            this.poolConfig.minEvictableIdleTimeMillis = minEvictableIdleTime;
        }

        public void setNumTestsPerEviction(int numTestsPerEviction)
        {
            this.poolConfig.numTestsPerEvictionRun = numTestsPerEviction;
        }

        public GenericObjectPool.Config getPoolConfig()
        {
            return poolConfig;
        }

        //******************************************************************

        static public class TestOnActionsFlags extends XdmBitmaskedFlagsAttribute
        {
            public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);

            private static final int TEST_ON_BORROW = 1;
            private static final int TEST_ON_RETURN = TEST_ON_BORROW * 2;
            private static final int TEST_WHILE_IDLE = TEST_ON_RETURN * 2;

            private static FlagDefn[] FLAG_DEFNS = new FlagDefn[]
            {
                new FlagDefn(ACCESS_XDM, "TEST_ON_BORROW", TEST_ON_BORROW),
                new FlagDefn(ACCESS_XDM, "TEST_ON_RETURN", TEST_ON_RETURN),
                new FlagDefn(ACCESS_XDM, "TEST_WHILE_IDLE", TEST_WHILE_IDLE),
            };

            public FlagDefn[] getFlagsDefns()
            {
                return FLAG_DEFNS;
            }
        }

        static public class ExhaustedAction extends XdmEnumeratedAttribute
        {
            public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);

            private static final String[] VALUES = new String[]{
                "WHEN_EXHAUSTED_FAIL", "WHEN_EXHAUSTED_BLOCK", "WHEN_EXHAUSTED_GROW"
            };

            public String[] getValues()
            {
                return VALUES;
            }
        }

        public TestOnActionsFlags createTestFlags()
        {
            return new TestOnActionsFlags();
        }

        public void setTestFlags(TestOnActionsFlags testFlags)
        {
            this.testFlags.copy(testFlags);
            poolConfig.testOnBorrow = testFlags.flagIsSet(TestOnActionsFlags.TEST_ON_BORROW);
            poolConfig.testOnReturn = testFlags.flagIsSet(TestOnActionsFlags.TEST_ON_RETURN);
            poolConfig.testWhileIdle = testFlags.flagIsSet(TestOnActionsFlags.TEST_WHILE_IDLE);
        }

        public ExhaustedAction createExhaustedAction()
        {
            return new ExhaustedAction();
        }

        public void setExhaustedAction(ExhaustedAction exhaustedAction)
        {
            poolConfig.whenExhaustedAction = (byte) exhaustedAction.getValueIndex();
        }
    }

    public DataSourceInfo createDataSource()
    {
        return new DataSourceInfo();
    }

    public void addDataSource(DataSourceInfo dataSourceInfo)
    {
        if(dataSourceInfo.getName() == null || dataSourceInfo.getName().equals(""))
        {
            dataSourceInfo.setName("data-source-" + this.dataSourceInfoIndex);
        }
        this.dataSourceInfoIndex++;

        dataSourcesInfo.put(dataSourceInfo.getName(), dataSourceInfo);

        //TODO: Figure out how to set the default data source on SqlManager
        //if (dataSourceInfo.isDefault()) Do something

    }

    public class DbcpPoolingDataSource extends PoolingDataSource
    {
        public DbcpPoolingDataSource(ObjectPool objectPool)
        {
            super(objectPool);
        }

        public ObjectPool getPool()
        {
            return _pool;
        }
    }

    protected DataSource createDataSource(ValueContext vc, String dataSourceId) throws NamingException
    {

        DataSourceInfo dataSourceInfo = (DataSourceInfo) dataSourcesInfo.get(dataSourceId);

        if(dataSourceInfo == null)
            throw new NamingException("Data Source: '" + dataSourceId + "' not defined as a data source for Jakarta Commons DBCP provider.");

        String driverClassName = dataSourceInfo.driverClass.getTextValueOrBlank(vc);
        try
        {
            Class.forName(driverClassName);
        }
        catch(ClassNotFoundException cnfe)
        {
            log.error("Driver '" + driverClassName + "' not found for name '" + dataSourceId + "'");
            throw new NamingException("Driver '" + driverClassName + "' not found for name '" + dataSourceId + "'");
        }

        if(log.isDebugEnabled())
        {
            log.debug("Initializing data source: '" + dataSourceInfo.getName() + "'\n" +
                      "                  driver: '" + driverClassName + "'\n" +
                      "                     url: '" + dataSourceInfo.url.getTextValueOrBlank(vc) + "'\n" +
                      "                    user: '" + dataSourceInfo.user.getTextValueOrBlank(vc) + "'\n" +
                      "                password: '" + dataSourceInfo.password.getTextValueOrBlank(vc) + "'");
        }

        ObjectPool connectionPool = new GenericObjectPool(null, dataSourceInfo.getPoolConfig());
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(dataSourceInfo.url.getTextValueOrBlank(vc), dataSourceInfo.user.getTextValueOrBlank(vc), dataSourceInfo.password.getTextValueOrBlank(vc));
        try
        {
            //The reference to this object is not used within this method.  It's constuctor sets a reference of itself
            //in the conectionPool object we pass as a parameter.
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
        }
        catch(IllegalStateException e)
        {
            log.error("Trying to reset the pool factory for data source: '" + dataSourceInfo.name + "' when the pool objects are already in use, thus the pool is active.");
            return null;
        }

        catch(Exception e) //Generic Exception being caught here because Constructor of PoolableConnectionFactory is declared that way
        {
            log.error("An Exception was encountered when creating the pool factory in the Jakarta Commons DBCP framework.", e);
            return null;
        }

        DbcpPoolingDataSource dataSource = new DbcpPoolingDataSource(connectionPool);

        return dataSource;
    }

    /**
     * Returns the configured info object associated with a datasource. This will contain more information then what
     * a {@link javax.sql.DataSource} will reveal.
     *
     * @param dataSourceId datasource name
     *
     * @return DataSourceInfo object configured in the connection provider
     */
    public DataSourceInfo getDataSourceInfo(String dataSourceId)
    {
        return (DataSourceInfo) dataSourcesInfo.get(dataSourceId);
    }

    public DataSource getDataSource(ValueContext vc, String dataSourceId) throws NamingException
    {
        DataSource dataSource = (DataSource) dataSources.get(dataSourceId);
        if(dataSource == null)
        {
            dataSource = createDataSource(vc, dataSourceId);
            if(dataSource != null)
                dataSources.put(dataSourceId, dataSource);
        }
        return dataSource;
    }

    public String getConnectionProviderName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public final Connection getConnection(ValueContext vc, String dataSourceId) throws NamingException, SQLException
    {
        if(dataSourceId == null)
            throw new NamingException("name is NULL in " + this.getClass().getName() + ".getConnection(String)");

        DataSource source = getDataSource(vc, dataSourceId);

        if(source == null)
        {
            if(log.isDebugEnabled())
                log.debug("name not found in " + JakartaCommonsDbcpConnectionProvider.class.getName() + ".getConnection('" + dataSourceId + "'). Available: " + getAvailableDataSources());
            throw new NamingException("Data source '" + dataSourceId + "' not found in Jakarta Commons DBCP provider.");
        }

        return source.getConnection();
    }

    public ConnectionProviderEntry getDataSourceEntry(ValueContext vc, String dataSourceId)
    {
        try
        {
            DataSource source = getDataSource(vc, dataSourceId);
            if(source == null)
                return null;

            return getDataSourceEntry(dataSourceId, source);
        }
        catch(Exception ex)
        {
            log.error(JakartaCommonsDbcpConnectionProvider.class.getName() + ".getDataSourceEntry('" + dataSourceId + "')", ex);
            return null;
        }
    }

    public ConnectionProviderEntry getDataSourceEntry(String dataSourceId, DataSource source) throws SQLException
    {
        BasicConnectionProviderEntry result = new BasicConnectionProviderEntry();
        result.init(dataSourceId, source);
        return result;
    }

    public Set getAvailableDataSources()
    {
        return dataSourcesInfo.keySet();
    }

    public ConnectionProviderEntries getDataSourceEntries(ValueContext vc)
    {
        ConnectionProviderEntries entries = new BasicConnectionProviderEntries();
        Set available = getAvailableDataSources();
        for(Iterator i = available.iterator(); i.hasNext();)
        {

            ConnectionProviderEntry entry = getDataSourceEntry(vc, (String) i.next());
            if(entry != null)
                entries.add(entry);
        }
        return entries;
    }

    public Class getUnderlyingImplementationClass()
    {
        return DataSourceInfo.class;
    }
}
