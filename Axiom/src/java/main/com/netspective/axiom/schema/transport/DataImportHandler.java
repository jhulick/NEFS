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
package com.netspective.axiom.schema.transport;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.netspective.axiom.ConnectionContext;
import com.netspective.axiom.DatabasePolicies;
import com.netspective.axiom.schema.Column;
import com.netspective.axiom.schema.ColumnValue;
import com.netspective.axiom.schema.ColumnValues;
import com.netspective.axiom.schema.PrimaryKeyColumnValues;
import com.netspective.axiom.schema.Row;
import com.netspective.axiom.schema.Schema;
import com.netspective.axiom.schema.Table;
import com.netspective.axiom.schema.Tables;
import com.netspective.axiom.schema.constraint.ParentForeignKey;
import com.netspective.axiom.sql.DbmsSqlText;
import com.netspective.commons.io.Resource;
import com.netspective.commons.text.DelimitedValuesParser;
import com.netspective.commons.text.TextUtils;
import com.netspective.commons.validate.ValidationContext;
import com.netspective.commons.xdm.XdmEnumeratedAttribute;
import com.netspective.commons.xml.AbstractContentHandler;
import com.netspective.commons.xml.ContentHandlerException;
import com.netspective.commons.xml.ContentHandlerNodeStackEntry;
import com.netspective.commons.xml.ParseContext;
import com.netspective.commons.xml.template.Template;
import com.netspective.commons.xml.template.TemplateApplyContext;

public class DataImportHandler extends AbstractContentHandler
{
    static public final String ATTRNAME_SQL_EXPR = "dal:sql-expr";
    static public final String ATTRNAME_CSV_FILE = "dal:csv-file";
    static public final String ATTRNAME_CSV = "dal:csv";
    static public final String ATTRNAME_CSV_BLANK_IS_NULL = "dal:csv-blank-is-null";
    static public final String ATTRNAME_STORE_ID = "ID";
    static public final String ATTRNAME_RETRIEVE_ID = "IDREF";
    public static final String ATTRVALUE_RETRIEVE_ID = "IDREF:";

    static public final Set SPECIAL_ATTRIBUTES = new HashSet();

    static
    {
        SPECIAL_ATTRIBUTES.add(ATTRNAME_SQL_EXPR);
        SPECIAL_ATTRIBUTES.add(ATTRNAME_CSV);
        SPECIAL_ATTRIBUTES.add(ATTRNAME_CSV_FILE);
        SPECIAL_ATTRIBUTES.add(ATTRNAME_CSV_BLANK_IS_NULL);
        SPECIAL_ATTRIBUTES.add(ATTRNAME_SQL_EXPR);
        SPECIAL_ATTRIBUTES.add(ATTRNAME_STORE_ID);
        SPECIAL_ATTRIBUTES.add(ATTRNAME_RETRIEVE_ID);
    }

    private class NodeStackEntry implements ContentHandlerNodeStackEntry
    {
        private String qName;
        private int depth;
        private Row row;
        private String rowColumnName;
        private boolean isSqlExpr;
        private String sqlExprDbmsId;
        private boolean written;
        private String storeId;
        private PrimaryKeyColumnValues idRefValues;

        public NodeStackEntry(String qName, int depth)
        {
            this.qName = qName;
            this.depth = depth;
        }

        public NodeStackEntry(String qName, Row row, int depth)
        {
            this(qName, depth);
            this.row = row;
        }

        public NodeStackEntry(String qName, Row row, String activeRowColName, int depth)
        {
            this(qName, row, depth);
            this.rowColumnName = activeRowColName;
        }

        public Object getResourceIncludeRelativeTo()
        {
            return this.getClass().getClassLoader();
        }

