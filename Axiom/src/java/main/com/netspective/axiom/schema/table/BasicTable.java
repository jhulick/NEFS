/*
 * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
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
 *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
 *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
 *    without written consent of Netspective.
 *
 * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
 *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
 * ARE HEREBY DISCLAIMED.
 *
 * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
 * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
 * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * @author Shahid N. Shah
 */

/**
 * $Id: BasicTable.java,v 1.4 2003-04-09 16:57:37 shahid.shah Exp $
 */

package com.netspective.axiom.schema.table;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.netspective.axiom.schema.Column;
import com.netspective.axiom.schema.Schema;
import com.netspective.axiom.schema.Table;
import com.netspective.axiom.schema.Columns;
import com.netspective.axiom.schema.Tables;
import com.netspective.axiom.schema.Row;
import com.netspective.axiom.schema.Rows;
import com.netspective.axiom.schema.ForeignKey;
import com.netspective.axiom.schema.PrimaryKeyColumnValues;
import com.netspective.axiom.schema.PrimaryKeyColumns;
import com.netspective.axiom.schema.ColumnValues;
import com.netspective.axiom.schema.column.BasicColumn;
import com.netspective.axiom.schema.BasicSchema;
import com.netspective.axiom.schema.Index;
import com.netspective.axiom.schema.Indexes;
import com.netspective.axiom.schema.TableHierarchyReference;
import com.netspective.axiom.schema.constraint.ParentForeignKey;
import com.netspective.axiom.schema.column.ColumnsCollection;
import com.netspective.axiom.schema.column.PrimaryKeyColumnsCollection;
import com.netspective.axiom.sql.QueryResultSet;
import com.netspective.axiom.sql.QueryExecutionLog;
import com.netspective.axiom.sql.dynamic.QueryDefnCondition;
import com.netspective.axiom.sql.dynamic.SqlComparisonEnumeratedAttribute;
import com.netspective.axiom.sql.dynamic.QueryDefnConditionConnectorEnumeratedAttribute;
import com.netspective.axiom.sql.dynamic.QueryDefnFieldReference;
import com.netspective.axiom.sql.dynamic.QueryDefnJoin;
import com.netspective.axiom.sql.dynamic.QueryDefnSelect;
import com.netspective.axiom.sql.dynamic.exception.QueryDefinitionException;
import com.netspective.axiom.sql.dynamic.exception.QueryDefnFieldNotFoundException;
import com.netspective.axiom.sql.dynamic.exception.QueryDefnSqlComparisonNotFoundException;
import com.netspective.axiom.ConnectionContext;
import com.netspective.axiom.DatabasePolicy;
import com.netspective.commons.xdm.XmlDataModelSchema;
import com.netspective.commons.xml.template.TemplateConsumerDefn;
import com.netspective.commons.xml.template.TemplateProducer;
import com.netspective.commons.xml.template.TemplateProducerParent;
import com.netspective.commons.xml.template.TemplateProducers;
import com.netspective.commons.xml.template.TemplateConsumer;
import com.netspective.commons.text.TextUtils;

public class BasicTable implements Table, TemplateProducerParent, TemplateConsumer
{
    private static final Log log = LogFactory.getLog(BasicTable.class);
    public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options();
    public static final String ATTRNAME_TYPE = "type";
    public static final String[] ATTRNAMES_SET_BEFORE_CONSUMING = new String[] { "name" };

    static
    {
        XML_DATA_MODEL_SCHEMA_OPTIONS.setIgnorePcData(true);
        XML_DATA_MODEL_SCHEMA_OPTIONS.addIgnoreNestedElements(new String[] { "row" } );
    }

    protected class TableTypeTemplateConsumerDefn extends TemplateConsumerDefn
    {
        public TableTypeTemplateConsumerDefn()
        {
            super(getSchema().getTableTypesTemplatesNameSpaceId(), ATTRNAME_TYPE, ATTRNAMES_SET_BEFORE_CONSUMING);
        }
    }

    protected class TablePresentationTemplate extends TemplateProducer
    {
        public TablePresentationTemplate()
        {
            super(getSchema().getPresentationTemplatesNameSpaceId(), BasicSchema.TEMPLATEELEMNAME_PRESENTATION, null, null, false, false);
        }

        public String getTemplateName(String url, String localName, String qName, Attributes attributes) throws SAXException
        {
            return getName();
        }
    }

