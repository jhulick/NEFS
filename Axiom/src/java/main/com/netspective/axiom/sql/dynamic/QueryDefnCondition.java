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
package com.netspective.axiom.sql.dynamic;

import com.netspective.axiom.sql.JdbcTypesEnumeratedAttribute;
import com.netspective.axiom.sql.dynamic.exception.QueryDefinitionException;
import com.netspective.axiom.sql.dynamic.exception.QueryDefnFieldNotFoundException;
import com.netspective.axiom.sql.dynamic.exception.QueryDefnSqlComparisonNotFoundException;
import com.netspective.commons.script.BeanScript;
import com.netspective.commons.script.Script;
import com.netspective.commons.script.ScriptContext;
import com.netspective.commons.script.ScriptException;
import com.netspective.commons.text.TextUtils;
import com.netspective.commons.value.Value;
import com.netspective.commons.value.ValueContext;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.xdm.XmlDataModelSchema;

/**
 * Class representing the selection criteria for a dynamic query (query definition).
 * Determine how the data input from this query definition's select-dialog is
 * interpreted by the query definition engine.
 */
public class QueryDefnCondition
{
    public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);

    public static final int CONNECT_AND = 0;
    public static final int CONNECT_OR = 1;
    public static final String[] CONNECTOR_TOKENS = new String[]{"and", "or"};
    public static final String[] CONNECTOR_SQL = new String[]{" and ", " or "};

    private QueryDefinition owner;
    private QueryDefnCondition parentCondition;
    private QueryDefnField field;
    private SqlComparison comparison;
    private ValueSource value;
    private int connector = CONNECT_AND;
    private boolean removeIfValueNull;
    private boolean removeIfValueNullChildren;
    private Script script;
    private boolean joinOnly = true; // use only the join condition from the field (changed to false if comparison is provided)
    private String bindExpr;
    private QueryDefnConditions nestedConditions;
    private JdbcTypesEnumeratedAttribute bindJdbcType;

    public QueryDefnCondition(QueryDefinition owner)
    {
        this.owner = owner;
    }

    public QueryDefnCondition(QueryDefnCondition parentCondition) throws QueryDefinitionException
    {
        this(parentCondition.owner);
        this.parentCondition = parentCondition;
        field = parentCondition.field;
        comparison = parentCondition.comparison;
        value = parentCondition.value;
        removeIfValueNull = parentCondition.removeIfValueNull;
        bindExpr = parentCondition.bindExpr;
        bindJdbcType = parentCondition.bindJdbcType;
        joinOnly = parentCondition.joinOnly;

        // right now we're not allowing nested conditions to have dynamic include/exclude capability
        if(removeIfValueNull)
            throw new QueryDefinitionException(owner, "Nested conditions can not have attribute 'allow-null=\"no\"'");
    }

    public QueryDefinition getOwner()
    {
        return owner;
    }

    public QueryDefnCondition getParentCondition()
    {
        return parentCondition;
    }

    public String getBindExpr()
    {
        return bindExpr;
    }

    public void setBindExpr(String bindExpr)
    {
        this.bindExpr = bindExpr;
    }

    public JdbcTypesEnumeratedAttribute getBindJdbcType()
    {
        return bindJdbcType;
    }

    public void setBindJdbcType(final JdbcTypesEnumeratedAttribute bindJdbcType)
    {
        this.bindJdbcType = bindJdbcType;
    }

    public QueryDefnField getField()
    {
        return field;
    }

    /**
     * Defines the select-dialog field on which the condition is being defined
     * using &lt;condition&gt; tag.
     *
     * @param fieldName name of the conditional field
     */
    public void setField(String fieldName) throws QueryDefnFieldNotFoundException
    {
        field = owner.getFields().get(fieldName);
        if(field == null)
            throw new QueryDefnFieldNotFoundException(owner, fieldName, "Field name '" + fieldName + "' not found in condition.");
    }

    public SqlComparison getComparison()
    {
        return comparison;
    }

    /**
     * Sets the relational operator to be used for this condition.  For example,
     * gte-date, greater-than-equal and ends-with.
     *
     * @param attr comparison operator
     *
     * @throws QueryDefnSqlComparisonNotFoundException
     *          when the given value does not
     *          correspond to a valid operator
     */
    public void setComparison(SqlComparisonEnumeratedAttribute attr) throws QueryDefnSqlComparisonNotFoundException
    {
        comparison = SqlComparisonFactory.getComparison(attr.getValue());
        if(comparison == null)
            throw new QueryDefnSqlComparisonNotFoundException(owner, attr.getValue(), "SQL comparison '" + attr.getValue() + "' not found.");
        setJoinOnly(false);
    }

    public ValueSource getValue()
    {
        return value;
    }

    /**
     * Set the field of the main query definition with which this select-dialog
     * field is to be compared.
     *
     * @param value value source object containing the main query-defn field with which
     *              this select-dialog fields is to be compared
     */
    public void setValue(ValueSource value)
    {
        this.value = value;
    }

    public String getConnectorSql()
    {
        return QueryDefnCondition.CONNECTOR_SQL[connector];
    }

    /**
     * Defines how many field criteria each record in the database has to match
     * before it is selected. For example, if all the conditional fields (defined by
     * &lt;condition&gt; tags) have a connector value of <cide>and</code>, a record
     * would have to match all these field criteria to be selected in the final result.
     *
     * @param connector connector for this conditional field
     */
    public void setConnector(QueryDefnConditionConnectorEnumeratedAttribute connector)
    {
        this.connector = connector.getValueIndex();
    }

    public boolean isJoinOnly()
    {
        return joinOnly;
    }

    public void setJoinOnly(boolean joinOnly)
    {
        this.joinOnly = joinOnly;
    }

    public boolean isNested()
    {
        return nestedConditions != null;
    }

    public boolean isNotNested()
    {
        return nestedConditions == null;
    }

    public boolean removeIfValueIsNull()
    {
        return removeIfValueNull || removeIfValueNullChildren;
    }

    /**
     * Sets whether or not to allow <code>NULL</code> as a valid bind parameter value.
     *
     * @param allowNull If set to <code>false</code>, the select generated will
     *                  omit the field if the corresponding dialog field happens
     *                  to be empty.  Otherwise, it keeps the field in the generated
     *                  sql (with NULL value) if the corresponding dialog field
     *                  happens to be empty.
     */
    public void setAllowNull(boolean allowNull)
    {
        removeIfValueNull = !allowNull;
    }

    public String getWhereCondExpr(ValueContext vc, QueryDefnSelect select, QueryDefnSelectStmtGenerator stmt) throws QueryDefinitionException
    {
        if(nestedConditions == null)
            return comparison.getWhereCondExpr(vc, select, stmt, this);

        StringBuffer sql = new StringBuffer();
        int lastNestedCond = nestedConditions.size() - 1;
        for(int c = 0; c <= lastNestedCond; c++)
        {
            QueryDefnCondition cond = nestedConditions.get(c);
            stmt.addJoin(cond.getField());
            sql.append(" (" + cond.getWhereCondExpr(vc, select, stmt) + ")");
            if(c != lastNestedCond)
                sql.append(cond.getConnectorSql());
        }
        return sql.toString();
    }

    public QueryDefnCondition createCondition() throws QueryDefinitionException
    {
        return new QueryDefnCondition(this);
    }

    public void addCondition(QueryDefnCondition cond)
    {
        if(nestedConditions == null)
            nestedConditions = new QueryDefnConditions(this);
        nestedConditions.add(cond);
        if(cond.removeIfValueIsNull())
            removeIfValueNullChildren = true;
    }

    /**
     * Return true if this condition should be kept when dynamically generating the where clause. One of the
     * reasons to not keep the condition would be because the value is null and we don't want the where clause
     * element to have any items with nulls. If this condition is a nested condition, we will check to see if
     * any of our nested conditions are used; if none of the nested conditions are used, then we will not keep
     * the condition.
     */
    public boolean useCondition(QueryDefnSelectStmtGenerator stmtGen, ValueContext vc, QueryDefnConditions usedConditions) throws QueryDefinitionException
    {
        if(script != null)
        {
            Object scriptResult = null;
            try
            {
                ValueSource vs = getValue();
                if(vs != null)
                {
                    Value value = vs.getValue(vc);
                    if(value == null)
                        return false;

                    ScriptContext sc = vc.getScriptContext(script);
                    scriptResult = script.callFunction(sc, null, "useCondition", new Object[]{this, vc, value});
                }
            }
            catch(ScriptException e)
            {
                throw new QueryDefinitionException(getOwner(), e);
            }

            boolean useCondition = true;
            if(scriptResult instanceof Boolean)
                useCondition = ((Boolean) scriptResult).booleanValue();
            else if(scriptResult != null)
                useCondition = TextUtils.getInstance().toBoolean(scriptResult.toString(), false);

            if(! useCondition)
                return false;
        }

        // if we don't allow nulls, always use the condition
        if(!removeIfValueNull)
            return true;

        if(nestedConditions != null)
        {
            QueryDefnConditions nestedUsedConditions = nestedConditions.getUsedConditions(stmtGen, vc);
            if(nestedUsedConditions.size() == 0)
                return false;

            usedConditions.add(nestedUsedConditions);
            return true;
        }
        else
        {
            ValueSource vs = getValue();
            if(vs != null)
            {
                Value value = vs.getValue(vc);
                if(value == null)
                    return false;

                if(value.isListValue())
                {
                    String[] values = value.getTextValues();
                    if(values == null || values.length == 0 || (values.length == 1 && (values[0] == null || values[0].length() == 0)))
                        return false;
                }
                else
                {
                    String textValue = value.getTextValue();
                    if(textValue == null || textValue.length() == 0)
                        return false;
                }

                usedConditions.add(this);
                stmtGen.addJoin(field);
                return true;
            }
            return false;

        }
    }

    public Script getScript()
    {
        return script;
    }

    public Script createScript()
    {
        return new BeanScript();
    }

    public void addScript(Script conditionScript)
    {
        this.script = conditionScript;
    }
}