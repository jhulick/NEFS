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
package com.netspective.axiom.schema.column.type;

import java.sql.SQLException;

import com.netspective.axiom.ConnectionContext;
import com.netspective.axiom.DatabasePolicy;
import com.netspective.axiom.schema.Column;
import com.netspective.axiom.schema.ColumnValue;
import com.netspective.axiom.schema.ColumnValues;
import com.netspective.axiom.schema.Row;
import com.netspective.axiom.schema.Table;
import com.netspective.axiom.schema.table.type.EnumerationTable;
import com.netspective.axiom.schema.table.type.EnumerationTableRows;
import com.netspective.commons.text.TextUtils;

public class EnumSetColumn extends TextColumn implements DatabasePolicy.ColumnInsertListener, DatabasePolicy.ColumnUpdateListener, DatabasePolicy.ColumnDeleteListener
{
    static public final int COLINDEX_SYSTEM_ID = 0;
    static public final int COLINDEX_PARENT_ID = 1;
    static public final int COLINDEX_ENUM_INDEX = 2;
    static public final int COLINDEX_ENUM_VALUE = 3;

    private String delimiter = ",";
    private boolean trim = true;

    public EnumSetColumn(Table table)
    {
        super(table);
    }

    public String getDelimiter()
    {
        return delimiter;
    }

    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    public boolean isTrim()
    {
        return trim;
    }

    public void setTrim(boolean trim)
    {
        this.trim = trim;
    }

    public void afterDelete(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
    {
    }

    public void afterInsert(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
    {
        String[] values = columnValue.isListValue()
                          ?
                          columnValue.getTextValues()
                          : TextUtils.getInstance().split(columnValue.getTextValue(), delimiter, true);
        if(values == null)
            return;

        Column primaryKeyColumn = getTable().getPrimaryKeyColumns().getSole();
        Table setTable = getColumnTables().getSole();
        EnumerationTable enumTable = (EnumerationTable) setTable.getColumns().get(COLINDEX_ENUM_VALUE).getForeignKey().getReferencedColumns().getFirst().getTable();
        EnumerationTableRows enumRows = enumTable.getEnums();

        for(int i = 0; i < values.length; i++)
        {
            Row setRow = setTable.createRow();
            ColumnValues setRowColValues = setRow.getColumnValues();

            setRowColValues.getByColumnIndex(COLINDEX_PARENT_ID).setValue(columnValues.getByColumn(primaryKeyColumn));
            setRowColValues.getByColumnIndex(COLINDEX_ENUM_INDEX).setValue(new Integer(i));
            setRowColValues.getByColumnIndex(COLINDEX_ENUM_VALUE).setValue(enumRows.getByIdOrCaptionOrAbbrev(values[i]).getIdAsInteger());

            setTable.insert(cc, setRow);
        }
    }

    public void afterUpdate(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
    {
        // delete all the existing entries and reinsert them
        beforeDelete(cc, flags, columnValue, columnValues);
        afterInsert(cc, flags, columnValue, columnValues);
    }

    public void beforeDelete(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
    {
        Column primaryKeyColumn = getTable().getPrimaryKeyColumns().getSole();
        Object parentId = columnValues.getByColumn(primaryKeyColumn).getValue();
        if(parentId == null)
            return;

        Table setTable = getColumnTables().getSole();
        setTable.delete(cc, setTable.createRow(), "parent_id = ?", new Object[]{parentId});
    }

    public void beforeInsert(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
    {
    }

    public void beforeUpdate(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
    {
    }
}
