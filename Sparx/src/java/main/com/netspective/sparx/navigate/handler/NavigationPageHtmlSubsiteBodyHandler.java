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
 * $Id: NavigationPageHtmlSubsiteBodyHandler.java,v 1.1 2003-10-24 03:28:10 shahid.shah Exp $
 */

package com.netspective.sparx.navigate.handler;

import java.io.Writer;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.ServletException;

import org.apache.oro.text.perl.Perl5Util;

import com.netspective.commons.value.ValueSource;
import com.netspective.commons.value.source.StaticValueSource;
import com.netspective.sparx.navigate.NavigationPage;
import com.netspective.sparx.navigate.NavigationContext;

/**
 * Allows the navigation of a HTML website but makes it looks like it's part of the same theme (including headers, footers, etc)
 */

public class NavigationPageHtmlSubsiteBodyHandler extends NavigationPageBodyDefaultHandler
{
    protected class Substitute
    {
        private ValueSource perlRegEx;

        public Substitute()
        {
        }

        public ValueSource getPerlRegEx()
        {
            return perlRegEx;
        }

        public void setPerlRegEx(ValueSource perlRegEx)
        {
            this.perlRegEx = perlRegEx;
        }
    }

    private ValueSource indexFile = new StaticValueSource("index.html");
    private ValueSource root;
    private List substitutions = new ArrayList();

    public ValueSource getIndexFile()
    {
        return indexFile;
    }

    public void setIndexFile(ValueSource indexFile)
    {
        this.indexFile = indexFile;
    }

    public ValueSource getRoot()
    {
        return root;
    }

    public void setRoot(ValueSource root)
    {
        this.root = root;
    }

    public Substitute createSubstitute()
    {
        return new Substitute();
    }

    public void addSubstitute(Substitute replace)
    {
        substitutions.add(replace);
    }

    public void handleNavigationPageBody(NavigationPage page, Writer writer, NavigationContext nc) throws ServletException, IOException
    {
        if(root == null)
        {
            writer.write("No root path specified.");
            return;
        }

        File rootPath = new File(root.getTextValue(nc));

        StringBuffer activePath = new StringBuffer(rootPath.getAbsolutePath());
        String relativePath = nc.getActivePathFindResults().getUnmatchedPath(0);
        if(relativePath != null && relativePath.length() > 0)
            activePath.append(relativePath);

        File activeFile = new File(activePath.toString());
        if(activeFile.isDirectory())
            activeFile = new File(activeFile.getAbsolutePath() + "/" + indexFile.getTextValue(nc));

        if(! activeFile.exists())
        {
            writer.write("URI " + activeFile.getAbsolutePath() + " does not exist");
            return;
        }

        FileReader fr = new FileReader(activeFile);
        BufferedReader br = new BufferedReader(fr);

        try
        {
            if(substitutions.size() > 0)
            {
                final Perl5Util perl5Util = new Perl5Util();
                final String[] regExs = new String[substitutions.size()];

                for(int i  = 0; i < substitutions.size(); i++)
                {
                    Substitute substitute = (Substitute) substitutions.get(i);
                    regExs[i] = substitute.getPerlRegEx().getTextValue(nc);
                }

                String rec;
                while((rec = br.readLine()) != null)
                {
                    for(int i = 0; i < regExs.length; i++)
                        rec = perl5Util.substitute(regExs[i], rec);
                    writer.write(rec);
                }
            }
            else
            {
                String rec;
                while((rec = br.readLine()) != null)
                    writer.write(rec);
            }
        }
        finally
        {
            br.close();
            fr.close();
        }
    }
}