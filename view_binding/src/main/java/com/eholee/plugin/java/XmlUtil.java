package com.eholee.plugin.java;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.gradle.api.logging.Logging;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class XmlUtil {
    private static Document document = null;

    public static Document getXmlDocument(String xmlPath){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(xmlPath);
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            document = reader.read(fis);
        } catch (IOException | SAXException | DocumentException var7) {
            Logging.getLogger(CommonUtil.class).error(String.format("path %s get xml document failed:error:%s", xmlPath, var7.getMessage()));
        } finally {
            IOUtil.closeAll(new AutoCloseable[]{fis});
        }
        return document;
    }


    public static Element getRootElement(String xmlPath){
        return getXmlDocument(xmlPath).getRootElement();
    }

    public static boolean getRootElementViewBindingEnable(String xmlPath){
        Element rootElement = getXmlDocument(xmlPath).getRootElement();
        Attribute attribute = rootElement.attribute(QName.get(CommonUtil.XML_NODE_ATTR_NAME, Namespace.get("eholee" , CommonUtil.XML_NAMESPACE)));
        return attribute == null || !"false".equalsIgnoreCase(attribute.getValue());
    }

    public static void getAllIdElement(Element element , List<String> ids){
        if (element.isRootElement()){
            // 空实现
            /*System.out.println(element.getName()+"is root element");
            Attribute attribute = element.attribute(QName.get("id" , Namespace.get("ohos" , "http://schemas.huawei.com/res/ohos")));
            if (attribute!=null){
                ids.add(attribute.getValue().split(":")[1].concat("_").concat(element.getName()));
            }*/
        }
        Iterator iterator = element.elementIterator();
        while (iterator.hasNext()){
            Element childNode = (Element)iterator.next();
            Attribute attribute = childNode.attribute(QName.get("id" , Namespace.get("ohos" , "http://schemas.huawei.com/res/ohos")));
            if (attribute!=null){
                ids.add(attribute.getValue().split(":")[1].concat("_").concat(childNode.getName()));
            }
            getAllIdElement(childNode ,ids);
        }
    }


}
