package auto.dal.db.dao.org;

import com.netspective.axiom.schema.Schema;
import com.netspective.axiom.schema.Row;
import com.netspective.axiom.schema.Rows;
import com.netspective.axiom.sql.dynamic.QueryDefnSelects;
import com.netspective.axiom.schema.Table;
import javax.naming.NamingException;
import java.sql.SQLException;
import com.netspective.axiom.ConnectionContext;
import com.netspective.axiom.sql.dynamic.QueryDefnSelect;
import com.netspective.axiom.schema.ColumnValues;
import com.netspective.axiom.schema.ForeignKey;
import com.netspective.axiom.schema.constraint.ParentForeignKey;
import com.netspective.axiom.sql.QueryResultSet;
import com.netspective.axiom.schema.PrimaryKeyColumnValues;
import com.netspective.axiom.schema.column.type.DateTimeColumn;
import com.netspective.axiom.schema.column.type.DateColumn;
import com.netspective.axiom.schema.column.type.GuidTextColumn;
import com.netspective.axiom.schema.column.type.TextColumn;
import com.netspective.axiom.schema.column.type.RecordStatusIdColumn;
import com.netspective.axiom.schema.column.type.EnumerationIdRefColumn;
import com.netspective.axiom.schema.column.type.GuidColumn;
import com.netspective.axiom.schema.column.type.LongIntegerColumn;
import com.netspective.axiom.schema.column.type.IntegerColumn;
import auto.dal.db.dao.OrgTable;

public final class OrgPersonIdSrcTypeTable
{
    public static final int ACCESSORID_BY_PRIMARY_KEYS_EQUALITY = 0;
    public static final int ACCESSORID_BY_PARENT_KEY_EQUALITY = 1;
    public static final int ACCESSORID_BY_CR_STAMP_EQUALITY = 2;
    public static final int ACCESSORID_BY_CR_SESS_ID_EQUALITY = 3;
    public static final int ACCESSORID_BY_REC_STAT_ID_EQUALITY = 4;
    public static final int ACCESSORID_BY_SYSTEM_ID_EQUALITY = 5;
    public static final int ACCESSORID_BY_ORG_ID_EQUALITY = 6;
    public static final int ACCESSORID_BY_ITEM_ID_EQUALITY = 7;
    public static final int ACCESSORID_BY_CAPTION_EQUALITY = 8;
    public static final int ACCESSORID_BY_ABBREV_EQUALITY = 9;
    public static final int ACCESSORID_BY_INDEX_ORG_PERSONID_SRC_TYPE_UNQ_EQUALITY = 10;
    public static final int COLINDEX_CR_STAMP = 0;
    public static final int COLINDEX_CR_SESS_ID = 1;
    public static final int COLINDEX_REC_STAT_ID = 2;
    public static final int COLINDEX_SYSTEM_ID = 3;
    public static final int COLINDEX_ORG_ID = 4;
    public static final int COLINDEX_ITEM_ID = 5;
    public static final int COLINDEX_CAPTION = 6;
    public static final int COLINDEX_ABBREV = 7;
    
    public OrgPersonIdSrcTypeTable(Table table)
    {
        this.table = (com.netspective.axiom.schema.table.BasicTable)table;
        this.schema = table.getSchema();
        this.accessors = table.getQueryDefinition().getSelects();
        crSessIdForeignKey = table.getColumns().get(COLINDEX_CR_SESS_ID).getForeignKey();
        recStatIdForeignKey = table.getColumns().get(COLINDEX_REC_STAT_ID).getForeignKey();
        orgIdForeignKey = (ParentForeignKey)table.getColumns().get(COLINDEX_ORG_ID).getForeignKey();
    }
    
    public final OrgPersonIdSrcTypeTable.Record createChildLinkedByOrgId(OrgTable.Record parentRecord)
    {
        return new OrgPersonIdSrcTypeTable.Record(table.createRow(orgIdForeignKey, parentRecord.getRow()));
    }
    
    public final OrgPersonIdSrcTypeTable.Record createRecord()
    {
        return new OrgPersonIdSrcTypeTable.Record(table.createRow());
    }
    
    public final TextColumn getAbbrevColumn()
    {
        return (TextColumn)table.getColumns().get(COLINDEX_ABBREV);
    }
    
    public final QueryDefnSelect getAccessorByAbbrevEquality()
    {
        return accessors.get(ACCESSORID_BY_ABBREV_EQUALITY);
    }
    
