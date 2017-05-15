<?xml version="1.0" encoding="UTF-8"?>
<!--
  This stylesheet updates thew servlet version to 3.1
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="web-app">
    <web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee/"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
        version="3.1">
      <xsl:apply-templates />
    </web-app>
  </xsl:template>
  
  <xsl:template match="*" >
    <xsl:element name="{local-name()}">
      <xsl:apply-templates select="@*|node()" namespace="http://xmlns.jcp.org/xml/ns/javaee/"/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
