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
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;

import com.netspective.sparx.navigate.NavigationContext;

public interface SearchHitsRenderer
{
    /**
     * The search expression that has been entered by the user
     */
    public SearchExpression getSearchExpression(NavigationContext nc);

    /**
     * Called when the user should be requested (through a form or something) to enter a search expression
     */
    public void renderSearchRequest(Writer writer, NavigationContext nc) throws IOException;

    /**
     * Called when the user has entered a search parameter and now should be shown the results
     */
    public void renderSearchResults(Writer writer, NavigationContext nc, FullTextSearchResults searchResults) throws IOException;

    /**
     * Called when the user has entered a search query but the query could not be parsed properly
     */
    public void renderQueryError(Writer writer, NavigationContext nc, SearchExpression expression, ParseException exception) throws IOException;

    /**
     * Called when the search throws an exception
     */
    public void renderSearchError(Writer writer, NavigationContext nc, SearchExpression expression, Exception exception) throws IOException;

    /**
     * Called when the user enters an empty query
     */
    public void renderEmptyQuery(Writer writer, NavigationContext nc) throws IOException;

    /**
     * If we want to debug the FTS index, we will be passed a list of top terms so we should render them
     *
     * @param termsByFieldsMap Each key is a field name, the value is another Map of terms and term frequency
     */
    public void renderTerms(Writer writer, NavigationContext nc, Map termsByFieldsMap) throws IOException;

    /**
     * If this renderer expects to get a hits matrix using SearchResults.getActivePageHitMatrix then it can provide
     * the default field names that belong in the matrix
     */
    public String[] getHitsMatrixFieldNames();
}