        public void handleAttributes(Attributes attributes, boolean allowColumnAssignments) throws ParseException
        {
            if(allowColumnAssignments && row != null)
            {
                for(int i = 0; i < attributes.getLength(); i++)
                {
                    String attrName = attributes.getQName(i);
                    if(!SPECIAL_ATTRIBUTES.contains(attrName))
                    {
                        Column column = row.getTable().getColumns().getByNameOrXmlNodeName(attrName);
                        if(column != null)
                        {
                            String value = attributes.getValue(i);
                            if(value.startsWith(ATTRVALUE_RETRIEVE_ID))
                            {
                                String idRef = value.substring(ATTRVALUE_RETRIEVE_ID.length());
                                if(idRef != null)
                                {
                                    idRefValues = (PrimaryKeyColumnValues) idReferences.get(idRef);
                                    if(idRefValues != null)
                                        row.getColumnValues().getByColumn(column).setValue(idRefValues.getByColumnIndex(0));
                                }
                                else
                                {
                                    getParseContext().addError("IDREF '" + idRef + "' not found in table '" + row.getTable().getName() + "'. Available: " + idReferences.keySet() + " ");
                                }
                            }
                            else
                            {
                                row.getColumnValues().getByColumn(column).setTextValue(value);
                            }
                        }
                        else
                            getParseContext().addError("Column '" + attrName + "' not found for attribute in table '" + row.getTable().getName() + "'");
                    }
                }
            }

            String sqlExprAttrValue = attributes.getValue(ATTRNAME_SQL_EXPR);
            if(sqlExprAttrValue != null)
            {
                XdmEnumeratedAttribute dpea = DatabasePolicies.getInstance().getEnumeratedAttribute();
                if(dpea.containsValue(sqlExprAttrValue))
                {
                    isSqlExpr = true;
                    sqlExprDbmsId = sqlExprAttrValue;
                }
                else
                    getParseContext().addError("DBMS id '" + sqlExprAttrValue + "' is not valid in " + ATTRNAME_SQL_EXPR + "'" + row.getTable().getName() + "'");
            }

            String storeId = attributes.getValue(ATTRNAME_STORE_ID);
            if(storeId != null && storeId.length() > 0)
                setStoreId(storeId);

            String idRef = attributes.getValue(ATTRNAME_RETRIEVE_ID);
            if(idRef != null)
            {
                idRefValues = (PrimaryKeyColumnValues) idReferences.get(idRef);
                if(idRefValues != null)
                    row.getColumnValues().copyValuesUsingColumnNames(idRefValues);
                else
                    getParseContext().addError("IDREF '" + idRef + "' not found in table '" + row.getTable().getName() + "'. Available: " + idReferences.keySet() + " ");
            }

            String csv = attributes.getValue(ATTRNAME_CSV);
            if(csv != null)
            {
                if(row != null)
                {
                    row.getColumnValues().setValues(new DelimitedValuesParser().parse(csv),
                                                    TextUtils.getInstance().toBoolean(attributes.getValue(ATTRNAME_CSV_BLANK_IS_NULL), true));
                }
                else
                    getParseContext().addError("Trying to assign CSV value '" + csv + "' but there is no active row.");
            }
        }

        public String getQName()
        {
            return qName;
        }

        public boolean isColumnEntry()
        {
            return rowColumnName != null ? true : false;
        }

        public boolean isSqlExpression()
        {
            return isSqlExpr;
        }

        public String getStoreId()
        {
            return storeId;
        }

        public void setStoreId(String storeId)
        {
            this.storeId = storeId;
        }

