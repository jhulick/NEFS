<#include "*/library.ftl"/>

There are several schools of thought when it comes to answering the "where do I start writing my application?" question.
Two of the popular ones are:
<ol>
    <li>
      Define and prototype the entire user interface (presentation layer or UI) and wait until the UI is completed
      before starting the database management tasks. In this option, the user interface elements (forms, fields,
      etc.) drive the data model and schema.
    </li>
    <li>
      Define and prototype the entire data management layer first and then follow-up with the presentation layer. In this
      option, the data model and schema drive the user interface.
    </li>
</ol>
<p>
Netspective does not assume either method to be correct so it supports both of them. Basically, developing applications
with Sparx requires that you do the following:
<p>
<center>
<@contentImage image='nefs-swdev-process.gif'/>
</center>
<ol>
    <li>
        <b>Define</b> your requirements. This definition is usually a mental exercise but it may involve formal
        requirements gathering (Waterfall style) or using customer stories (XP/Agile style). The more time you spend
        in the definition phase understanding your customer's requirements, the less time you'll spend changing code
        later on.
    </li>
    <li>
        <b>Declare</b> your requirements to Sparx using the XML tags. Almost all facets of the software development
        process are covered by the Sparx XML tags and Java APIs, including but not limited to, forms declarations,
        data validation, page definition, template writing, navigation, dynamic and static SQL, and database schemas.
    </li>
    <li>
        Use the Console to <b>Unit Test</b> your Sparx XML declarations. Every Sparx XML tag you use to declare your
        application's requirements has an automated means to verify and test your declaration within the <i>Console</i>.
        At this point you may begin to <i>involve your customers</i> by <b>demonstrating</b> the forms, validation,
        pages, etc within the Console -- before you start writing any code.
    </li>
    <li>
        <b>Generate</b> SQL DDL (data definition language) for creating database schemas and generate the data access
        objects (DAOs) that become the Data Access Layer (DAL) for reading and writing content from the database.
    </li>
    <li>
        Use Java class inheritance, composition, or listeners to <b>Customize</b> your Sparx XML declarations. The Sparx
        XML declarations basically create common functionality. By adding Java code you will customize the declarations
        to execute customized business logic.
    </li>
    <li>
        Run <b>Functional/Integration tests</b> of your combined Sparx XML declarations and Java code customizations.
    </li>
    <li>
        <b>Deploy</b> your application once everything is working.
    </li>
</ol>

<table class="report" border="0" cellspacing="2" cellpadding="0">
    <tr>
        <th class="report-column-heading" rowspan=2>&nbsp;</th>
        <th class="report-column-heading" rowspan=2>Task</th>
        <th class="report-column-heading" colspan=2>Presentation</th>
        <th class="report-column-heading" colspan=3>Database</th>
        <th class="report-column-heading" colspan=2>Security</th>
    </tr>

    <tr>
        <th class="report-column-heading">Navigation/Pages</th>
        <th class="report-column-heading">Dialogs</th>

        <th class="report-column-heading">Static SQL</th>
        <th class="report-column-heading">Dynamic SQL</th>
        <th class="report-column-heading">Schema</th>

        <th class="report-column-heading">Roles</th>
        <th class="report-column-heading">Permissions</th>
    </tr>

    <tr>
        <td class="report-column-odd">1</td>
        <td class="report-column-odd">Define</td>
        <td class="report-column-odd">Define your application's hierarchy (the "main" menu, submenus, and so on). Consider each navigation item a "page".</td>
        <td class="report-column-odd">Define the manner in which data will be collected.</td>
        <td class="report-column-odd">Consider what SQL should be created manually.</td>
        <td class="report-column-odd">Consider what type of searches your application should perform.</td>
        <td class="report-column-odd">Define the structures (tables, columns, relationships) needed to store your data.</td>
        <td class="report-column-odd">Define the different security roles necessary.</td>
        <td class="report-column-odd">Define all the permissions that will be assigned to roles.</td>
    </tr>

    <tr>
        <td class="report-column-even">2</td>
        <td class="report-column-even">Declare/Describe</td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
    </tr>

    <tr>
        <td class="report-column-odd">3</td>
        <td class="report-column-odd">Unit Test/Demonstration</td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
    </tr>

    <tr>
        <td class="report-column-even">4</td>
        <td class="report-column-even">Generate Code</td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
    </tr>

    <tr>
        <td class="report-column-odd">5</td>
        <td class="report-column-odd">Create Custom Code</td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
    </tr>

    <tr>
        <td class="report-column-even">6</td>
        <td class="report-column-even">Acceptance Test</td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
        <td class="report-column-even"></td>
    </tr>

    <tr>
        <td class="report-column-odd">7</td>
        <td class="report-column-odd">Deploy</td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
        <td class="report-column-odd"></td>
    </tr>

</table>