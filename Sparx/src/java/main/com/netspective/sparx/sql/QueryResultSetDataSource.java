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
package com.netspective.sparx.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netspective.axiom.ConnectionContext;
import com.netspective.axiom.sql.QueryResultSet;
import com.netspective.commons.value.ValueSource;
import com.netspective.sparx.report.tabular.AbstractHtmlTabularReportDataSource;

public class QueryResultSetDataSource extends AbstractHtmlTabularReportDataSource
{
    private static final Log log = LogFactory.getLog(QueryResultSetDataSource.class);

    protected boolean cacheColumnData = true;
    protected boolean[] retrievedColumnData;
    protected Object[] cachedColumnData;
    protected int activeRowIndex = -1;
    protected QueryResultSet queryResultSet;
    protected ResultSet resultSet;
    protected boolean scrollable;
    protected ValueSource message;
    protected boolean calculatedTotalRows;
    protected int totalRows = TOTAL_ROWS_UNKNOWN;
    protected int selectedRowColumnSpecifier = -1;
    protected Object selectedRowColumnValue;
    protected boolean selectedRowCompareValueAsText;

    public QueryResultSetDataSource(ValueSource noDataMessage)
    {
        super();
        this.message = noDataMessage;
    }

    public boolean isCacheColumnData()
    {
        return cacheColumnData;
    }

    public void setCacheColumnData(boolean cacheColumnData)
    {
        this.cacheColumnData = cacheColumnData;
    }

    public void setSelectedRowRule(int selectedRowColumnSpecifier, String selectedRowColumnValue)
    {
        this.selectedRowColumnValue = selectedRowColumnValue;
        this.selectedRowColumnSpecifier = selectedRowColumnSpecifier;
        this.selectedRowCompareValueAsText = true;
    }

    public void setSelectedRowRule(int selectedRowColumnSpecifier, Object selectedRowColumnValue)
    {
        this.selectedRowColumnValue = selectedRowColumnValue;
        this.selectedRowColumnSpecifier = selectedRowColumnSpecifier;
        this.selectedRowCompareValueAsText = false;
    }

    public Object getSelectedRowColumnValue()
    {
        return selectedRowColumnValue;
    }

    public int getSelectedRowColumnSpecifier()
    {
        return selectedRowColumnSpecifier;
    }

    public boolean isSelectedRowCompareValueAsText()
    {
        return selectedRowCompareValueAsText;
    }

    public boolean isActiveRowSelected()
    {
        if(selectedRowColumnValue == null || selectedRowColumnSpecifier == -1)
            return false;
        else if(selectedRowCompareValueAsText)
            return selectedRowColumnValue.equals(getActiveRowColumnData(selectedRowColumnSpecifier, 0).toString());
        else
            return (selectedRowColumnValue.equals(getActiveRowColumnData(selectedRowColumnSpecifier, 0)));
    }

    public void close()
    {
        if(!isClosed())
        {
            super.close();
            try
            {
                queryResultSet.close(true);
            }
            catch(SQLException e)
            {
                log.error("Unable to close result set", e);
                throw new NestableRuntimeException(e);
            }
            finally
            {
                // failing to close result set would mean the connection wasn't closed
                try
                {
                    // if the result set was closed properly, then the connection context would be NULL
                    if(queryResultSet.getConnectionContext() != null)
                    {
                        ConnectionContext cc = queryResultSet.getConnectionContext();
                        cc.getDatabaseValueContext().returnConnection(cc);
                    }
                }
                catch(SQLException e)
                {
                    log.error("Unable to close connection", e);
                }
            }
        }
    }

    public QueryResultSet getQueryResultSet()
    {
        return queryResultSet;
    }

    public void setQueryResultSet(QueryResultSet queryResultSet)
    {
        this.queryResultSet = queryResultSet;
        setResultSet(queryResultSet.getResultSet());
    }