    public final QueryDefnSelect getAccessorByCaptionEquality()
    {
        return accessors.get(ACCESSORID_BY_CAPTION_EQUALITY);
    }
    
    public final QueryDefnSelect getAccessorByCrSessIdEquality()
    {
        return accessors.get(ACCESSORID_BY_CR_SESS_ID_EQUALITY);
    }
    
    public final QueryDefnSelect getAccessorByCrStampEquality()
    {
        return accessors.get(ACCESSORID_BY_CR_STAMP_EQUALITY);
    }
    
    public final QueryDefnSelect getAccessorByIndexOrgPersonIdSrcTypeUnqEquality()
    {
        return accessors.get(ACCESSORID_BY_INDEX_ORG_PERSONID_SRC_TYPE_UNQ_EQUALITY);
    }
    
    public final QueryDefnSelect getAccessorByItemIdEquality()
    {
        return accessors.get(ACCESSORID_BY_ITEM_ID_EQUALITY);
    }
    
    public final QueryDefnSelect getAccessorByOrgIdEquality()
    {
        return accessors.get(ACCESSORID_BY_ORG_ID_EQUALITY);
    }
    
    public final QueryDefnSelect getAccessorByParentKeyEquality()
    {
        return accessors.get(ACCESSORID_BY_PARENT_KEY_EQUALITY);
    }
    
    public final QueryDefnSelect getAccessorByPrimaryKeysEquality()
    {
        return accessors.get(ACCESSORID_BY_PRIMARY_KEYS_EQUALITY);
    }
    
    public final QueryDefnSelect getAccessorByRecStatIdEquality()
    {
        return accessors.get(ACCESSORID_BY_REC_STAT_ID_EQUALITY);
    }
    
    public final QueryDefnSelect getAccessorBySystemIdEquality()
    {
        return accessors.get(ACCESSORID_BY_SYSTEM_ID_EQUALITY);
    }
    
    public final OrgPersonIdSrcTypeTable.Records getAccessorRecords(ConnectionContext cc, QueryDefnSelect accessor, Object[] bindValues)
    throws NamingException, SQLException
    {
        Rows rows = getTable().createRows();
        QueryResultSet qrs = accessor.execute(cc, bindValues, false);
        if(qrs != null) rows.populateDataByIndexes(qrs.getResultSet());
        qrs.close(false);
        return new Records(rows);
    }
    
    public final TextColumn getCaptionColumn()
    {
        return (TextColumn)table.getColumns().get(COLINDEX_CAPTION);
    }
    
    public final GuidTextColumn getCrSessIdColumn()
    {
        return (GuidTextColumn)table.getColumns().get(COLINDEX_CR_SESS_ID);
    }
    
    public final ForeignKey getCrSessIdForeignKey()
    {
        return crSessIdForeignKey;
    }
    
    public final DateTimeColumn getCrStampColumn()
    {
        return (DateTimeColumn)table.getColumns().get(COLINDEX_CR_STAMP);
    }
    
    public final IntegerColumn getItemIdColumn()
    {
        return (IntegerColumn)table.getColumns().get(COLINDEX_ITEM_ID);
    }
    
    public final LongIntegerColumn getOrgIdColumn()
    {
        return (LongIntegerColumn)table.getColumns().get(COLINDEX_ORG_ID);
    }
    
    public final ParentForeignKey getOrgIdForeignKey()
    {
        return orgIdForeignKey;
    }
    
    /**
     * Parent reference: ParentForeignKey Sources: Org_PersonId_Src_Type.org_id;
     *  Referenced Columns: Org.org_id
     */
    public final OrgPersonIdSrcTypeTable.Records getParentRecordsByOrgId(OrgTable.Record parentRecord, ConnectionContext cc)
    throws NamingException, SQLException
    {
        Records result = new Records(parentRecord, orgIdForeignKey.getChildRowsByParentRow(cc, parentRecord.getRow()));
        return result;
    }
    
    public final RecordStatusIdColumn getRecStatIdColumn()
    {
        return (RecordStatusIdColumn)table.getColumns().get(COLINDEX_REC_STAT_ID);
    }
    
    public final ForeignKey getRecStatIdForeignKey()
    {
        return recStatIdForeignKey;
    }
    
    public final OrgPersonIdSrcTypeTable.Record getRecord(Row row)
    {
        return new OrgPersonIdSrcTypeTable.Record(row);
    }
    
