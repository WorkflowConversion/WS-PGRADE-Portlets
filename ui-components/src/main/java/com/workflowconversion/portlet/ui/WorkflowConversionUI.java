package com.workflowconversion.portlet.ui;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinPortletService;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.workflowconversion.portlet.core.app.ResourceProvider;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.validation.PortletSanityCheck;

import hu.sztaki.lpds.information.com.ServiceType;
import hu.sztaki.lpds.information.local.InformationBase;
import hu.sztaki.lpds.information.local.PropertyLoader;
import hu.sztaki.lpds.storage.inf.PortalStorageClient;

/**
 * The entry point of every portlet in this project.
 * 
 * @author delagarza
 *
 */
@Theme("mytheme")
public abstract class WorkflowConversionUI extends UI {

	private static final long serialVersionUID = -1691439006632825854L;

	private final static Logger LOG = LoggerFactory.getLogger(WorkflowConversionUI.class);

	protected PortletUser currentUser;
	protected final Collection<ResourceProvider> applicationProviders;
	protected final PortletSanityCheck portletSanityCheck;

	/**
	 * Constructor.
	 * 
	 * @param portletSanityCheck
	 *            the portlet sanity check.
	 * @param applicationProviders
	 *            the application providers.
	 */
	protected WorkflowConversionUI(final PortletSanityCheck portletSanityCheck,
			final Collection<ResourceProvider> applicationProviders) {
		Validate.notEmpty(applicationProviders, "applicationProviders cannot be null or empty");
		Validate.notNull(portletSanityCheck, "portletSanityCheck cannot be null");
		this.applicationProviders = applicationProviders;
		this.portletSanityCheck = portletSanityCheck;
	}

	@Override
	public final void init(final VaadinRequest vaadinRequest) {
		LOG.info("initializing WorkflowConversionApplication");

		this.currentUser = extractCurrentUser(VaadinPortletService.getCurrentPortletRequest());
		final Layout content;

		if (currentUser.isAuthenticated()) {
			if (portletSanityCheck.isPortletProperlyInitialized()) {
				// happy path!
				initApplicationProviders();
				content = prepareContent();
			} else {
				content = new SimpleWarningContent.Builder().setIconLocation("../runo/icons/64/lock.png")
						.setLongDescription(
								"This portlet has not been properly initialized. If the problem persists after restarting gUSE, please check the logs and report the problem, for this might be caused by a bug or a configuration error.")
						.newWarningWindow();
			}
		} else {
			content = new SimpleWarningContent.Builder().setIconLocation("../runo/icons/64/attention.png")
					.setLongDescription("You need to be logged-in to access this portlet.").newWarningWindow();
		}
		setContent(content);
	}

	/**
	 * Prepares the main window to set.
	 * 
	 * @return the main window of this application.
	 */
	protected abstract Layout prepareContent();

	private void initApplicationProviders() {
		// init any app provider that needs initialization
		LOG.info("initializing ApplicationProviders");
		for (final ResourceProvider provider : applicationProviders) {
			if (provider.needsInit()) {
				if (LOG.isInfoEnabled()) {
					LOG.info("initializing " + provider.getClass());
				}
				provider.init();
			} else {
				if (LOG.isInfoEnabled()) {
					LOG.info("ApplicationProvider " + provider.getClass()
							+ " does not require initialization or has already been initialized.");
				}
			}
		}
	}

	private PortletUser extractCurrentUser(final PortletRequest request) {
		try {
			return new PortletUser(PortalUtil.getUser(request));
		} catch (SystemException | PortalException e) {
			// there isn't much we can do, really
			throw new ApplicationException("Could not extract current user from the PortletRequest.", e);
		}
	}

	// copies workflow into guse's internal storage... it's magic
	private void importWorkflow(final File serverSideFile) {
		try {
			if (serverSideFile == null) {
				throw new NullPointerException("serverSideFile is null, something went wrong with the data transfer.");
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("importing Workflow from " + serverSideFile.getCanonicalPath());
			}
			Hashtable<String, Object> h = new Hashtable<String, Object>();
			ServiceType st = InformationBase.getI().getService("wfs", "portal", new Hashtable<String, Object>(),
					new Vector<Object>());
			h.put("senderObj", "ZipFileSender");
			h.put("portalURL", PropertyLoader.getInstance().getProperty("service.url"));
			h.put("wfsID", st.getServiceUrl());
			// h.put("userID", currentRequest.getRemoteUser());

			Hashtable<String, Object> hsh = new Hashtable<String, Object>();
			st = InformationBase.getI().getService("storage", "portal", hsh, new Vector<Object>());
			PortalStorageClient psc = (PortalStorageClient) Class.forName(st.getClientObject()).newInstance();
			psc.setServiceURL(st.getServiceUrl());
			psc.setServiceID("/receiver");
			psc.fileUpload(serverSideFile, "fileName", h);
			LOG.info("workflow has been imported");
		} catch (Exception e) {
			// mainWindow.showNotification(new Notification("Workflow Importer",
			// "Could not import workflow." + e.getMessage(), Notification.TYPE_ERROR_MESSAGE));
			LOG.error("Error while importing workflow", e);
		}
	}
}
