<% 
    response.setHeader("Content-Disposition", "attachment; filename=\"launch_manager.jnlp\"");
    Boolean secure = ((HttpServletRequest)pageContext.getRequest()).isSecure();
    String prot = "http";
    if (secure) prot =" https";
    String serverURL = prot+"://"+request.getServerName()+":"+request.getServerPort();
%><%@page contentType="application/x-java-jnlp-file" pageEncoding="UTF-8"%><?xml version="1.0" encoding="UTF-8" standalone="no"?>
<jnlp codebase="<%= serverURL %>/HI3Author/sysop" href="launch.jsp" spec="1.0+">
    <information>
        <title>HIManager</title>
        <vendor>HyperImage VRE</vendor>
        <homepage href="http://hyperimage.ws/"/>
        <description>Manager Client for HIAuthor_3.0</description>
        <description kind="short">HISystemManager_3.0</description>
    </information>
    <update check="always"/>
    <security>
<all-permissions/>
</security>
    <resources>
        <j2se version="1.7+" />
        <jar href="HI3SystemManager.jar" main="true"/>
    <jar href="lib/swing-layout-1.0.4.jar"/>
    <jar href="../editor/HI3Author-EditorClient_3.0.jar"/>
</resources>
    <application-desc main-class="org.hyperimage.sysop.client.Main">
</application-desc>
</jnlp>
