package com.mh.mvc.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLReader {
	public static ArrayList<String> getNodeValues(String xmlPath, String nodeName) {
		// xmlPath = xmlPath.replace("%20", " ");
		ArrayList<String> names = new ArrayList<String>();
		String nodeAttr = "class";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Element root = null;
		factory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder builder;
		Document xmldoc = null;
		try {
			builder = factory.newDocumentBuilder();
			File xmlFile = new File(xmlPath);
			xmldoc = builder.parse(xmlFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		root = xmldoc.getDocumentElement();
		NodeList fatherNodeList = root.getElementsByTagName(nodeName);
		if(fatherNodeList == null || fatherNodeList.getLength() < 1) {
			return names;
		}
		Element nameNode = (Element) fatherNodeList.item(0);
		NodeList nodeList = nameNode.getChildNodes();
		int length = nodeList.getLength();
		
		for (int i = 0; i < length; i++) {
			Node node = nodeList.item(i);
			// 前后的空白也会被看作节点
			if (node instanceof Element) {
				NamedNodeMap attrs = node.getAttributes();
				if (attrs.getLength() > 0 && attrs != null) {
					Node attr = attrs.getNamedItem(nodeAttr);
					String name = attr.getNodeValue();
					names.add(name);
				}
			}
		}
		return names;
	}
}