    private Schema schema;
    private Column parentColumn;
    private String name;
    private String abbrev;
    private String xmlNodeName;
    private String description;
    private Tables parentTables = new TablesCollection();
    private TableHierarchyReference hierarchyRef;
    private Columns columns = new ColumnsCollection();
    private Tables childTables = new TablesCollection();
    private PrimaryKeyColumns primaryKeyColumns;
    private Columns parentRefColumns;
    private QueryExecutionLog execLog = new QueryExecutionLog();
    private TableQueryDefinition queryDefn = null;
    private QueryDefnSelect selectByPrimaryKey;
    private QueryDefnSelect selectByParentKey;
    private Rows staticData;
    private Indexes indexes = new IndexesCollection();
    private TemplateProducers templateProducers;
    private TemplateConsumerDefn templateConsumer;

    static public String translateTableNameForMapKey(String name)
    {
        return name != null ? name.toUpperCase() : null;
    }

    public TemplateConsumerDefn getTemplateConsumerDefn()
    {
        if(templateConsumer == null)
            templateConsumer = new TableTypeTemplateConsumerDefn();
        return templateConsumer;
    }

    public TemplateProducers getTemplateProducers()
    {
        if(templateProducers == null)
        {
            templateProducers = new TemplateProducers();
            templateProducers.add(new TablePresentationTemplate());
        }
        return templateProducers;
    }

    public BasicTable(Schema schema)
    {
        setSchema(schema);
    }

    public BasicTable(Column parentColumn)
    {
        setParentColumn(parentColumn);
    }

    public void finishConstruction()
    {
        columns.finishConstruction();
        if(hierarchyRef != null)
        {
            Table parentTable = schema.getTables().getByName(hierarchyRef.getParent());
            if(parentTable == null)
                throw new RuntimeException("Unable to find table '"+ hierarchyRef.getParent() +"' for '"+ getName() +"' parent hierarchy");
            parentTable.registerChildTable(this);
        }
    }

    public boolean isApplicationTable()
    {
        return true;
    }

    public Schema getSchema()
    {
        return schema;
    }

    public void setSchema(Schema value)
    {
        schema = value;
    }

    public Column getParentColumn()
    {
        return parentColumn;
    }

    public void setParentColumn(Column parentColumn)
    {
        this.parentColumn = parentColumn;
        setSchema(parentColumn.getSchema());
    }

    public String getName()
    {
        return name;
    }

    public String getNameForMapKey()
    {
        return name != null ? name.toUpperCase() : null;
    }

    public void setName(String value)
    {
        name = value;
    }

    public String getXmlNodeName()
    {
        return xmlNodeName == null ? TextUtils.xmlTextToNodeName(getName()) : xmlNodeName;
    }

    public void setXmlNodeName(String value)
    {
        xmlNodeName = value;
    }

    public String getAbbrev()
    {
        return abbrev != null ? abbrev : getName();
    }

    public void setAbbrev(String abbrev)
    {
        this.abbrev = abbrev;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String value)
    {
        description = value;
    }

    public PrimaryKeyColumns getPrimaryKeyColumns()
    {
        if(primaryKeyColumns == null)
        {
            primaryKeyColumns = new PrimaryKeyColumnsCollection();
            for(int i = 0; i < columns.size(); i++)
            {
                Column column = columns.get(i);
                if(column.isPrimaryKey())
                    primaryKeyColumns.add(column);
            }
        }
        return primaryKeyColumns;
    }

    public Columns getForeignKeyColumns(int fkeyType)
    {
        Columns result = new ColumnsCollection();
        for(int i = 0; i < columns.size(); i++)
        {
            Column column = columns.get(i);
            ForeignKey fkey = column.getForeignKey();
            if(fkey != null && fkey.getType() == fkeyType)
                result.add(column);
        }
        return result;
    }

    public Columns getParentRefColumns()
    {
        if(parentRefColumns == null)
            parentRefColumns = getForeignKeyColumns(ForeignKey.FKEYTYPE_PARENT);

        return parentRefColumns;
    }

    public Column createColumn()
    {
        return new BasicColumn(this);
    }