    protected void setResultSet(ResultSet resultSet)
    {
        this.resultSet = queryResultSet.getResultSet();

        try
        {
            if(cacheColumnData)
            {
                final ResultSetMetaData metaData = resultSet.getMetaData();
                retrievedColumnData = new boolean[metaData.getColumnCount()];
                cachedColumnData = new Object[metaData.getColumnCount()];
            }
            scrollable = resultSet.getType() != ResultSet.TYPE_FORWARD_ONLY ? true : false;
        }
        catch(SQLException e)
        {
            log.error("Unable to set result set", e);
            throw new NestableRuntimeException(e);
        }

        activeRowIndex = -1;
        calculatedTotalRows = false;
    }

    public Object getActiveRowColumnData(int columnIndex, int flags)
    {
        try
        {
            if(cacheColumnData)
            {
                if(retrievedColumnData[columnIndex])
                    return cachedColumnData[columnIndex];

                cachedColumnData[columnIndex] = resultSet.getObject(columnIndex + 1);
                retrievedColumnData[columnIndex] = true;
                return cachedColumnData[columnIndex];
            }
            else
                return resultSet.getObject(columnIndex + 1);
        }
        catch(SQLException e)
        {
            log.error("Unable to retrieve column data: columnIndex = " + columnIndex + ", row = " + activeRowIndex + ", flags = " + flags, e);
            return e.toString();
        }
    }

    /**
     * Calculates the total number of rows in the result set by moving the cursor to the last row and getting
     * the row number. It then returns the cursor to the original row position.
     */
    public int getTotalRows()
    {
        if(calculatedTotalRows)
            return totalRows;

        try
        {
            if(scrollable)
            {
                // get the current row number
                int currentRow = resultSet.getRow();
                resultSet.last();
                totalRows = resultSet.getRow();
                if(currentRow == 0)
                {
                    // there is no current row so THE DEFAULT BEHAVIOR is to send the cursor to before the first row
                    resultSet.beforeFirst();
                }
                else
                {
                    // set the cusor back to the original row
                    resultSet.absolute(currentRow);
                }
            }
            else
            {
                log.error("The result set cursor may move only forward, thus unable to obtain total rows.");
            }
        }
        catch(SQLException e)
        {
            log.error("Unable to get total rows", e);
            throw new NestableRuntimeException(e);
        }

        calculatedTotalRows = true;
        return totalRows;
    }

    public boolean hasMoreRows()
    {
        try
        {
            return !resultSet.isAfterLast();
        }
        catch(SQLException e)
        {
            log.error("Unable to check if more rows are available", e);
            throw new NestableRuntimeException(e);
        }
    }

    public boolean isScrollable()
    {
        return scrollable;
    }

    public void setActiveRow(int rowNum)
    {

        try
        {
            if(scrollable)
            {
                resultSet.absolute(rowNum);
                resultSet.previous();
            }
            else
            {
                queryResultSet.reExecute();
                setResultSet(queryResultSet.getResultSet());

                if(rowNum > 0)
                {
                    int atRow = 0;
                    while(resultSet.next())
                    {
                        if(atRow >= rowNum)
                            break;

                        atRow++;
                    }
                }
            }
            activeRowIndex = rowNum;
        }
        catch(SQLException e)
        {
            log.error("Unable to set active row", e);
            throw new NestableRuntimeException(e);
        }
    }

    public boolean next()
    {
        try
        {
            if(cacheColumnData)
            {
                for(int i = 0; i < retrievedColumnData.length; i++)
                    retrievedColumnData[i] = false;
            }

            if(resultSet.next())
            {
                activeRowIndex++;
                return true;
            }
        }
        catch(SQLException e)
        {
            log.error("Unable to move to next row", e);
            throw new NestableRuntimeException(e);
        }

        return false;
    }

    public int getActiveRowNumber()
    {
        return activeRowIndex + 1;
    }

    public ValueSource getNoDataFoundMessage()
    {
        return message;
    }
}