        public void write() throws NamingException, SQLException
        {
            if(!written && row != null)
            {
                ValidationContext vc = row.getValidationResult(null);

                if(vc.isValid())
                {
                    written = true;
                    Table table = row.getTable();
                    long startTime = System.currentTimeMillis();
                    long endTime;
                    try
                    {
                        table.insert(cc, row);
                    }
                    catch(SQLException e)
                    {
                        throw new SQLException(e.getMessage() + "\n" + row + "\nIDREF values: " + idRefValues + "\n" + getParseContext().getLocator().getSystemId() + " line " + getParseContext().getLocator().getLineNumber());
                    }
                    finally
                    {
                        endTime = System.currentTimeMillis();
                    }
                    TableImportStatistic tis = ((DataImportParseContext) getParseContext()).getStatistics(table);
                    tis.incSuccessfulRows();
                    tis.addSqlTimeSpent(startTime, endTime);

                    if(storeId != null)
                    {
                        PrimaryKeyColumnValues primaryKeyValues = row.getPrimaryKeyValues();
                        idReferences.put(storeId, primaryKeyValues);
                        tis.addIdReference(storeId, primaryKeyValues);
                    }
                }
                else
                {
                    written = false;
                    getParseContext().addErrors(vc.getAllValidationErrors());
                    for(int i = 0; i < vc.getAllValidationErrors().size(); i++)
                        System.out.println(vc.getAllValidationErrors().get(i));
                }
            }
        }

        public Attributes evaluateTemplateAttrExpressions(TemplateApplyContext ac, boolean[] attrHasExpression) throws SAXException
        {
            return null;
        }

        public String evaluateTemplateTextExpressions(TemplateApplyContext ac, String text)
        {
            return null;
        }

        public void fillCreateApplyContextExpressionsVars(Map vars)
        {
        }

        public ParseContext parseInclude(ParseContext parentPC, Resource resource) throws ContentHandlerException
        {
            try
            {
                DataImportParseContext includePC = new DataImportParseContext(parentPC, resource);
                includePC.parse(cc, schema);
                return includePC;
            }
            catch(Exception e)
            {
                if(e instanceof ContentHandlerException)
                    throw (ContentHandlerException) e;
                else
                    throw new ContentHandlerException(parentPC, e);
            }
        }

        public ParseContext parseInclude(ParseContext parentPC, File srcFile) throws ContentHandlerException
        {
            try
            {
                DataImportParseContext includePC = new DataImportParseContext(parentPC, srcFile);
                includePC.parse(cc, schema);
                return includePC;
            }
            catch(Exception e)
            {
                if(e instanceof ContentHandlerException)
                    throw (ContentHandlerException) e;
                else
                    throw new ContentHandlerException(parentPC, e);
            }
        }
    }

    private Schema schema;
    private ConnectionContext cc;
    private int depth;
    private Map idReferences = new HashMap();
    private boolean isCsvImport;

    public DataImportHandler(DataImportParseContext pc, ConnectionContext cc, Schema schema)
    {
        super(pc, "dal");
        this.schema = schema;
        this.cc = cc;
    }

    public Map getIdReferences()
    {
        return idReferences;
    }

    public void text(String text) throws SAXException
    {
        if(handleDefaultText(text))
            return;

        NodeStackEntry entry = (NodeStackEntry) getActiveNodeEntry();
        if(!entry.isColumnEntry())
            return;

        try
        {
            Row row = entry.row;
            Column column = row.getTable().getColumns().getByNameOrXmlNodeName(entry.rowColumnName);
            if(column != null)
            {
                ColumnValue value = row.getColumnValues().getByColumn(column);

                if(entry.isSqlExpression())
                {
                    DbmsSqlText sqlExpr = value.createSqlExpr();
                    DatabasePolicies.DatabasePolicyEnumeratedAttribute dpea = DatabasePolicies.getInstance().getEnumeratedAttribute();
                    dpea.setValue(entry.sqlExprDbmsId);
                    sqlExpr.setDbms(dpea);
                    sqlExpr.addText(text);
                    value.addSqlExpr(sqlExpr);
                }
                else
                    value.appendText(text);
            }
        }
        catch(Exception e)
        {
            throw new SAXParseException(e.getMessage(), getParseContext().getLocator(), e);
        }
    }

    public void startTemplateElement(TemplateApplyContext tac, String url, String localName, String qName, Attributes attributes) throws SAXException
    {
        startElement(url, localName, qName, attributes);
    }

