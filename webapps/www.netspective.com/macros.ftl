<#macro childMenus parentPage>
    <#list parentPage.childrenList as childPage>
    <tr>
        <td>
            <a class="menu" href="${childPage.getUrl(vc)}">
            <span class="L${childPage.level}">
                <#if childPage = activePage>
                    <span class="active-page">${childPage.caption.getTextValue(vc)}</a>
                <#elseif childPage.isInActivePath(vc)>
                    <span class="active-path">${childPage.caption.getTextValue(vc)}</a>
                <#else>
                    ${childPage.caption.getTextValue(vc)}
                </#if>
            </span>
            </a>
        </td>
    </tr>
    <#if childPage.childrenList?size gt 0>
        <@childMenus parentPage=childPage/>
    </#if>
    </#list>
</#macro>

<#macro primaryAncestorChildren>
    <table width="151" border="0" cellspacing="0" cellpadding="0">
        <@childMenus parentPage=activePage.primaryAncestor/>
    </table>
</#macro>

<#macro nextPageLink>
   <#local nextPage = activePage.getNextPath()?default('')/>
   <#if nextPage != ''><p align=right><br><a href="${nextPage.getUrl(vc)}">${nextPage.getHeading(vc)}</a> &gt;&nbsp;</p></#if>
</#macro>

<#macro pageBodyBegin>

    <table class="site-area" width="600" border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td width=151 valign=bottom><img src="${resourcesPath}/images/products/frameworks/spacer-border-left.gif"/><table width=100% border="0" cellspacing="0" cellpadding="0">
                    <tr><td class="site-area-name">${activePage.primaryAncestor.getCaption(vc)}</td></tr>
                </table></td>
            <td width=443 class="tag-line" valign=bottom><p align=right>${activePage.getHeading(vc)}</p></td>
        </tr>
    </table>

    <table width="600" border="0" cellspacing="0" cellpadding="0" class="body-content">
        <tr>
            <td valign="top" class="page-nav" rowspan=2>
                <@primaryAncestorChildren/>
            </td>
            <td class="body-summary">
                ${activePage.summary?default('')}
            </td>
        </tr>
        <tr>
            <td valign="top" class="body-content">
            <div class="body-content">
</#macro>

<#macro pageBodyEnd>
            </div>
            </td>
        </tr>
   </table>
</#macro>