    public Column createColumn(Class cls) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        if(Column.class.isAssignableFrom(cls))
        {
            Constructor c = cls.getConstructor(new Class[] { Table.class });
            return (Column) c.newInstance(new Object[] { this });
        }
        else
            throw new RuntimeException("Don't know what to do with with class: " + cls);
    }

    public Tables getChildTables()
    {
        return childTables;
    }

    public Columns getColumns()
    {
        return columns;
    }

    public void addColumn(Column column)
    {
        // columns that contain <composite> don't want to be added since they have children that have been added
        if(column.isAllowAddToTable())
        {
            int indexInRow = columns.add(column);
            column.setIndexInRow(indexInRow);
        }
    }

    public Rows getData()
    {
        return staticData;
    }

    public Rows createData()
    {
        if(staticData == null)
            staticData = createRows();
        return staticData;
    }

    public void addData(Rows data)
    {
        // staticData is already present, do nothing
    }

    public boolean isChildTable()
    {
        return parentTables.size() > 0;
    }

    public Tables getParentTables()
    {
        return parentTables;
    }

    public boolean isParentTable()
    {
        return childTables != null && childTables.size() > 0;
    }

    public void registerChildTable(Table table)
    {
        if (childTables == null)
            childTables = new TablesCollection();
        childTables.add(table);
        table.getParentTables().add(table);
    }

    public void removeChildTable(Table table)
    {
        if(childTables != null)
            childTables.remove(table);
    }

    public TableHierarchyReference createHierarchy()
    {
        if(hierarchyRef == null)
            hierarchyRef = new BasicTableHierarchyReference();
        return hierarchyRef;
    }

    public TableHierarchyReference getHierarchy()
    {
        return hierarchyRef;
    }

    public void addHierarchy(TableHierarchyReference hierarchy)
    {
        // do nothing
    }

    public Row createRow()
    {
        return new BasicRow(this);
    }

    public Rows createRows()
    {
        return new BasicRows(this);
    }

    public Row createRow(ParentForeignKey pfKey, Row parentRow)
    {
        Row childRow = createRow();
        pfKey.fillChildValuesFromParentConnector(childRow.getColumnValues(), parentRow.getColumnValues());
        return childRow;
    }

    public void refreshData(ConnectionContext cc, Row row) throws NamingException, SQLException
    {
        getRowByPrimaryKeys(cc, row.getPrimaryKeyValues(), row);
    }

    public boolean dataChangedInStorage(ConnectionContext cc, Row row) throws NamingException, SQLException
    {
        Row compareTo = getRowByPrimaryKeys(cc, row.getPrimaryKeyValues(), null);
        return !row.equals(compareTo);
    }

    public Row getRowByPrimaryKeys(ConnectionContext cc, PrimaryKeyColumnValues values, Row row) throws NamingException, SQLException
    {
        return getRowByPrimaryKeys(cc, row.getPrimaryKeyValues().getValuesForSqlBindParams(), row);
    }

    public Row getRowByPrimaryKeys(ConnectionContext cc, Object[] pkValues, Row row) throws NamingException, SQLException
    {
        QueryDefnSelect pkSelect = getAccessorByPrimaryKeyEquality();

        Row resultRow = row;
        QueryResultSet qrs = pkSelect.execute(cc, pkValues, false);
        if(qrs != null)
        {
            ResultSet rs = qrs.getResultSet();
            if (rs.next())
            {
                if (resultRow == null) resultRow = createRow();
                resultRow.getColumnValues().populateValues(rs, ColumnValues.RESULTSETROWNUM_SINGLEROW);
            }
        }
        qrs.close(false);
        return resultRow;
    }

    /* ------------------------------------------------------------------------------------------------------------- */

    public void registerForeignKeyDependency(ForeignKey fKey)
    {
        // if we are the "referenced" foreign key, then the source is a child of ours
        if (fKey.getType() == ForeignKey.FKEYTYPE_PARENT)
        {
            Table parentTable = fKey.getReferencedColumns().getFirst().getTable();
            parentTable.registerChildTable(this);
        }
    }

    public void removeForeignKeyDependency(ForeignKey fKey)
    {
        // if we are the "referenced" foreign key, then the source is a child of ours
        if (fKey.getType() == ForeignKey.FKEYTYPE_PARENT)
        {
            Table parentTable = fKey.getReferencedColumns().getFirst().getTable();
            parentTable.removeChildTable(this);
        }
    }

    /* ------------------------------------------------------------------------------------------------------------- */

    public Indexes getIndexes()
    {
        return indexes;
    }

    public void addIndex(Index index)
    {
        indexes.add(index);
    }

    public Index createIndex()
    {
        return new BasicIndex(this);
    }

    public Index createIndex(Column column)
    {
        if(column.getTable() != this)
            throw new RuntimeException("Unable to create index -- column '"+ column.getQualifiedName() +"' does not belong to table '"+ getName() +"'. ("+ this + " != " + column.getTable() +")");
        else
        {
            Index index = new BasicIndex(column);
            index.setUnique(column.isUnique());
            return index;
        }
    }

    /* ------------------------------------------------------------------------------------------------------------- */

    public QueryExecutionLog getDmlExecutionLog()
    {
        return execLog;
    }

    public void insert(ConnectionContext cc, Row row) throws SQLException
    {
        try
        {
            cc.getDatabasePolicy().insertValues(cc, DatabasePolicy.DMLFLAG_EXECUTE | DatabasePolicy.DMLFLAG_USE_BIND_PARAMS, row.getColumnValues(), row);
        }
        catch (NamingException e)
        {
            log.error(e);
            throw new SQLException(e.getMessage());
        }
    }

    public void update(ConnectionContext cc, Row row) throws SQLException
    {
        update(cc, row, getAccessorByPrimaryKeyEquality().getWhereClauseSql(), row.getPrimaryKeyValues().getValuesForSqlBindParams());
    }

    public void update(ConnectionContext cc, Row row, String whereCond, Object[] whereCondBindParams) throws SQLException
    {
        try
        {
            cc.getDatabasePolicy().updateValues(cc, DatabasePolicy.DMLFLAG_EXECUTE | DatabasePolicy.DMLFLAG_USE_BIND_PARAMS, row.getColumnValues(), row, whereCond, whereCondBindParams);
        }
        catch (NamingException e)
        {
            log.error(e);
            throw new SQLException(e.getMessage());
        }
    }

    public void delete(ConnectionContext cc, Row row) throws SQLException
    {
        delete(cc, row, getAccessorByPrimaryKeyEquality().getWhereClauseSql(), row.getPrimaryKeyValues().getValuesForSqlBindParams());
    }

    public void delete(ConnectionContext cc, Row row, String whereCond, Object[] whereCondBindParams) throws SQLException
    {
        try
        {
            cc.getDatabasePolicy().deleteValues(cc, DatabasePolicy.DMLFLAG_EXECUTE | DatabasePolicy.DMLFLAG_USE_BIND_PARAMS, row.getColumnValues(), row, whereCond, whereCondBindParams);
        }
        catch (NamingException e)
        {
            log.error(e);
            throw new SQLException(e.getMessage());
        }
    }

    /* ------------------------------------------------------------------------------------------------------------- */

    public void initQueryDefinition(TableQueryDefinition tqd)
    {
        tqd.setName(getName());

        for(int i = 0; i < columns.size(); i++)
        {
            tqd.addField(columns.get(i).createQueryDefnField(tqd));
        }

        QueryDefnJoin join = tqd.createJoin();
        join.setName(getName());
        join.setTable(getName());
        join.setAutoInclude(true);
        tqd.addJoin(join);

        // automatically create and add the primary key accessor
        getAccessorByPrimaryKeyEquality();

        // automatically create and add the parent key accessor
        getAccessorByParentKeyEquality();

        // create all the accessors by single column equality
        for(int i = 0; i < columns.size(); i++)
        {
            Column column = columns.get(i);
            QueryDefnSelect selectByColumn = tqd.createSelect();
            selectByColumn.setName("by-"+ column.getXmlNodeName() +"-equality");
            selectByColumn.setDistinct(false);
            tqd.addSelect(selectByColumn);

            try
            {
                populateTableColumnsAsDisplayFields(selectByColumn);
                populateColumnEqualityCondition(selectByColumn, column);
            }
            catch (QueryDefinitionException e)
            {
                log.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }

        // create all the accessors by index equality
        Indexes indexes = getIndexes();
        for(int i = 0; i < indexes.size(); i++)
        {
            Index index = indexes.get(i);

            // if it's a single column, we already have the equality accessor
            if(index.getColumns().size() == 1)
                continue;

            QueryDefnSelect selectByIndex = tqd.createSelect();
            selectByIndex.setName("by-index-"+ index.getName() +"-equality");
            selectByIndex.setDistinct(false);
            tqd.addSelect(selectByIndex);

            try
            {
                populateTableColumnsAsDisplayFields(selectByIndex);
                populateColumnEqualityConditions(selectByIndex, index.getColumns());
            }
            catch (QueryDefinitionException e)
            {
                log.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    protected void populateTableColumnsAsDisplayFields(QueryDefnSelect select) throws QueryDefinitionException
    {
        Columns columns = getColumns();
        for(int i = 0; i < columns.size(); i++)
        {
            QueryDefnFieldReference qdfr = select.createDisplay();
            qdfr.setField(columns.get(i).getName());
            select.addDisplay(qdfr);
        }
    }

    protected void populateColumnEqualityCondition(QueryDefnSelect qds, Column column) throws QueryDefnFieldNotFoundException, QueryDefnSqlComparisonNotFoundException
    {
        QueryDefnCondition cond = qds.createCondition();
        cond.setField(column.getName());
        cond.setAllowNull(true);

        SqlComparisonEnumeratedAttribute scea = new SqlComparisonEnumeratedAttribute();
        scea.setValue("equals");
        cond.setComparison(scea);

        qds.addCondition(cond);
    }

    protected void populateColumnEqualityConditions(QueryDefnSelect qds, Columns cols) throws QueryDefnFieldNotFoundException, QueryDefnSqlComparisonNotFoundException
    {
        int lastColumn = cols.size() - 1;
        for(int c = 0; c <= lastColumn; c++)
        {
            QueryDefnCondition cond = qds.createCondition();
            cond.setField(cols.get(c).getName());
            cond.setAllowNull(true);

            SqlComparisonEnumeratedAttribute scea = new SqlComparisonEnumeratedAttribute();
            scea.setValue("equals");
            cond.setComparison(scea);

            if(c < lastColumn)
            {
                QueryDefnConditionConnectorEnumeratedAttribute qdccea = new QueryDefnConditionConnectorEnumeratedAttribute();
                qdccea.setValue("and");
                cond.setConnector(qdccea);
            }

            qds.addCondition(cond);
        }
    }

    public QueryDefnSelect getAccessorByPrimaryKeyEquality()
    {
        if(selectByPrimaryKey == null)
        {
            TableQueryDefinition queryDefn = getQueryDefinition();
            selectByPrimaryKey = queryDefn.createSelect();
            selectByPrimaryKey.setName("by-primary-keys-equality");
            selectByPrimaryKey.setDistinct(false);
            queryDefn.addSelect(selectByPrimaryKey);

            try
            {
                populateTableColumnsAsDisplayFields(selectByPrimaryKey);
                populateColumnEqualityConditions(selectByPrimaryKey, getPrimaryKeyColumns());
            }
            catch (QueryDefinitionException e)
            {
                log.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }

        return selectByPrimaryKey;
    }

    private QueryDefnSelect getAccessorByParentKeyEquality()
    {
        Columns parentRefCols = getParentRefColumns();
        if(selectByParentKey == null && parentRefCols.size() > 0)
        {
            TableQueryDefinition queryDefn = getQueryDefinition();
            selectByParentKey = queryDefn.createSelect();
            selectByParentKey.setName("by-parent-key-equality");
            selectByParentKey.setDistinct(false);
            queryDefn.addSelect(selectByParentKey);

            try
            {
                populateTableColumnsAsDisplayFields(selectByParentKey);
                populateColumnEqualityConditions(selectByParentKey, parentRefCols);
            }
            catch (QueryDefinitionException e)
            {
                log.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }

        return selectByParentKey;
    }

    public TableQueryDefinition getQueryDefinition()
    {
        if(queryDefn == null)
        {
            queryDefn = new TableQueryDefinition(this);
            initQueryDefinition(queryDefn);
        }

        return queryDefn;
    }

    public QueryDefnSelect createAccessor() throws QueryDefinitionException
    {
        return getQueryDefinition().createSelect();
    }

    public void addAccessor(QueryDefnSelect accessor) throws QueryDefinitionException
    {
        if(accessor.getDisplayFields().size() == 0)
            populateTableColumnsAsDisplayFields(accessor);

        getQueryDefinition().addSelect(accessor);
    }

    public QueryDefnSelect getAccessorByColumnEquality(Column column)
    {
        TableQueryDefinition tqd = getQueryDefinition();
        return tqd.getSelects().get("by-"+ column.getXmlNodeName() +"-equality");
    }

    public QueryDefnSelect getAccessorByColumnsEquality(Columns columns)
    {
        TableQueryDefinition tqd = getQueryDefinition();
        String accessorName = "by-"+ columns.getOnlyXmlNodeNames("-") +"-equality";
        QueryDefnSelect qds = tqd.getSelects().get("by-"+ columns.getOnlyXmlNodeNames("-") +"-equality");
        if(qds == null)
        {
            qds = queryDefn.createSelect();
            qds.setName(accessorName);
            qds.setDistinct(false);
            tqd.addSelect(qds);

            try
            {
                populateTableColumnsAsDisplayFields(qds);
                populateColumnEqualityConditions(qds, columns);
            }
            catch (QueryDefinitionException e)
            {
                log.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }
        return qds;
    }

    /* ------------------------------------------------------------------------------------------------------------- */

    public Schema.TableTreeNode createTreeNode(Schema.TableTree owner, Schema.TableTreeNode parent, int level)
    {
        return new BasicTableTreeNode(owner, parent, level);
    }

    public class BasicTableTreeNode implements Schema.TableTreeNode
    {
        private Schema.TableTree owner;
        private Schema.TableTreeNode parent;
        private ParentForeignKey parentForeignKey;
        private int level;
        private List children = new ArrayList();
        private List ancestors;

        public BasicTableTreeNode(Schema.TableTree owner, Schema.TableTreeNode parent, int level)
        {
            this.owner = owner;
            this.parent = parent;
            this.level = level;

            Columns parentRefColumns = getParentRefColumns();
            for(int i = 0; i < parentRefColumns.size(); i++)
            {
                Column parentRefColumn = parentRefColumns.get(i);
                ParentForeignKey fKey = (ParentForeignKey) parentRefColumn.getForeignKey();
                if(fKey.getReferencedColumns().getFirst().getTable() == parent.getTable())
                    parentForeignKey = fKey;
            }

            Tables childTables = getChildTables();
            for(int i = 0; i < childTables.size(); i++)
            {
                Table childTable = childTables.get(i);
                children.add(childTable.createTreeNode(owner, this, level+1));
            }
        }

        public Schema.TableTree getOwner()
        {
            return owner;
        }

        public Table getTable()
        {
            return BasicTable.this;
        }

        public Schema.TableTreeNode getParentNode()
        {
            return parent;
        }

        public ParentForeignKey getParentForeignKey()
        {
            return parentForeignKey;
        }

        public List getChildren()
        {
            return children;
        }

        public String getAncestorTableNames(String delimiter)
        {
            List ancestors = getAncestors();
            if(ancestors.size() == 0)
                return null;

            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < ancestors.size(); i++)
            {
                Schema.TableTreeNode node = (Schema.TableTreeNode) ancestors.get(i);
                if(i > 0)
                    sb.append(delimiter);
                sb.append(node.getTable().getName());
            }
            return sb.toString();
        }

        public List getAncestors()
        {
            if(ancestors == null)
            {
                ancestors = new ArrayList();
                Schema.TableTreeNode activeParentNode = getParentNode();
                while(activeParentNode != null)
                {
                    if(ancestors.size() == 0)
                        ancestors.add(activeParentNode);
                    else
                        ancestors.add(0, activeParentNode);

                    activeParentNode = activeParentNode.getParentNode();
                }
            }

            return ancestors;
        }

        public boolean hasChildren()
        {
            return children.size() > 0;
        }

        public boolean hasGrandchildren()
        {
            if(! hasChildren())
                return false;

            for(int i = 0; i < children.size(); i++)
            {
                // if any of our children have children then we have grandchildren
                if(((Schema.TableTreeNode) children.get(i)).hasChildren())
                    return true;
            }

            return false;
        }

        public String toString()
        {
            StringBuffer sb = new StringBuffer();

            for(int i = 0; i < level; i++)
                sb.append("  ");
            sb.append(getTable().getName());
            sb.append(" [");
            sb.append(parentForeignKey);
            sb.append("] (");
            sb.append(getAncestorTableNames("."));
            sb.append(")");
            sb.append("\n");

            for(int c = 0; c < children.size(); c++)
                sb.append(children.get(c));

            return sb.toString();
        }
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Table " + getName() + "\n");
        sb.append(columns.toString());
        return sb.toString();
    }
}
