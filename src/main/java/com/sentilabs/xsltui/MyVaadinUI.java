package com.sentilabs.xsltui;

import javax.servlet.annotation.WebServlet;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import net.sf.saxon.s9api.*;

import java.io.StringReader;
import java.io.StringWriter;

@Theme("mytheme")
@SuppressWarnings("serial")
public class MyVaadinUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = true, ui = MyVaadinUI.class, widgetset = "com.sentilabs.xsltui.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        setContent(mainLayout);

        final HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);
        horizontalLayout.setHeight(300, Unit.PIXELS);

        final TextArea xmlTA = new TextArea("XML");
        xmlTA.setWidth(100, Unit.PERCENTAGE);
        xmlTA.setHeight(100, Unit.PERCENTAGE);
        horizontalLayout.addComponent(xmlTA);

        final TextArea xsltTA = new TextArea("XSLT");
        xsltTA.setWidth(100, Unit.PERCENTAGE);
        xsltTA.setHeight(100, Unit.PERCENTAGE);
        horizontalLayout.addComponent(xsltTA);
        mainLayout.addComponent(horizontalLayout);

        final TextArea xsltResult = new TextArea("XSLT result");
        xsltResult.setWidth(100, Unit.PERCENTAGE);
        xsltResult.setHeight(300, Unit.PIXELS);
        mainLayout.addComponent(xsltResult);

        final Label resLabel = new Label();
        mainLayout.addComponent(resLabel);

        final Button btnTransformJava = new Button("Transform using Java");
        btnTransformJava.setDisableOnClick(true);
        btnTransformJava.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                TransformerFactory tFactory = TransformerFactory.newInstance();
                StringReader xmlReader = new StringReader(xsltTA.getValue());
                Transformer transformer;
                try {
                    transformer = tFactory.newTransformer(new StreamSource(xmlReader));
                } catch (TransformerConfigurationException e) {
                    btnTransformJava.setEnabled(true);
                    resLabel.setValue("Error while parsing XSLT: " + e.getMessage());
                    //e.printStackTrace();
                    return;
                }
                StringWriter xmlWriter = new StringWriter();
                try {
                    transformer.transform(new StreamSource(new StringReader(xmlTA.getValue())), new StreamResult(xmlWriter));
                } catch (TransformerException e) {
                    btnTransformJava.setEnabled(true);
                    resLabel.setValue("Error while parsing XML: " + e.getMessage());
                    //e.printStackTrace();
                    return;
                }
                btnTransformJava.setEnabled(true);
                resLabel.setValue("");
                xsltResult.setValue(xmlWriter.toString());
            }
        });
        final Button btnTransformSaxon = new Button("Transform using Saxon");
        btnTransformSaxon.setDisableOnClick(true);
        btnTransformSaxon.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                Processor proc = new Processor(false);
                XsltCompiler comp = proc.newXsltCompiler();
                StringReader xmlReader = new StringReader(xsltTA.getValue());
                XsltExecutable exp;
                try {
                    exp = comp.compile(new StreamSource(xmlReader));
                } catch (SaxonApiException e) {
                    btnTransformSaxon.setEnabled(true);
                    resLabel.setValue("Error while parsing XSLT: " + e.getMessage());
                    //e.printStackTrace();
                    return;
                }
                XdmNode source;
                try {
                    source = proc.newDocumentBuilder().build(new StreamSource(new StringReader(xmlTA.getValue())));
                } catch (SaxonApiException e) {
                    btnTransformSaxon.setEnabled(true);
                    resLabel.setValue("Error while parsing XML: " + e.getMessage());
                    //e.printStackTrace();
                    return;
                }
                Serializer out = new Serializer();
                out.setOutputProperty(Serializer.Property.INDENT, "yes");
                StringWriter xmlWriter = new StringWriter();
                out.setOutputWriter(xmlWriter);
                XsltTransformer trans = exp.load();
                trans.setInitialContextNode(source);
                trans.setDestination(out);
                try {
                    trans.transform();
                } catch (SaxonApiException e) {
                    btnTransformSaxon.setEnabled(true);
                    resLabel.setValue("Error while performing XSLT: " + e.getMessage());
                    //e.printStackTrace();
                    return;
                }
                btnTransformSaxon.setEnabled(true);
                resLabel.setValue("");
                xsltResult.setValue(xmlWriter.toString());
            }
        });

        Button btnTestData = new Button("Test data");
        btnTestData.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                xmlTA.setValue("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                        "<messages>\n" +
                        "    <message>\n" +
                        "        <text>Hello </text>\n" +
                        "    </message>\n" +
                        "    <message>\n" +
                        "        <text>World!</text>\n" +
                        "    </message>\n" +
                        "</messages>");
                xsltTA.setValue("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<xsl:stylesheet version=\"2.0\"\n" +
                        "    xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" >\n" +
                        "    <xsl:output indent=\"yes\" method=\"html\"/>\n" +
                        "    <xsl:template match=\"/\">\n" +
                        "        <html>\n" +
                        "        <body>\n" +
                        "        <h1>\n" +
                        "            <xsl:for-each select=\".//message\">\n" +
                        "                <xsl:value-of select=\"./text\"/>\n" +
                        "            </xsl:for-each>\n" +
                        "            </h1>\n" +
                        "        </body>\n" +
                        "        </html>\n" +
                        "    </xsl:template>\n" +
                        "</xsl:stylesheet>");
                xsltResult.setValue("");
            }
        });

        Label spacer = new Label();
        spacer.setWidth(100, Unit.PERCENTAGE);

        final HorizontalLayout btnLayout = new HorizontalLayout();
        btnLayout.setWidth(100, Unit.PERCENTAGE);
        btnLayout.addComponent(btnTestData);

        btnLayout.addComponent(spacer);
        btnLayout.setExpandRatio(spacer, 1.0f);

        btnLayout.addComponent(btnTransformSaxon);
        btnLayout.addComponent(btnTransformJava);
        mainLayout.addComponent(btnLayout);
    }

}
