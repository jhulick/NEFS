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
package com.netspective.sparx.navigate.fts;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.lucene.queryParser.ParseException;

import com.netspective.commons.template.TemplateProcessor;
import com.netspective.commons.xdm.XmlDataModelSchema;
import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.template.freemarker.FreeMarkerTemplateProcessor;

public class SearchHitsTemplateRenderer implements SearchHitsRenderer
{
    public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);

    private TemplateProcessor requestTemplate;
    private TemplateProcessor resultsTemplate;
    private TemplateProcessor queryErrorBodyTemplate;
    private String expressionFormFieldName = "expression";
    private String searchWithinSearchResultsFormFieldName = "searchWithinResults";
    private String requestFormFieldTemplateVarName = "formFieldName";
    private String searchWithinCheckboxFormFieldTemplateVarName = "searchWithinFieldName";
    private String rendererTemplateVarName = "renderer";
    private String searchResultsTemplateVarName = "searchResults";
    private String expressionTemplateVarName = "expression";
    private String exceptionTemplateVarName = "exception";
    private String[] hitsMatrixFieldNames;

    public SearchHitsTemplateRenderer()
    {
        FreeMarkerTemplateProcessor renderer = (FreeMarkerTemplateProcessor) createRequestBody();
        renderer.addTemplateContent("You have not provided any search request renderer content.");
        renderer.finalizeContents();
        addRequestBody(renderer);

        renderer = (FreeMarkerTemplateProcessor) createResultsBody();
        renderer.addTemplateContent("You have not provided any search results renderer content.");
        renderer.finalizeContents();
        addResultsBody(renderer);
    }

    protected Map createDefaultTemplateVars(FullTextSearchResults searchResults)
    {
        final Map templateVars = new HashMap();
        templateVars.put(rendererTemplateVarName, this);
        templateVars.put(requestFormFieldTemplateVarName, expressionFormFieldName);
        templateVars.put(searchWithinCheckboxFormFieldTemplateVarName, searchWithinSearchResultsFormFieldName);
        if(searchResults != null)
        {
            templateVars.put(searchResultsTemplateVarName, searchResults);
            templateVars.put(expressionTemplateVarName, searchResults.getExpression().getExprText());
        }
        return templateVars;
    }

    public SearchExpression getSearchExpression(NavigationContext nc)
    {
        final ServletRequest request = nc.getRequest();
        final String exprText = request.getParameter(getExpressionFormFieldName());
        return exprText == null ? null : new SearchExpression()
        {
            public String getExprText()
            {
                return exprText;
            }

            public boolean isEmptyExpression()
            {
                return exprText.length() == 0;
            }

            public boolean isSearchWithinPreviousResults()
            {
                return request.getParameter(searchWithinSearchResultsFormFieldName) != null;
            }
        };
    }

    public void renderSearchRequest(Writer writer, NavigationContext nc) throws IOException
    {
        final Map templateVars = createDefaultTemplateVars(null);
        requestTemplate.process(writer, nc, templateVars);
    }

    public void renderEmptyQuery(Writer writer, NavigationContext nc) throws IOException
    {
        final Map templateVars = createDefaultTemplateVars(null);
        templateVars.put(expressionTemplateVarName, "");
        requestTemplate.process(writer, nc, templateVars);
    }

    public void renderQueryError(Writer writer, NavigationContext nc, SearchExpression expression, ParseException exception) throws IOException
    {
        final Map templateVars = createDefaultTemplateVars(null);
        templateVars.put(expressionTemplateVarName, expression);
        templateVars.put(exceptionTemplateVarName, exception);
        if(queryErrorBodyTemplate != null)
            queryErrorBodyTemplate.process(writer, nc, templateVars);
        else
            requestTemplate.process(writer, nc, templateVars);
    }

    public void renderSearchResults(Writer writer, NavigationContext nc, FullTextSearchResults searchResults) throws IOException
    {
        final Map templateVars = createDefaultTemplateVars(searchResults);
        resultsTemplate.process(writer, nc, templateVars);
    }

    public String getExpressionFormFieldName()
    {
        return expressionFormFieldName;
    }

    public void setExpressionFormFieldName(String expressionFormFieldName)
    {
        this.expressionFormFieldName = expressionFormFieldName;
    }

    public TemplateProcessor createRequestBody()
    {
        return new FreeMarkerTemplateProcessor();
    }

    public void addRequestBody(TemplateProcessor templateProcessor)
    {
        requestTemplate = templateProcessor;
    }

    public TemplateProcessor getRequestBody()
    {
        return requestTemplate;
    }

    public TemplateProcessor createResultsBody()
    {
        return new FreeMarkerTemplateProcessor();
    }

    public void addResultsBody(TemplateProcessor templateProcessor)
    {
        resultsTemplate = templateProcessor;
    }

    public TemplateProcessor getResultsBody()
    {
        return resultsTemplate;
    }

    public TemplateProcessor createQueryErrorBody()
    {
        return new FreeMarkerTemplateProcessor();
    }

    public void addQueryErrorBody(TemplateProcessor templateProcessor)
    {
        queryErrorBodyTemplate = templateProcessor;
    }

    public TemplateProcessor getQueryErrorBody()
    {
        return queryErrorBodyTemplate;
    }

    public String[] getHitsMatrixFieldNames()
    {
        return hitsMatrixFieldNames;
    }

    public void setHitsMatrixFieldNames(String[] hitsMatrixFieldNames)
    {
        this.hitsMatrixFieldNames = hitsMatrixFieldNames;
    }

    public String getExpressionTemplateVarName()
    {
        return expressionTemplateVarName;
    }

    public void setExpressionTemplateVarName(String expressionTemplateVarName)
    {
        this.expressionTemplateVarName = expressionTemplateVarName;
    }

    public String getExceptionTemplateVarName()
    {
        return exceptionTemplateVarName;
    }

    public void setExceptionTemplateVarName(String exceptionTemplateVarName)
    {
        this.exceptionTemplateVarName = exceptionTemplateVarName;
    }
}
