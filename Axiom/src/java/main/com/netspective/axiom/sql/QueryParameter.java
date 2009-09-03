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
package com.netspective.axiom.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import com.netspective.axiom.ConnectionContext;
import com.netspective.commons.value.Value;
import com.netspective.commons.value.ValueContext;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.xdm.XdmParseContext;
import com.netspective.commons.xdm.XmlDataModelSchema;
import com.netspective.commons.xdm.exception.DataModelException;

/**
 * Class for handling a parameter defined for a query.
 */
public class QueryParameter implements XmlDataModelSchema.ConstructionFinalizeListener
{
    public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options();

    static
    {
        XML_DATA_MODEL_SCHEMA_OPTIONS.setIgnorePcData(true);
        XML_DATA_MODEL_SCHEMA_OPTIONS.addIgnoreAttributes(new String[]{"index"});
    }

    private QueryParameters parent;
    private String name;
    private ValueSource value;
    private int sqlType = Types.VARCHAR;
    private Class javaType = String.class;
    private int index;

    public QueryParameter(QueryParameters parent)
    {
        setParent(parent);
    }

    public void finalizeConstruction(XdmParseContext pc, Object element, String elementName) throws DataModelException
    {
        if(value == null)
        {
            RuntimeException e = new RuntimeException(QueryParameter.class.getName() + " '" + this.parent.getQuery().getQualifiedName() + "' has no 'value' or 'values' attribute.");
            this.parent.getQuery().getLog().error(e.getMessage(), e);
            throw e;
        }
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
        if(name == null)
            setName("param-" + index);
    }

    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the query parameter.
     *
     * @param name query parameter name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public QueryParameters getParent()
    {
        return parent;
    }

    public void setParent(QueryParameters parent)
    {
        this.parent = parent;
    }

    public int getSqlTypeCode()
    {
        return sqlType;
    }

    /**
     * Sets an integer value corresponding to the JDBC type of this query parameter.
     *
     * @param type integer value corresponding to the JDBC type of query parameter
     */
    public void setSqlTypeCode(int type)
    {
        this.sqlType = type;
    }

    /**
     * Sets the JDBC type for this query parameter.
     *
     * @param sqlType JDBC type for query parameter
     */
    public void setSqlType(QueryParameterTypeEnumeratedAttribute sqlType)
    {
        String paramTypeName = sqlType.getValue();
        if(paramTypeName != null)
        {
            QueryParameterType type = QueryParameterType.get(paramTypeName);
            if(type == null)
                throw new RuntimeException("param type '" + paramTypeName + "' is invalid for param '" + getName() + "' in query '" + parent.getQuery().getQualifiedName() + "'");
            setSqlTypeCode(type.getJdbcType());
            setJavaType(type.getJavaClass());
        }
    }

    public Class getJavaType()
    {
        return javaType;
    }

    /**
     * Sets the Java Type for this query parameter.
     *
     * @param javaType Java type for the query parameter
     */
    public void setJavaType(Class javaType)
    {
        this.javaType = javaType;
    }

    public ValueSource getValue()
    {
        return value;
    }

    /**
     * Sets the value for this query parameter using a value source object.
     *
     * @param value value source object containing the query parameter value
     */
    public void setValue(ValueSource value)
    {
        this.value = value;
    }

    /**
     * Sets multiple values for this query parameter.
     *
     * @param values value source object containing list of values for this query parameter
     */
    public void setValues(ValueSource values)
    {
        setValue(values);
        setSqlTypeCode(Types.ARRAY);
    }

    public boolean isScalarType()
    {
        return sqlType != Types.ARRAY;
    }

    public boolean isListType()
    {
        return sqlType == Types.ARRAY;
    }