    public void registerTemplateConsumption(Template template)
    {
    }

    public void startElement(String url, String localName, String qName, Attributes attributes) throws SAXException
    {
        //System.out.println(getStackDepthPrefix() + qName + " " + getAttributeNames(attributes));

        if(handleDefaultStartElement(url, localName, qName, attributes))
            return;

        try
        {
            if(depth == 0)
            {
                // root node, do nothing
                getNodeStack().push(new NodeStackEntry(qName, depth));
            }
            else if(depth == 1)
            {
                // all the primary (top level) row inserts are done as single transactions and all children of
                // top-level rows are part of the same transaction
                Table childTable = schema.getTables().getByNameOrXmlNodeName(qName);
                if(childTable == null)
                {
                    getNodeStack().push(new NodeStackEntry(qName, depth));
                    getParseContext().addError("Table '" + qName + "' not found in the schema");
                }
                else
                {
                    Row childRow = childTable.createRow();
                    NodeStackEntry newEntry = new NodeStackEntry(qName, childRow, depth);
                    newEntry.handleAttributes(attributes, true);
                    getNodeStack().push(newEntry);
                }
            }
            else
            {
                if(haveActiveNodeEntry())
                {
                    NodeStackEntry entry = (NodeStackEntry) getActiveNodeEntry();
                    if(entry.row != null)
                    {
                        Tables childTables = entry.row.getTable().getChildTables();
                        Table childTable = childTables.size() > 0 ? childTables.getByNameOrXmlNodeName(qName) : null;

                        if(childTable != null)
                        {
                            // if we're starting a child row, be sure to write out the active entry so that if there
                            // are relational dependencies everything will work
                            entry.write();

                            Column parentRefCol = childTable.getParentRefColumns().getSole();
                            Row childRow = childTable.createRow((ParentForeignKey) parentRefCol.getForeignKey(), entry.row);
                            NodeStackEntry newEntry = new NodeStackEntry(qName, childRow, depth);
                            newEntry.handleAttributes(attributes, true);
                            getNodeStack().push(newEntry);
                        }
                        else
                        {
                            Column column = entry.row.getTable().getColumns().getByNameOrXmlNodeName(qName);
                            if(column != null)
                            {
                                NodeStackEntry newEntry = new NodeStackEntry(qName, entry.row, qName, depth);
                                newEntry.handleAttributes(attributes, false);
                                getNodeStack().push(newEntry);
                            }
                            else
                            {
                                getNodeStack().push(new NodeStackEntry(qName, depth));
                                getParseContext().addError("Column '" + qName + "' not found in table '" + entry.row.getTable().getName() + "'. Available: " + entry.row.getTable().getColumns().getOnlyNames());
                            }
                        }
                    }
                    else
                        getParseContext().addError("Don't know what to do with element '" + qName + "'");
                }
                else
                    getParseContext().addError("Don't know what to do with element '" + qName + "'");
            }

            String csvFileName = attributes.getValue(ATTRNAME_CSV_FILE);
            if(csvFileName != null)
            {
                isCsvImport = true;
                if(haveActiveNodeEntry())
                {
                    // the file is relative to the current XML file unless it doesn't exist
                    File xdmFile = new File(getParseContext().getInputSource().getSystemId());
                    File csvFile = xdmFile.exists()
                                   ? new File(xdmFile.getParentFile(), csvFileName) : new File(csvFileName);
                    if(csvFile.exists())
                    {
                        NodeStackEntry entry = (NodeStackEntry) getActiveNodeEntry();
                        if(entry.row != null)
                            loadFromCsv(entry.row.getTable(), csvFile, TextUtils.getInstance().toBoolean(attributes.getValue(ATTRNAME_CSV_BLANK_IS_NULL), true));
                        else
                            getParseContext().addError("Request for CSV file import from '" + csvFile.getAbsolutePath() + "' not possible: '" + qName + "' does not have a valid row.");
                    }
                    else
                        getParseContext().addError("Request for CSV file import not possible: '" + csvFile.getAbsolutePath() + "' does not exist.");
                }
                else
                    getParseContext().addError("Request for CSV file import from '" + csvFileName + "' not possible: '" + qName + "' does not seem to be a valid table.");
            }
            else
                isCsvImport = false;
        }
        catch(NamingException exc)
        {
            throw new SAXParseException(exc.getMessage(), getParseContext().getLocator(), exc);
        }
        catch(IOException exc)
        {
            throw new SAXParseException(exc.getMessage(), getParseContext().getLocator(), exc);
        }
        catch(SQLException exc)
        {
            try
            {
                cc.getConnection().rollback();
            }
            catch(Exception e)
            {
                throw new SAXParseException(exc.getMessage(), getParseContext().getLocator(), e);
            }
            throw new SAXParseException(exc.getMessage(), getParseContext().getLocator(), exc);
        }
        catch(ParseException exc)
        {
            throw new SAXParseException(exc.getMessage(), getParseContext().getLocator(), exc);
        }
        catch(DataException exc)
        {
            throw new SAXParseException(exc.getMessage(), getParseContext().getLocator(), exc);
        }
        depth++;
    }

