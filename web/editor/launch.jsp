<% 
    response.setHeader("Content-Disposition", "attachment; filename=\"launch.jnlp\"");
    Boolean secure = ((HttpServletRequest)pageContext.getRequest()).isSecure();
    String prot = "http";
    if (secure) prot =" https";
    String serverURL = prot+"://"+request.getServerName()+":"+request.getServerPort();
%><%@page contentType="application/x-java-jnlp-file" pageEncoding="UTF-8"%><?xml version="1.0" encoding="UTF-8" standalone="no"?>
<jnlp codebase="<%= serverURL %>/HI3Author/editor" href="launch.jsp" spec="1.0+">
    <information>
        <title>HIEditor_3.0-EditorClient</title>
        <vendor>HyperImage VRE</vendor>
        <homepage href="http://hyperimage.ws/"/>
        <description>HIEditor_3.0-EditorClient</description>
        <description kind="short">HIEditor_3.0-EditorClient</description>
    </information>
    <update check="always"/>
    <security>
<all-permissions/>
</security>
    <resources>
        <j2se version="1.7+" java-vm-args="-Xms500m -Xmx1000m" max-heap-size="1000m" initial-heap-size="500m" />
        <jar href="HI3Author-EditorClient_3.0.jar" main="true"/>
    <jar href="lib/jai_codec.jar"/>
<jar href="lib/jai_core.jar"/>
<jar href="lib/jaxb-impl.jar"/>
<jar href="lib/jaxb-xjc.jar"/>
<jar href="lib/FastInfoset.jar"/>
<jar href="lib/gmbal-api-only.jar"/>
<jar href="lib/ha-api.jar"/>
<jar href="lib/javax.mail_1.4.jar"/>
<jar href="lib/jaxws-rt.jar"/>
<jar href="lib/jaxws-tools.jar"/>
<jar href="lib/management-api.jar"/>
<jar href="lib/mimepull.jar"/>
<jar href="lib/policy.jar"/>
<jar href="lib/saaj-impl.jar"/>
<jar href="lib/stax-ex.jar"/>
<jar href="lib/stax2-api.jar"/>
<jar href="lib/streambuffer.jar"/>
<jar href="lib/woodstox-core-asl.jar"/>
<jar href="lib/jaxws-api.jar"/>
<jar href="lib/jsr181-api.jar"/>
<jar href="lib/javax.annotation.jar"/>
<jar href="lib/saaj-api.jar"/>
<jar href="lib/activation.jar"/>
<jar href="lib/jaxb-api.jar"/>
<jar href="lib/jsr173_1.0_api.jar"/>
<jar href="lib/swing-layout-1.0.4.jar"/>
</resources>
    <application-desc main-class="org.hyperimage.client.Main">
    <argument><%= serverURL %>/HI3Author</argument>
</application-desc>
</jnlp>
