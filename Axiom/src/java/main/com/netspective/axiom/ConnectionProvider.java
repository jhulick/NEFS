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
package com.netspective.axiom;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import javax.naming.NamingException;
import javax.sql.DataSource;

import com.netspective.commons.value.ValueContext;

public interface ConnectionProvider
{
    /**
     * Obtain the name of this connection provider (for identifying it versus other connectino providers)
     *
     * @return The name of the connection provider
     */
    public String getConnectionProviderName();

    /**
     * Given a valid data source identifier, return a database connection for the data source
     *
     * @param dataSourceId The data source identifier
     *
     * @return An open database connection for the given identifier
     */
    public Connection getConnection(ValueContext vc, String dataSourceId) throws NamingException, SQLException;

    /**
     * Given a valid data source identifier, return a database connection for the data source
     *
     * @param dataSourceId The data source identifier
     *
     * @return An open database connection for the given identifier
     */
    public DataSource getDataSource(ValueContext vc, String dataSourceId) throws NamingException, SQLException;

    /**
     * Get the class that actually implements the connection provider.
     *
     * @return The class that is actually providing the connections -- JNDI context, etc.
     */
    public Class getUnderlyingImplementationClass();

    /**
     * Retrieve a list of all data source identifiers being managed by this provider. Each item in the Set is a String
     * with the name/id of the datasource.
     *
     * @return List
     */
    public Set getAvailableDataSources();

    /**
     * Retrieve an informational list of all data sources being managed by this provider. This method is used to
     * catalog the available datasources and inquire about their details.
     *
     * @return ConnectionProviderEntries
     */
    public ConnectionProviderEntries getDataSourceEntries(ValueContext vc);

    /**
     * Retrieve an informational record of a particular data sources being managed by this provider. This method is used
     * to inquire about the details of a particular data source.
     *
     * @return ConnectionProviderEntry
     */
    public ConnectionProviderEntry getDataSourceEntry(ValueContext vc, String dataSourceId);
}
