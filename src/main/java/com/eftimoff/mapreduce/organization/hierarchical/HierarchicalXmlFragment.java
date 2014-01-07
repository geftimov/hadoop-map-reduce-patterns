package com.eftimoff.mapreduce.organization.hierarchical;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class HierarchicalXmlFragment {
	private static final DocumentBuilderFactory dbf = DocumentBuilderFactory
			.newInstance();
	private final Document doc;

	public HierarchicalXmlFragment(String post, List<String> comments)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = dbf.newDocumentBuilder();
		doc = builder.newDocument();

		// Copy parent node to document
		Element postEl = getXmlElementFromString(post);
		Element toAddPostEl = doc.createElement("post");

		// Copy the attributes of the original post element to the new one
		copyAttributesToElement(postEl.getAttributes(), toAddPostEl);

		// For each comment, copy it to the "post" node
		for (String commentXml : comments) {
			Element commentEl = getXmlElementFromString(commentXml);
			Element toAddCommentEl = doc.createElement("comments");

			// Copy the attributes of the original comment element to
			// the new one
			copyAttributesToElement(commentEl.getAttributes(), toAddCommentEl);

			// Add the copied comment to the post element
			toAddPostEl.appendChild(toAddCommentEl);
		}

		// Add the post element to the document
		doc.appendChild(toAddPostEl);
	}

	public static Element getXmlElementFromString(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = dbf.newDocumentBuilder();

		return builder.parse(new InputSource(new StringReader(xml)))
				.getDocumentElement();
	}

	private void copyAttributesToElement(NamedNodeMap attributes,
			Element element) {
		for (int i = 0; i < attributes.getLength(); ++i) {
			Attr toCopy = (Attr) attributes.item(i);
			element.setAttribute(toCopy.getName(), toCopy.getValue());
		}
	}

	private String memoizedToString = null;

	@Override
	public String toString() {
		// Transform the document into a String of XML and return
		if (memoizedToString != null) {
			return memoizedToString;
		}
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));

			// Replace all new line characters with an empty string to have
			// one record per line.
			memoizedToString = writer.getBuffer().toString()
					.replaceAll("\n|\r", "");

		} catch (Exception e) {
			e.printStackTrace();
			memoizedToString = "";
		}

		return memoizedToString;
	}
}
