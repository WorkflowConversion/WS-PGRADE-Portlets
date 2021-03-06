package com.workflowconversion.portlet.ui.table;

import com.vaadin.ui.Window;

/**
 * Modal dialog to edit elements.
 * 
 * @author delagarza
 *
 */
public class AbstractGenericElementDetailsDialog<T> extends Window {

	private static final long serialVersionUID = 3605623408453706094L;

	protected final Object itemId;
	protected final T element;
	protected final boolean allowEdition;
	protected final GenericElementDetailsSavedListener<T> listener;

	/**
	 * Constructor.
	 * 
	 * @param element
	 *            the element to edit.
	 * @param allowEdition
	 *            whether edition is allowed.
	 */
	public AbstractGenericElementDetailsDialog(final Object itemId, final T element,
			final GenericElementDetailsSavedListener<T> listener, final boolean allowEdition) {
		this.itemId = itemId;
		this.element = element;
		this.listener = listener;
		this.allowEdition = allowEdition;
		setModal(true);
		setClosable(false);
		setResizable(false);
	}

}
