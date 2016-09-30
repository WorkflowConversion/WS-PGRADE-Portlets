package com.workflowconversion.portlet.ui.apptable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.ApplicationField;
import com.workflowconversion.portlet.core.exception.InvalidApplicationFieldException;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;

import dci.data.Middleware;

/**
 * Modal dialog to add new applications.
 * 
 * @author delagarza
 *
 */
class AddApplicationDialog extends Window {

	private static final long serialVersionUID = 2514225841272484511L;

	private static final String LARGE_FIELD_WIDTH = "15em";
	private static final String COMMON_FIELD_WIDTH = "10em";

	private final Form applicationForm;
	private final Application application;
	private final MiddlewareProvider middlewareProvider;
	private final ApplicationCommittedListener listener;

	/**
	 * Constructor.
	 * 
	 * @param middlewareProvider
	 *            The middleware provider.
	 */
	AddApplicationDialog(final MiddlewareProvider middlewareProvider, final ApplicationCommittedListener listener) {
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		Validate.notNull(listener, "listener cannot be null");
		super.setCaption("Add application");
		super.setModal(true);
		this.middlewareProvider = middlewareProvider;
		this.application = newEmptyApplication();
		this.applicationForm = new Form();
		this.listener = listener;
		setUpLayout();
	}

	private Application newEmptyApplication() {
		final Application app = new Application();
		app.setId("automatically generated");
		app.setName("");
		app.setDescription("");
		app.setPath("");
		app.setResource("");
		app.setResourceType("");
		app.setVersion("");
		return app;
	}

	void removeFromParentWindow() {
		getParent().removeWindow(this);
	}

	void setUpLayout() {
		// adapted from: http://demo.vaadin.com/sampler-for-vaadin6#FormBasic
		applicationForm.setCaption("New application");
		applicationForm.setWriteThrough(false);
		applicationForm.setInvalidCommitted(false);
		applicationForm.setFormFieldFactory(new ApplicationFieldFactory());
		applicationForm.setItemDataSource(new BeanItem<Application>(application));
		applicationForm.setVisibleItemProperties(
				new String[] { ApplicationField.Name.getMemberName(), ApplicationField.Version.getMemberName(),
						ApplicationField.Path.getMemberName(), ApplicationField.Description.getMemberName(),
						ApplicationField.ResourceType.getMemberName(), ApplicationField.Resource.getMemberName() });

		final Button addApplicationButton = new Button("Add", new Button.ClickListener() {
			private static final long serialVersionUID = -2999251588313488770L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					applicationForm.commit();
					try {
						// notify listeners that there's a committed application
						listener.applicationCommitted(application);
					} finally {
						// always close the dialog
						removeFromParentWindow();
					}
				} catch (SourceException | InvalidValueException e) {
					// ignore
				}
			}
		});
		addApplicationButton.setDescription("Add application");
		applicationForm.getFooter().addComponent(addApplicationButton);
		applicationForm.getFooter().setMargin(true, false, true, true);

		final VerticalLayout layout = (VerticalLayout) getContent();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeUndefined();
		layout.addComponent(applicationForm);
	}

	private class ApplicationFieldFactory extends DefaultFieldFactory {

		private static final long serialVersionUID = 1952414851060513902L;

		@Override
		public Field createField(final Item item, final Object propertyId, final Component uiContext) {
			final String applicationField = (String) propertyId;
			final Field field;
			// yes, we could have used a switch statement, but case extensions must be constant expressions
			if (applicationField.equals(ApplicationField.Name.getMemberName())) {
				field = createRequiredTextField("Application name:", "Please enter a name for the application",
						ApplicationField.Name.getMaxLength());
			} else if (applicationField.equals(ApplicationField.Version.getMemberName())) {
				field = createRequiredTextField("Version:", "Please enter a valid version",
						ApplicationField.Version.getMaxLength());
			} else if (applicationField.equals(ApplicationField.Path.getMemberName())) {
				field = createRequiredTextField("Executable path:", "Please enter a valid application path",
						ApplicationField.Path.getMaxLength());
			} else if (applicationField.equals(ApplicationField.Description.getMemberName())) {
				field = createOptionalTextArea("Description:", ApplicationField.Description.getMaxLength());
			} else if (applicationField.equals(ApplicationField.Resource.getMemberName())) {
				field = createRequiredTextField("Resource:",
						"Please enter a valid resource (i.e., IP address, DNS name of resource)",
						ApplicationField.Resource.getMaxLength());
			} else if (applicationField.equals(ApplicationField.ResourceType.getMemberName())) {
				field = createResourceTypeComboBox();
			} else if (applicationField.equals(ApplicationField.Id.getMemberName())) {
				field = createRequiredTextField("Id:", "Unique ID of the application (automatically generated)",
						ApplicationField.Id.getMaxLength());
				field.setReadOnly(true);
				field.setEnabled(false);
			} else {
				throw new InvalidApplicationFieldException(applicationField);
			}
			return field;
		}

		private Field createResourceTypeComboBox() {
			final ComboBox resourceTypeComboBox = new ComboBox("Resource type:");
			resourceTypeComboBox.setInputPrompt("Please select a resource type");
			resourceTypeComboBox.setWidth(COMMON_FIELD_WIDTH);
			resourceTypeComboBox.setNullSelectionAllowed(false);
			setRequired(resourceTypeComboBox, "Plese select a resource type");
			resourceTypeComboBox.addValidator(new NonBlankStringValidator("Please select a resource type"));
			// add all of the possible resource types
			for (final Middleware middleware : middlewareProvider.getAllMiddlewares()) {
				resourceTypeComboBox.addItem(middleware.getType());
			}
			return resourceTypeComboBox;
		}

		private Field createRequiredTextField(final String caption, final String requiredError, final int fieldSize) {
			final Field textField = new TextField(caption);
			textField.addValidator(new NonBlankMaxLengthStringValidator(fieldSize));
			setRequired(textField, requiredError);
			textField.setWidth(COMMON_FIELD_WIDTH);
			return textField;
		}

		private void setRequired(final Field field, final String requiredError) {
			field.setRequired(true);
			field.setRequiredError(requiredError);
		}

		private Field createOptionalTextArea(final String caption, final int maxLength) {
			final Field textArea = new TextArea(caption);
			textArea.addValidator(new StringLengthValidator(
					"Please enter a description no longer than " + maxLength + " characters.", -1, maxLength, true));
			textArea.setRequired(false);
			textArea.setWidth(LARGE_FIELD_WIDTH);
			return textArea;
		}
	}

	private static class NonBlankMaxLengthStringValidator extends AbstractValidator {

		private static final long serialVersionUID = 8476005512295626122L;
		private final int maxLength;

		private NonBlankMaxLengthStringValidator(final int maxLength) {
			this("The field cannot be empty, blank, contain only whitespaces or contain more than " + maxLength
					+ " characters.", maxLength);
		}

		private NonBlankMaxLengthStringValidator(final String errorMessage, final int maxLength) {
			super(errorMessage);
			this.maxLength = maxLength;
		}

		@Override
		public boolean isValid(final Object value) {
			if (value == null) {
				return false;
			}
			return StringUtils.isNotBlank(value.toString()) && value.toString().length() <= maxLength;
		}
	}

	private static class NonBlankStringValidator extends NonBlankMaxLengthStringValidator {

		private static final long serialVersionUID = 1584625763507132085L;

		private NonBlankStringValidator() {
			super("The field cannot be empty, blank or contain only whitespaces.", Integer.MAX_VALUE);
		}

		private NonBlankStringValidator(final String message) {
			super(message, Integer.MAX_VALUE);
		}
	}

}
