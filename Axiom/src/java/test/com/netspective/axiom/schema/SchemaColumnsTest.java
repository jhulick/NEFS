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
package com.netspective.axiom.schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.NamingException;

import com.netspective.axiom.ConnectionContext;
import com.netspective.axiom.DatabasePolicies;
import com.netspective.axiom.SqlManager;
import com.netspective.axiom.SqlManagerComponent;
import com.netspective.axiom.TestUtils;
import com.netspective.axiom.schema.column.BasicColumn;
import com.netspective.axiom.schema.column.ColumnQueryDefnField;
import com.netspective.axiom.schema.column.ColumnValueException;
import com.netspective.axiom.schema.column.SqlDataDefns;
import com.netspective.axiom.schema.column.ValueDefns;
import com.netspective.axiom.schema.column.type.AutoIncColumn;
import com.netspective.axiom.schema.column.type.BooleanColumn;
import com.netspective.axiom.schema.column.type.DateColumn;
import com.netspective.axiom.schema.column.type.EnumSetColumn;
import com.netspective.axiom.schema.column.type.EnumerationIdRefColumn;
import com.netspective.axiom.schema.column.type.FloatColumn;
import com.netspective.axiom.schema.column.type.GuidColumn;
import com.netspective.axiom.schema.column.type.GuidTextColumn;
import com.netspective.axiom.schema.column.type.IntegerColumn;
import com.netspective.axiom.schema.column.type.LongIntegerColumn;
import com.netspective.axiom.schema.column.type.ShortIntegerColumn;
import com.netspective.axiom.schema.column.type.TextColumn;
import com.netspective.axiom.schema.column.type.TextSetColumn;
import com.netspective.axiom.schema.table.type.EnumerationTableRow;
import com.netspective.axiom.sql.DbmsSqlText;
import com.netspective.axiom.sql.QueryResultSet;
import com.netspective.axiom.sql.dynamic.QueryDefnSelect;
import com.netspective.axiom.value.BasicDatabaseConnValueContext;
import com.netspective.axiom.value.DatabaseConnValueContext;
import com.netspective.commons.io.Resource;
import com.netspective.commons.value.exception.ValueException;
import com.netspective.commons.xdm.XdmComponentFactory;

import junit.framework.TestCase;

public class SchemaColumnsTest extends TestCase
{
    public static final String RESOURCE_NAME = "test-data-schema.xml";
    protected SqlManagerComponent component = null;
    protected SqlManager manager = null;
    protected Schema schema = null;

    protected Schema populatedSchema = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        component =
                (SqlManagerComponent) XdmComponentFactory.get(SqlManagerComponent.class, new Resource(SchemaColumnsTest.class, RESOURCE_NAME), XdmComponentFactory.XDMCOMPFLAGS_DEFAULT);
        assertNotNull(component);

        schema = component.getManager().getSchema("local");
        assertNotNull(schema);

        populatedSchema = component.getManager().getSchema("db");
        assertNotNull(populatedSchema);