    public final OrgPersonIdSrcTypeTable.Record getRecordByPrimaryKey(ConnectionContext cc, java.lang.String systemId)
    throws NamingException, SQLException
    {
        Row row = table.getRowByPrimaryKeys(cc, new Object[] { systemId }, null);
        Record result = row != null ? new OrgPersonIdSrcTypeTable.Record(row) : null;
        return result;
    }
    
    public final OrgPersonIdSrcTypeTable.Record getRecordByPrimaryKey(ConnectionContext cc, PrimaryKeyColumnValues pkValues)
    throws NamingException, SQLException
    {
        Row row = table.getRowByPrimaryKeys(cc, pkValues, null);
        Record result = row != null ? new OrgPersonIdSrcTypeTable.Record(row) : null;
        return result;
    }
    
    public final GuidColumn getSystemIdColumn()
    {
        return (GuidColumn)table.getColumns().get(COLINDEX_SYSTEM_ID);
    }
    
    public final com.netspective.axiom.schema.table.BasicTable getTable()
    {
        return table;
    }
    
    public final String toString()
    {
        return table.toString();
    }
    private QueryDefnSelects accessors;
    private ForeignKey crSessIdForeignKey;
    private ParentForeignKey orgIdForeignKey;
    private ForeignKey recStatIdForeignKey;
    private Schema schema;
    private com.netspective.axiom.schema.table.BasicTable table;
    
    public final class Record
    {
        
        public Record(Row row)
        {
            if(row.getTable() != table) throw new ClassCastException("Attempting to assign row from table "+ row.getTable().getName() +" to "+ this.getClass().getName() +" (expecting a row from table "+ table.getName() +").");
            this.row = row;
            this.values = row.getColumnValues();
        }
        
        public final boolean dataChangedInStorage(ConnectionContext cc)
        throws NamingException, SQLException
        {
            return table.dataChangedInStorage(cc, row);
        }
        
        public final void delete(ConnectionContext cc, String whereCond, Object[] whereCondBindParams)
        throws NamingException, SQLException
        {
            table.delete(cc, row, whereCond, whereCondBindParams);
        }
        
        public final void delete(ConnectionContext cc)
        throws NamingException, SQLException
        {
            table.delete(cc, row);
        }
        
        public final TextColumn.TextColumnValue getAbbrev()
        {
            return (TextColumn.TextColumnValue)values.getByColumnIndex(COLINDEX_ABBREV);
        }
        
        public final TextColumn.TextColumnValue getCaption()
        {
            return (TextColumn.TextColumnValue)values.getByColumnIndex(COLINDEX_CAPTION);
        }
        
        public final TextColumn.TextColumnValue getCrSessId()
        {
            return (TextColumn.TextColumnValue)values.getByColumnIndex(COLINDEX_CR_SESS_ID);
        }
        
        public final DateColumn.DateColumnValue getCrStamp()
        {
            return (DateColumn.DateColumnValue)values.getByColumnIndex(COLINDEX_CR_STAMP);
        }
        
        public final IntegerColumn.IntegerColumnValue getItemId()
        {
            return (IntegerColumn.IntegerColumnValue)values.getByColumnIndex(COLINDEX_ITEM_ID);
        }
        
        public final LongIntegerColumn.LongIntegerColumnValue getOrgId()
        {
            return (LongIntegerColumn.LongIntegerColumnValue)values.getByColumnIndex(COLINDEX_ORG_ID);
        }
        
        public final EnumerationIdRefColumn.EnumerationIdRefValue getRecStatId()
        {
            return (EnumerationIdRefColumn.EnumerationIdRefValue)values.getByColumnIndex(COLINDEX_REC_STAT_ID);
        }
        
        public final Row getRow()
        {
            return row;
        }
        
        public final TextColumn.TextColumnValue getSystemId()
        {
            return (TextColumn.TextColumnValue)values.getByColumnIndex(COLINDEX_SYSTEM_ID);
        }
        
        public final auto.dal.db.vo.OrgPersonIdSrcType getValues()
        {
            return getValues(new auto.dal.db.vo.impl.OrgPersonIdSrcTypeVO());
        }
        
