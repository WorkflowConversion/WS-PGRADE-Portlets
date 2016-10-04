package com.workflowconversion.portlet.ui.apptable;

import java.io.File;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.ApplicationField;

/**
 * Processes an uploaded XML file.
 * 
 * @author delagarza
 *
 */
public class XMLFileProcessor extends AbstractFileProcessor {

	XMLFileProcessor(final File serverSideFile, final ApplicationCommittedListener listener,
			final ProgressIndicator progressIndicator, final Label verboseProgressLabel,
			final Set<String> validMiddlewareTypes) {
		super(serverSideFile, listener, progressIndicator, verboseProgressLabel, validMiddlewareTypes);
	}

	@Override
	void parseFile(final File serverSideFile) throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(serverSideFile, new SAXHandler());
	}

	private class SAXHandler extends DefaultHandler {
		Locator locator;
		ApplicationField currentField = null;
		Application currentApplication;
		StringBuilder currentFieldValue;
		private final static String APPLICATION_NODE_NAME = "application";

		@Override
		public void setDocumentLocator(final Locator locator) {
			this.locator = locator;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName,
				final Attributes attributes) throws SAXException {
			if (qName.equalsIgnoreCase(APPLICATION_NODE_NAME)) {
				currentField = null;
				currentFieldValue.setLength(0);
				currentApplication = new Application();
			}
			if (qName.equalsIgnoreCase(ApplicationField.Description.name())) {
				currentField = ApplicationField.Description;
			}
			if (qName.equalsIgnoreCase(ApplicationField.Name.name())) {
				currentField = ApplicationField.Name;
			}
			if (qName.equalsIgnoreCase(ApplicationField.Path.name())) {
				currentField = ApplicationField.Path;
			}
			if (qName.equalsIgnoreCase(ApplicationField.Resource.name())) {
				currentField = ApplicationField.Resource;
			}
			if (qName.equalsIgnoreCase(ApplicationField.ResourceType.name())) {
				currentField = ApplicationField.ResourceType;
			}
			if (qName.equalsIgnoreCase(ApplicationField.Version.name())) {
				currentField = ApplicationField.Version;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equalsIgnoreCase(APPLICATION_NODE_NAME)) {
				XMLFileProcessor.this.addParsedApplication(currentApplication, locator.getLineNumber());
			}
			if (qName.equalsIgnoreCase(ApplicationField.Description.name())) {
				currentApplication.setDescription(getCleanCurrentFieldValue());
			}
			if (qName.equalsIgnoreCase(ApplicationField.Name.name())) {
				currentApplication.setName(getCleanCurrentFieldValue());
			}
			if (qName.equalsIgnoreCase(ApplicationField.Path.name())) {
				currentApplication.setPath(getCleanCurrentFieldValue());
			}
			if (qName.equalsIgnoreCase(ApplicationField.Resource.name())) {
				currentApplication.setResource(getCleanCurrentFieldValue());
			}
			if (qName.equalsIgnoreCase(ApplicationField.ResourceType.name())) {
				currentApplication.setResourceType(getCleanCurrentFieldValue());
			}
			if (qName.equalsIgnoreCase(ApplicationField.Version.name())) {
				currentApplication.setVersion(getCleanCurrentFieldValue());
			}
		}

		private String getCleanCurrentFieldValue() {
			return StringUtils.trimToEmpty(currentFieldValue.toString());
		}

		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			switch (currentField) {
			case Description:
			case Name:
			case Path:
			case Resource:
			case ResourceType:
			case Version:
				currentFieldValue.append(new String(ch, start, length));
				break;
			default:
				// nop
				break;
			}
		}
	}

}