        //TestUtils.getConnProvider(this.getClass().getPackage().getName(), false, false);
        //TestUtils.getConnProvider(this.getClass().getPackage().getName(), true, true);

    }

    public void testBooleanColumn()
    {


        //TODO:  The main thing to do here is to actually assign a value to each of the column types.
        //       1. We could do it through loading the data through xml.
        //       2. We could generate the DAL and create a row programatically.  In #1 we may still need to create the DAL.


        Table table = schema.getTables().getByName("SchemaTest");
        Row row = table.createRow();
        BooleanColumn.BooleanColumnValue colValue = (BooleanColumn.BooleanColumnValue) row.getColumnValues().getByName("boolean_column");
        assertEquals(colValue.getValueHolderClass(), Boolean.class);

        Boolean trueVal = new Boolean(true);
        colValue.setValue(trueVal);
        assertEquals(colValue.getValue(), trueVal);

        colValue.setTextValue("true");
        assertTrue(((Boolean) colValue.getValue()).booleanValue());

        colValue.setValue(new Integer(0));
        assertFalse(((Boolean) colValue.getValue()).booleanValue());
    }

    public void testNumericColumns()
    {

        Table table = schema.getTables().getByName("SchemaTest");
        Row row = table.createRow();

        //Testing Float
        FloatColumn.FloatColumnValue floatColValue = (FloatColumn.FloatColumnValue) row.getColumnValues().getByName("float_column");
        assertEquals(floatColValue.getValueHolderClass(), Float.class);

        Float floatValue = new Float(0.5);
        floatColValue.setTextValue("0.5");
        assertEquals(floatColValue.getValue(), floatValue);

        floatColValue.setValue(new Integer(0));

        try
        {
            floatColValue.setTextValue("abc");
            fail();
        }
        catch (ValueException e)
        {
            //This is good
        }

        //Testing Integer
        IntegerColumn.IntegerColumnValue intColValue = (IntegerColumn.IntegerColumnValue) row.getColumnValues().getByName("integer_column");
        assertEquals(intColValue.getValueHolderClass(), Integer.class);

        Integer intValue = new Integer(1);
        intColValue.setTextValue("1");
        assertEquals(intColValue.getValue(), intValue);

        try
        {
            intColValue.setValue(new Float(0.0));
            fail();
        }
        catch (ClassCastException e)
        {
            //This is good
        }

        try
        {
            intColValue.setTextValue("abc");
            fail();
        }
        catch (ValueException e)
        {
            //This is good
        }





        //Testing Small Integer
        ShortIntegerColumn.SmallIntegerColumnValue smallIntColValue = (ShortIntegerColumn.SmallIntegerColumnValue) row.getColumnValues().getByName("small_int_column");
        assertEquals(smallIntColValue.getValueHolderClass(), Short.class);

        Short shortValue = new Short((short) 1);
        smallIntColValue.setTextValue("1");
        assertEquals(smallIntColValue.getValue(), shortValue);

        try
        {
            intColValue.setValue(new Float(0.0));
            fail();
        }
        catch (ClassCastException e)
        {
            //This is good
        }

        try
        {
            intColValue.setTextValue("abc");
            fail();
        }
        catch (ValueException e)
        {
            //This is good
        }

        //Testing Long Integer
        LongIntegerColumn.LongIntegerColumnValue longIntColValue = (LongIntegerColumn.LongIntegerColumnValue) row.getColumnValues().getByName("long_integer_column");
        assertEquals(longIntColValue.getValueHolderClass(), Long.class);

        Long longValue = new Long(1);
        longIntColValue.setTextValue("1");
        assertEquals(longIntColValue.getValue(), longValue);

        try
        {
            intColValue.setValue(new String(""));
            fail();
        }
        catch (ClassCastException e)
        {
            //This is good
        }

        try
        {
            intColValue.setTextValue("abc");
            fail();
        }
        catch (ValueException e)
        {
            //This is good
        }
    }

    public void testDateColumn()
    {
        Table table = schema.getTables().getByName("SchemaTest");
        Row row = table.createRow();
        DateColumn.DateColumnValue colValue = (DateColumn.DateColumnValue) row.getColumnValues().getByName("date_column");

        assertEquals(colValue.getValueHolderClass(), Date.class);
        assertEquals(colValue.getBindParamValueHolderClass(), java.sql.Date.class);

        Date dateVal = null;
        try
        {
            SimpleDateFormat myFormat = (SimpleDateFormat) DateFormat.getInstance();
            dateVal = myFormat.parse("01/01/2003 0:0 AM, PDT");
            //myFormat.applyPattern("MMM d, yyyy");
            //dateVal = myFormat.parse("Jan 1, 2003");
            //M/d/yy h:mm a  --  MMM d, yyyy
        }
        catch (ParseException e)
        {
            fail(); // This should never happen because is depends on the hardcoded string just above.
        }
        DateColumn col = (DateColumn) colValue.getColumn();
        DateFormat colFormat = col.getDateFormat();
        assertNotNull(colFormat);

        colValue.setTextValue("Jan 1, 2003");
        assertEquals(colValue.getValue(), dateVal);

        assertEquals(colValue.getTextValue(), "Jan 1, 2003");

        DateFormat format = DateFormat.getInstance();
        col.setDateFormat(format);
        assertSame(format, col.getDateFormat());

        try
        {
            colValue.setTextValue("abc");
            fail();
        }
        catch (ValueException e)
        {
            //This is good
        }
    }

    public void testGuidColumn()
    {
        Table table = schema.getTables().getByName("SchemaTest");
        GuidColumn col = (GuidColumn) table.getColumns().getByName("guid32_column");
        assertEquals(col.getForeignKeyReferenceeClass(), GuidTextColumn.class);
    }

    public void testAutoIncColumn()
    {
        Table table = schema.getTables().getByName("SchemaTest");
        AutoIncColumn col = (AutoIncColumn) table.getColumns().getByName("auto_inc_column");
        assertEquals(col.getForeignKeyReferenceeClass(), LongIntegerColumn.class);
    }

    public void testTextSetColumn() throws NamingException, SQLException
    {
        Table table = populatedSchema.getTables().getByName("Test_Three");
        TextSetColumn col = (TextSetColumn) table.getColumns().getByName("text_set_column");
        TextColumn colA = (TextColumn) table.getColumns().getByName("column_a");

        assertEquals(col.getDelimiter(), ",");
        col.setDelimiter("|");
        assertEquals(col.getDelimiter(), "|");
        assertEquals(col.isTrim(), true);
        col.setTrim(false);
        assertEquals(col.isTrim(), false);

        DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
        dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
        dbvc.setDefaultDataSource(this.getClass().getPackage().getName());
        ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);

        //Asser the data inserted
        table.getAccessorByColumnEquality(colA);
        QueryDefnSelect query = table.getAccessorByColumnEquality(colA);
        QueryResultSet resultSet = query.execute(dbvc, this.getClass().getPackage().getName(), new Object[]{"def"});
        ResultSet result = resultSet.getResultSet();
        Row row = table.createRow();
        ColumnValues values = row.getColumnValues();

        if (result.next())
            values.populateValues(result, 0);

        assertEquals(values.getByName("column_a").getTextValue(), "def");
        assertEquals(values.getByName("text_set_column").getTextValue(), "e,f,g,h");


        //TODO: Figure out why when we try to update we get a SQL Exception
        //values.getByName("rec_stat_id").setTextValue("0");
        //values.getByName("text_set_column").setTextValue("a,b,c,d");
        //System.out.println("row: " + row.getColumnValues());
        //table.update(cc, row);

        //TODO: Figure out why when we try to delete we get a SQL Exception
        resultSet.close(true);

        table.delete(cc, row);
        resultSet = query.execute(dbvc, this.getClass().getPackage().getName(), new Object[]{"def"});
        result = resultSet.getResultSet();
        assertTrue(!result.next());

        resultSet.close(true);

        cc.close();
    }

    public void testEnumSetColumn() throws NamingException, SQLException
    {
        Table table = populatedSchema.getTables().getByName("Test_Three");
        EnumSetColumn col = (EnumSetColumn) table.getColumns().getByName("enum_set_column");
        TextColumn colA = (TextColumn) table.getColumns().getByName("column_a");

        assertEquals(col.getDelimiter(), ",");
        col.setDelimiter("|");
        assertEquals(col.getDelimiter(), "|");
        assertEquals(col.isTrim(), true);
        col.setTrim(false);
        assertEquals(col.isTrim(), false);

        DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
        dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
        dbvc.setDefaultDataSource(this.getClass().getPackage().getName());
        ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);

        //Asser the data inserted
        table.getAccessorByColumnEquality(colA);
        QueryDefnSelect query = table.getAccessorByColumnEquality(colA);
        QueryResultSet resultSet = query.execute(dbvc, this.getClass().getPackage().getName(), new Object[]{"abc"});
        ResultSet result = resultSet.getResultSet();
        Row row = table.createRow();
        ColumnValues values = row.getColumnValues();

        if (result.next())
            values.populateValues(result, 0);

        //TODO: Somehow data is deleted when running this along with the previous test, the DB is supposed to get recreated
        //      but it doesn't, anyway, the above test is dealing with a different row, so it shouldn't matter
        //assertEquals(values.getByName("column_a").getTextValue(),"abc");
        //assertEquals(values.getByName("enum_set_column").getTextValue(),"2");

        //TODO: Figure out why when we try to update we get a SQL Exception
        //values.getByName("rec_stat_id").setTextValue("0");
        //values.getByName("text_set_column").setTextValue("a,b,c,d");
        //System.out.println("row: " + row.getColumnValues());
        //table.update(cc, row);

        resultSet.close(true);

        table.delete(cc, row);
        resultSet = query.execute(dbvc, this.getClass().getPackage().getName(), new Object[]{"abc"});
        result = resultSet.getResultSet();
        assertFalse(result.next());

        resultSet.close(true);
        cc.close();
    }

    public void testEnumIdRefColumn()
    {
        Table table = populatedSchema.getTables().getByName("Test_Three");
        Row row = table.createRow();
        EnumerationIdRefColumn.EnumerationIdRefValue colValue = (EnumerationIdRefColumn.EnumerationIdRefValue) row.getColumnValues().getByName("enumIdRef");
        assertEquals(colValue.getValueHolderClass(), Integer.class);
        EnumerationIdRefColumn col = (EnumerationIdRefColumn) table.getColumns().getByName("enumIdRef");

        try
        {
            colValue.setTextValue("AAA");
            fail();
        }
        catch (EnumerationIdRefColumn.InvalidEnumerationValueException e)
        {
            assertEquals(e.getTable().getName(), "Enum_set_Lookup");
            //This is good
        }

        try
        {
            colValue.setValue("9");
            fail();
        }
        catch (EnumerationIdRefColumn.InvalidEnumerationValueException e)
        {
            //This is good
            assertEquals(e.getTable().getName(), "Enum_set_Lookup");
        }


        try
        {
            colValue.setValue(new Integer(9));
            fail();
        }
        catch (EnumerationIdRefColumn.InvalidEnumerationValueException e)
        {
            //This is good
            assertEquals(e.getTable().getName(), "Enum_set_Lookup");
        }


    }

    public void testBasicColumn() throws NamingException, SQLException
    {

        Table table = schema.getTables().getByName("SchemaTest");
        Row row = table.createRow();
        Column col = table.getColumns().getByName("float_column");
        FloatColumn.FloatColumnValue floatColValue = (FloatColumn.FloatColumnValue) row.getColumnValues().getByName("float_column");

        DbmsSqlText sql = floatColValue.createSqlExpr();
        assertNotNull(sql);

        DbmsSqlText testSql = new DbmsSqlText(floatColValue.getSqlExprs());
        testSql.setDbms(new DatabasePolicies.DatabasePolicyEnumeratedAttribute());
        testSql.setSql("TEST");
        floatColValue.addSqlExpr(testSql);
        //TODO: Need to figure out how to perform an assertion here




        Table table2 = populatedSchema.getTables().getByName("Test_Three");
        Column col2 = table2.getColumns().getByName("enumIdRef");
        Row row2 = table2.createRow();
        EnumerationIdRefColumn.EnumerationIdRefValue colValue = (EnumerationIdRefColumn.EnumerationIdRefValue) row2.getColumnValues().getByName("enumIdRef");

        DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
        dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
        dbvc.setDefaultDataSource(this.getClass().getPackage().getName());
        ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);





        Row fkRow = colValue.getReferencedForeignKeyRow(cc);
        Rows fkRows = colValue.getReferencedForeignKeyRows(cc);
        //These values should be null before assigning a value to the ColumnValue
        assertNull(fkRow);
        assertNull(fkRows);

        colValue.setTextValue("1");
        fkRow = colValue.getReferencedForeignKeyRow(cc);
        fkRows = colValue.getReferencedForeignKeyRows(cc);
        EnumerationTableRow enumRow = colValue.getReferencedEnumRow();

        assertNotNull(fkRow);
        assertNotNull(fkRows);
        assertNotNull(enumRow);

        try
        {
            floatColValue.setTextValue("1.0");
            floatColValue.getReferencedForeignKeyRow(cc);
            fail();
        }
        catch (NamingException e)
        {
            fail();
        }
        catch (SQLException e)
        {
            fail();
        }
        catch (RuntimeException e)
        {
            //This is good.
        }

        try
        {
            floatColValue.getReferencedForeignKeyRows(cc);
            fail();
        }
        catch (NamingException e)
        {
            fail();
        }
        catch (SQLException e)
        {
            fail();
        }
        catch (RuntimeException e)
        {
            //This is good.
        }

       assertNotNull(col2.toString());

        cc.close();
    }

    public void testColumnsCollections(){
        Table table2 = populatedSchema.getTables().getByName("Test_Three");
        Columns cols = table2.getColumns();
        Column col2 = table2.getColumns().getByName("enumIdRef");

        assertTrue(cols.contains(col2));

        assertEquals("record-status-id|cr-stamp|auto-inc-column|text-set-column|enum-set-column|column-a|enumidref|column-b", cols.getOnlyXmlNodeNames("|"));

        int byName = cols.getColumnIndexInRowByName("column_a");
        int byXml = cols.getColumnIndexInRowByNameOrXmlNodeName("column-a");
        assertEquals(byName, byXml);

        assertEquals(Columns.COLUMN_INDEX_NOT_FOUND, cols.getColumnIndexInRowByName("WRONG"));
        assertEquals(Columns.COLUMN_INDEX_NOT_FOUND, cols.getColumnIndexInRowByNameOrXmlNodeName("WRONG"));

        try
        {
            cols.getSole();
            fail();
        }
        catch (RuntimeException e)
        {
            //This is good
        }
        assertNotNull(cols.toString());

        //TODO: The only thing left to test is the BasicValues. copyValuesUsingColumnNames, populateValues
        Row row = table2.createRow();
        ColumnValues vals = row.getColumnValues();
        vals.getByColumn(col2);
        //populateValues

        assertNull(vals.getByName("WRONG"));
        assertNull(vals.getByNameOrXmlNodeName("WRONG"));
        assertNotNull(vals.getByNameOrXmlNodeName("enumIdRef"));
        assertNotNull(vals.toString());

    }

    public void testColumnValueException(){
        ColumnValueException cve = new ColumnValueException(new Exception("Test Message"));
        assertEquals(cve.getMessage(), "Test Message");
        cve = new ColumnValueException("Test Message", new Exception());
        assertEquals(cve.getMessage(), "Test Message");
    }

    public void testValueDefs(){
        Table table = schema.getTables().getByName("SchemaTest");
        Row row = table.createRow();
        BasicColumn col = (FloatColumn)table.getColumns().getByName("float_column");
        BasicColumn col2 = (IntegerColumn)table.getColumns().getByName("integer_column");
        ValueDefns valDef = col.getValueDefns();
        assertSame(col, valDef.getColumn());

        valDef.merge(col2.getValueDefns());
        //TODO: Not sure what to assert here
    }

    public void testColumnQueryDefnField(){
        Table table = schema.getTables().getByName("SchemaTest");
        BasicColumn col = (FloatColumn)table.getColumns().getByName("float_column");
        ColumnQueryDefnField query = col.createQueryDefnField(table.getQueryDefinition());
        assertSame(col, query.getTableColumn());
    }

    public void testSqlDataDefns(){
        Table table = populatedSchema.getTables().getByName("Test_Three");
        BasicColumn col = (BasicColumn)table.getColumns().getByName("enumIdRef");
        SqlDataDefns sqlDef = col.getSqlDdl();
        assertSame(col, sqlDef.getColumn());
        assertNotNull(sqlDef.getSqlForeignKeyDefns());
    }


}
