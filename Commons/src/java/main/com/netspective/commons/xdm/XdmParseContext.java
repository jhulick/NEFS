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
package com.netspective.commons.xdm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.netspective.commons.io.Resource;
import com.netspective.commons.xdm.exception.DataModelException;
import com.netspective.commons.xml.ParseContext;
import com.netspective.commons.xml.TransformProcessingInstructionEncounteredException;

public class XdmParseContext extends ParseContext
{
    public XdmParseContext(ParseContext parentPC, String text) throws ParserConfigurationException, SAXException
    {
        super(parentPC, text);
    }

    public XdmParseContext(ParseContext parentPC, File srcFile) throws ParserConfigurationException, SAXException, FileNotFoundException
    {
        super(parentPC, srcFile);
    }

    public XdmParseContext(ParseContext parentPC, Resource resource) throws ParserConfigurationException, SAXException, IOException
    {
        super(parentPC, resource);
    }

    public XdmParseContext(ParseContext parentPC, File jarFile, ZipEntry jarFileEntry) throws ParserConfigurationException, SAXException, IOException
    {
        super(parentPC, jarFile, jarFileEntry);
    }

    public void parse(Object parent) throws DataModelException, TransformProcessingInstructionEncounteredException
    {
        XMLReader parser = getParser();
        InputSource inputSource = getInputSource();

        try
        {
            XdmHandler handler = new XdmHandler(this, parent);
            parser.setContentHandler(handler);
            parser.parse(inputSource);
        }
        catch(SAXParseException exc)
        {
            throw new DataModelException(this, exc.getMessage(), exc);
        }
        catch(SAXException exc)
        {
            Throwable t = exc.getException();
            if(t instanceof TransformProcessingInstructionEncounteredException)
            {
                throw (TransformProcessingInstructionEncounteredException) t;
            }
            else if(t instanceof DataModelException)
            {
                throw (DataModelException) t;
            }
            throw new DataModelException(this, exc.getMessage(), t);
        }
        catch(FileNotFoundException exc)
        {
            throw new DataModelException(this, exc);
        }
        catch(IOException exc)
        {
            throw new DataModelException(this, "Error reading XML data model file", exc);
        }
        finally
        {
            try
            {
                closeInputSource();
            }
            catch(IOException e)
            {
            }
        }
    }

    public static XdmParseContext parse(XmlDataModel dm, File srcFile) throws DataModelException, FileNotFoundException
    {
        XdmParseContext pc = null;
        try
        {
            pc = new XdmParseContext(null, srcFile);
            pc.setTemplateCatalog(dm.getTemplateCatalog());
            pc.parse(dm);
            pc.setTemplateCatalog(null); // free explicitly other will cause memory leak because the template catalog is not owned by the PC
            return pc;
        }
        catch(TransformProcessingInstructionEncounteredException exc)
        {
            try
            {
                pc.doExternalTransformations();
                pc.parse(dm);
                return pc;
            }
            catch(Exception e)
            {
                throw new DataModelException(pc, e);
            }
        }
        catch(ParserConfigurationException exc)
        {
            throw new DataModelException(pc, "Parser has not been configured correctly", exc);
        }
        catch(SAXException exc)
        {
            throw new DataModelException(pc, exc);
        }
    }

    public static XdmParseContext parse(XmlDataModel dm, String text) throws DataModelException
    {
        XdmParseContext pc = null;
        try
        {
            pc = new XdmParseContext(null, text);
            pc.setTemplateCatalog(dm.getTemplateCatalog());
            pc.parse(dm);
            pc.setTemplateCatalog(null); // free explicitly other will cause memory leak because the template catalog is not owned by the PC
            return pc;
        }
        catch(TransformProcessingInstructionEncounteredException exc)
        {
            try
            {
                pc.doExternalTransformations();
                pc.parse(dm);
                return pc;
            }
            catch(Exception e)
            {
                throw new DataModelException(pc, e);
            }
        }
        catch(ParserConfigurationException exc)
        {
            throw new DataModelException(pc, "Parser has not been configured correctly", exc);
        }
        catch(SAXException exc)
        {
            throw new DataModelException(pc, exc);
        }
    }

    public static XdmParseContext parse(XmlDataModel dm, Resource resource) throws DataModelException, IOException
    {
        XdmParseContext pc = null;
        try
        {
            pc = new XdmParseContext(null, resource);
            pc.setTemplateCatalog(dm.getTemplateCatalog());
            pc.parse(dm);
            pc.setTemplateCatalog(null); // free explicitly other will cause memory leak because the template catalog is not owned by the PC
            return pc;
        }
        catch(TransformProcessingInstructionEncounteredException exc)
        {
            try
            {
                pc.doExternalTransformations();
                pc.parse(dm);
                return pc;
            }
            catch(Exception e)
            {
                throw new DataModelException(pc, e);
            }
        }
        catch(ParserConfigurationException exc)
        {
            throw new DataModelException(pc, "Parser has not been configured correctly", exc);
        }
        catch(SAXException exc)
        {
            throw new DataModelException(pc, exc);
        }
    }

    public static XdmParseContext parse(XmlDataModel dm, File jarFile, ZipEntry jarFileEntry) throws DataModelException, IOException
    {
        XdmParseContext pc = null;
        try
        {
            pc = new XdmParseContext(null, jarFile, jarFileEntry);
            pc.setTemplateCatalog(dm.getTemplateCatalog());
            pc.parse(dm);
            pc.setTemplateCatalog(null); // free explicitly other will cause memory leak because the template catalog is not owned by the PC
            return pc;
        }
        catch(TransformProcessingInstructionEncounteredException exc)
        {
            try
            {
                pc.doExternalTransformations();
                pc.parse(dm);
                return pc;
            }
            catch(Exception e)
            {
                throw new DataModelException(pc, e);
            }
        }
        catch(ParserConfigurationException exc)
        {
            throw new DataModelException(pc, "Parser has not been configured correctly", exc);
        }
        catch(SAXException exc)
        {
            throw new DataModelException(pc, exc);
        }
    }
}
