<#if !(vc.request.session.getAttribute("signedin")?exists)> 
   <#global signedin = false/>
<#else>
  <#if vc.request.session.getAttribute("signedin") = "yes">
     <#global signedin = true/>
  <#else>
      <#global signedin = false/>
  </#if>
</#if>
 <TABLE height="85%" cellSpacing=0 width="100%" border=0>
  <TBODY>
  <TR>
    <TD vAlign=top>
      <TABLE cellSpacing=0 width=600 border=0>
        <TBODY>
        <TR>
          <TD>
            <TABLE height=355 cellSpacing=0 width=200 
            background="${vc.getAppResourceUrl('/images/bkg-sidebar.gif')}" 
            border=0>
              <TBODY>
              <TR>
                <TD><A 
                  href="category?category_id=FISH"><IMG 
                  src="${vc.getAppResourceUrl('/images/nav-fish.gif')}" 
                  border=0></A> <BR><FONT face=Verdana size=1>Saltwater, 
                  Freshwater</FONT> </TD></TR>
              <TR>
                <TD><BR><A 
                  href="category?category_id=DOGS"><IMG 
                  src="${vc.getAppResourceUrl('/images/nav-dogs.gif')}" 
                  border=0></A> <BR><FONT face=Verdana size=1>Various 
                  Breeds</FONT> </TD></TR>
              <TR>
                <TD><BR><A 
                  href="category?category_id=REPTILES"><IMG 
                  src="${vc.getAppResourceUrl('/images/nav-reptiles.gif')}" 
                  border=0></A> <BR><FONT face=Verdana size=1>Lizards, Turtles, 
                  Snakes</FONT> </TD></TR>
              <TR>
                <TD><BR><A 
                  href="category?category_id=CATS"><IMG 
                  src="${vc.getAppResourceUrl('/images/nav-cats.gif')}" 
                  border=0></A> <BR><FONT face=Verdana size=1>Various Breeds, 
                  Exotic Varieties</FONT> </TD></TR>
              <TR>
                <TD><BR><A 
                  href="category?category_id=BIRDS"><IMG 
                  src="${vc.getAppResourceUrl('/images/nav-birds.gif')}" 
                  border=0></A> <BR><FONT face=Verdana size=1>Exotic 
                  Varieties</FONT> <BR></TD></TR></TBODY></TABLE></TD>
          <TD bgColor=white height=300><MAP name=estoremap><AREA shape=RECT 
              alt=Birds coords=72,2,280,250 
              href="category?category_id=BIRDS"><AREA 
              shape=RECT alt=Fish coords=2,180,72,250 
              href="category?category_id=FISH"><AREA 
              shape=RECT alt=Dogs coords=60,250,130,320 
              href="category?category_id=DOGS"><AREA 
              shape=RECT alt=Reptiles coords=140,270,210,340 
              href="category?category_id=REPTILES"><AREA 
              shape=RECT alt=Cats coords=225,240,295,310 
              href="category?category_id=CATS"><AREA 
              shape=RECT alt=Birds coords=280,180,350,250 
              href="category?category_id=BIRDS"></MAP><IMG 
            height=355 src="${vc.getAppResourceUrl('/images/splash.gif')}" 
            width=350 useMap=#estoremap border=0> </TD></TR></TBODY></TABLE></TD></TR>
  <TR>
    <TD vAlign=bottom></TD></TR>
  <TR>
    <TD vAlign=bottom>
      <TABLE cellSpacing=0 width="100%" border=0 <tr>
        <TBODY>
</TBODY></TABLE></TD></TR></TBODY></TABLE>     