        public final auto.dal.db.vo.OrgPersonIdSrcType getValues(auto.dal.db.vo.OrgPersonIdSrcType valueObject)
        {
            valueObject.setCrStamp((java.util.Date) values.getByColumnIndex(COLINDEX_CR_STAMP).getValue());
            valueObject.setCrSessId((java.lang.String) values.getByColumnIndex(COLINDEX_CR_SESS_ID).getValue());
            valueObject.setRecStatId((java.lang.Integer) values.getByColumnIndex(COLINDEX_REC_STAT_ID).getValue());
            valueObject.setSystemId((java.lang.String) values.getByColumnIndex(COLINDEX_SYSTEM_ID).getValue());
            valueObject.setOrgId((java.lang.Long) values.getByColumnIndex(COLINDEX_ORG_ID).getValue());
            valueObject.setItemId((java.lang.Integer) values.getByColumnIndex(COLINDEX_ITEM_ID).getValue());
            valueObject.setCaption((java.lang.String) values.getByColumnIndex(COLINDEX_CAPTION).getValue());
            valueObject.setAbbrev((java.lang.String) values.getByColumnIndex(COLINDEX_ABBREV).getValue());
            return valueObject;
        }
        
        public final void insert(ConnectionContext cc)
        throws NamingException, SQLException
        {
            table.insert(cc, row);
        }
        
        public final void refresh(ConnectionContext cc)
        throws NamingException, SQLException
        {
            table.refreshData(cc, row);
        }
        
        public final void setAbbrev(com.netspective.commons.value.Value value)
        {
            getAbbrev().copyValueByReference(value);
        }
        
        public final void setCaption(com.netspective.commons.value.Value value)
        {
            getCaption().copyValueByReference(value);
        }
        
        public final void setCrSessId(com.netspective.commons.value.Value value)
        {
            getCrSessId().copyValueByReference(value);
        }
        
        public final void setCrStamp(com.netspective.commons.value.Value value)
        {
            getCrStamp().copyValueByReference(value);
        }
        
        public final void setItemId(com.netspective.commons.value.Value value)
        {
            getItemId().copyValueByReference(value);
        }
        
        public final void setOrgId(com.netspective.commons.value.Value value)
        {
            getOrgId().copyValueByReference(value);
        }
        
        public final void setRecStatId(com.netspective.commons.value.Value value)
        {
            getRecStatId().copyValueByReference(value);
        }
        
        public final void setSystemId(com.netspective.commons.value.Value value)
        {
            getSystemId().copyValueByReference(value);
        }
        
        public final void setValues(auto.dal.db.vo.OrgPersonIdSrcType valueObject)
        {
            values.getByColumnIndex(COLINDEX_CR_STAMP).setValue(valueObject.getCrStamp());
            values.getByColumnIndex(COLINDEX_CR_SESS_ID).setValue(valueObject.getCrSessId());
            values.getByColumnIndex(COLINDEX_REC_STAT_ID).setValue(valueObject.getRecStatId());
            values.getByColumnIndex(COLINDEX_SYSTEM_ID).setValue(valueObject.getSystemId());
            values.getByColumnIndex(COLINDEX_ORG_ID).setValue(valueObject.getOrgId());
            values.getByColumnIndex(COLINDEX_ITEM_ID).setValue(valueObject.getItemId());
            values.getByColumnIndex(COLINDEX_CAPTION).setValue(valueObject.getCaption());
            values.getByColumnIndex(COLINDEX_ABBREV).setValue(valueObject.getAbbrev());
        }
        
        public final String toString()
        {
            return row.toString();
        }
        
        public final void update(ConnectionContext cc, String whereCond, Object[] whereCondBindParams)
        throws NamingException, SQLException
        {
            table.update(cc, row, whereCond, whereCondBindParams);
        }
        
        public final void update(ConnectionContext cc)
        throws NamingException, SQLException
        {
            table.update(cc, row);
        }
        private Row row;
        private ColumnValues values;
    }
    
    public final class Records
    {
        
        public Records(Rows rows)
        {
            this.rows = rows;
            this.cache = new Record[rows.size()];
        }
        
        public Records(OrgTable.Record parentRecord, Rows rows)
        {
            this(rows);
            this.parentRecord = parentRecord;
        }
        
        public final void delete(ConnectionContext cc)
        throws NamingException, SQLException
        {
            for(int i = 0; i < cache.length; i++)get(i).delete(cc);
        }
        
        public final OrgPersonIdSrcTypeTable.Record get(int i)
        {
            if(cache[i] == null) cache[i] = new Record(rows.getRow(i));
            return cache[i];
        }
        
        public final int size()
        {
            return rows.size();
        }
        
        public final String toString()
        {
            return rows.toString();
        }
        private Record[] cache;
        private OrgTable.Record parentRecord;
        private Rows rows;
    }
}