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
package com.netspective.sparx.form.schema;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netspective.axiom.ConnectionContext;
import com.netspective.axiom.schema.ColumnValue;
import com.netspective.axiom.schema.ColumnValues;
import com.netspective.axiom.schema.PrimaryKeyColumnValues;
import com.netspective.axiom.schema.Row;
import com.netspective.sparx.form.DialogContext;

/**
 * Dialog context container class for the SchemaRecordEditorDialog
 */
public class SchemaRecordEditorDialogContext extends DialogContext
{
    private static final Log log = LogFactory.getLog(SchemaRecordEditorDialogContext.class);

    private ConnectionContext activeConnectionContext;
    private List rowsAdded = new ArrayList();
    private List rowsUpdated = new ArrayList();
    private List rowsDeleted = new ArrayList();

    public ConnectionContext getActiveConnectionContext()
    {
        return activeConnectionContext;
    }

    public void setActiveConnectionContext(ConnectionContext activeConnectionContext)
    {
        this.activeConnectionContext = activeConnectionContext;
    }

    /**
     * Gets a List object of recently added rows
     */
    public List getRowsAdded()
    {
        return rowsAdded;
    }

    /**
     * Gets a List object of recently updated rows
     */
    public List getRowsUpdated()
    {
        return rowsUpdated;
    }

    /**
     * Gets a List object of recently deleted rows
     */
    public List getRowsDeleted()
    {
        return rowsDeleted;
    }

    /**
     * Gets the primary key values of the row that was added
     *
     * @param rowNum Row number
     */
    public ColumnValues getAddedRowPrimaryKeyValues(int rowNum)
    {
        Row row = (Row) rowsAdded.get(rowNum);
        return row.getPrimaryKeyValues();
    }

    /**
     * Gets the primary key value of the row that was recently added. If there are more than
     * one primary key, the first primary key column is used.
     *
     * @return A NULL object is returned if there are not primary keys
     */
    public ColumnValue getAddedRowPrimaryKeyValue(int rowNum)
    {
        Row row = (Row) rowsAdded.get(rowNum);
        PrimaryKeyColumnValues primaryKeyValues = row.getPrimaryKeyValues();
        ColumnValue val = primaryKeyValues != null ? primaryKeyValues.getByColumnIndex(0) : null;
        return val;
    }
}