    public void apply(QueryParameters.ValueApplyContext vac, ConnectionContext cc, PreparedStatement stmt) throws SQLException
    {
        if(sqlType != Types.ARRAY)
        {
            int paramNum = vac.getNextParamNum();
            if(sqlType == Types.VARCHAR)
                stmt.setObject(paramNum, value.getTextValue(cc));
            else
            {
                Value sv = value.getValue(cc);
                switch(sqlType)
                {
                    case Types.INTEGER:
                        stmt.setInt(paramNum, sv.getIntValue());
                        break;

                    case Types.DOUBLE:
                        stmt.setDouble(paramNum, sv.getDoubleValue());
                        break;

                    case Types.DATE:
                        stmt.setDate(paramNum, new java.sql.Date(((Date) sv.getValueForSqlBindParam()).getTime()));
                        break;

                    case Types.TIME:
                        stmt.setTime(paramNum, new java.sql.Time(((Date) sv.getValueForSqlBindParam()).getTime()));
                        break;

                    case Types.TIMESTAMP:
                        stmt.setTimestamp(paramNum, new Timestamp(((Date) sv.getValueForSqlBindParam()).getTime()));
                        break;

                    case Types.FLOAT:
                        stmt.setLong(paramNum, sv.getLongValue());
                        break;

                    case Types.BOOLEAN:
                        stmt.setBoolean(paramNum, sv.getBooleanValue());
                        break;

                    default:
                        // assume the caller knows what they're doing
                        stmt.setObject(paramNum, sv.getValueForSqlBindParam(), sqlType);
                }
            }
        }
        else
        {
            final String[] textValues = value.getTextValues(cc);
            for(int q = 0; q < textValues.length; q++)
            {
                final int paramNum = vac.getNextParamNum();
                final String textValue = textValues[q];
                if(sqlType == Types.VARCHAR)
                    stmt.setString(paramNum, textValue);
                else
                {
                    switch(sqlType)
                    {
                        case Types.INTEGER:
                            stmt.setInt(paramNum, Integer.parseInt(textValue));
                            break;

                        case Types.DOUBLE:
                            stmt.setDouble(paramNum, Double.parseDouble(textValue));
                            break;

                        default:
                            // assume the caller knows what they're doing
                            stmt.setObject(paramNum, textValue, sqlType);
                    }
                }
            }
        }
    }

    public void retrieve(QueryParameters.ValueRetrieveContext vrc, ConnectionContext cc) throws SQLException
    {
        if(sqlType != Types.ARRAY)
        {
            if(sqlType == Types.VARCHAR)
                vrc.addBindValue(value.getTextValue(cc), sqlType);
            else
            {
                Value sv = value.getValue(cc);
                switch(sqlType)
                {
                    case Types.INTEGER:
                        vrc.addBindValue(new Integer(sv.getIntValue()), sqlType);
                        break;

                    case Types.DOUBLE:
                        vrc.addBindValue(new Double(sv.getDoubleValue()), sqlType);
                        break;

                    default:
                        // assume the caller knows what they're doing
                        vrc.addBindValue(sv.getValue(), sqlType);
                }
            }
        }
        else
        {
            String[] textValues = value.getTextValues(cc);
            for(int q = 0; q < textValues.length; q++)
                vrc.addBindValue(textValues[q], sqlType);
        }
    }

    /**
     * Appends a list of bind parameters and their respective information used for debugging to the buffer
     */
    public void appendBindText(StringBuffer text, ValueContext vc, String terminator)
    {
        text.append("[" + index + "]");
        if(sqlType != Types.ARRAY)
        {
            Object ov = value.getValue(vc);
            text.append(value.getSpecification().getSpecificationText());
            text.append(" = ");
            text.append(((Value) ov).getValueForSqlBindParam());
            text.append(" (java: ");
            text.append(ov != null ? ov.getClass().getName() : "<NULL>");
            text.append(", sqlType: ");
            text.append(sqlType);
            text.append(", QueryParameterType ");
            final QueryParameterType parameterType = QueryParameterType.get(sqlType);
            text.append(parameterType.getJdbcType());
            text.append(" ('"+ parameterType.getIdentifier() +"', "+ parameterType.getJavaClass().getName() +")");
            text.append(")");
        }
        else
        {
            text.append(value.getSpecification().getSpecificationText());
            text.append(" = ");

            String[] textValues = value.getTextValues(vc);
            if(value != null)
            {
                for(int v = 0; v < textValues.length; v++)
                {
                    if(v > 0)
                        text.append(", ");
                    text.append("'" + textValues[v] + "'");
                }
            }
            else
            {
                text.append("null");
            }

            text.append(" (text list)");
        }
        if(terminator != null) text.append(terminator);
    }
}