    protected void loadFromCsv(Table table, File file, boolean treatBlanksAsNull) throws IOException, SQLException
    {
        Reader isReader = null;
        LineNumberReader reader = null;

        try
        {
            isReader = new FileReader(file);
            reader = new LineNumberReader(isReader);
            DelimitedValuesParser dvParser = new DelimitedValuesParser();

            String line;
            while((line = reader.readLine()) != null)
            {
                Row row = table.createRow();
                ColumnValues colValues = row.getColumnValues();
                colValues.setValues(dvParser.parse(line), treatBlanksAsNull);

                ValidationContext vc = row.getValidationResult(null);

                if(vc.isValid())
                {
                    long startTime = System.currentTimeMillis();
                    long endTime;
                    try
                    {
                        table.insert(cc, row);
                    }
                    catch(SQLException e)
                    {
                        throw new SQLException(e.getMessage() + "\n" + row + "\n CSV File: " + file.getAbsolutePath() + " line " + reader.getLineNumber());
                    }
                    finally
                    {
                        endTime = System.currentTimeMillis();
                    }
                    TableImportStatistic tis = ((DataImportParseContext) getParseContext()).getStatistics(table);
                    tis.incSuccessfulRows();
                    tis.addSqlTimeSpent(startTime, endTime);
                }
                else
                {
                    getParseContext().addErrors(vc.getAllValidationErrors());
                    for(int i = 0; i < vc.getAllValidationErrors().size(); i++)
                        System.out.println(vc.getAllValidationErrors().get(i));
                }
            }
        }
        finally
        {
            if(reader != null)
                reader.close();

            if(isReader != null)
                isReader.close();
        }
    }

    public void endDocument() throws SAXException
    {
    }

    public void endElement(String url, String localName, String qName) throws SAXException
    {
        if(handleDefaultEndElement(url, localName, qName))
            return;

        try
        {
            depth--;
            if(!getNodeStack().isEmpty())
            {
                NodeStackEntry entry = (NodeStackEntry) getNodeStack().pop();
                if(entry != null)
                {
                    if(!entry.isColumnEntry() && entry.row != null && !isCsvImport)
                        entry.write();
                    if(entry.depth == 1)
                        cc.getConnection().commit();
                }
            }
        }
        catch(NamingException exc)
        {
            throw new SAXParseException(exc.getMessage(), getParseContext().getLocator(), exc);
        }
        catch(SQLException se)
        {
            try
            {
                cc.getConnection().rollback();
            }
            catch(Exception e)
            {
                throw new SAXParseException(e.getMessage(), getParseContext().getLocator(), e);
            }
            throw new SAXException(se);
        }
    }
}
