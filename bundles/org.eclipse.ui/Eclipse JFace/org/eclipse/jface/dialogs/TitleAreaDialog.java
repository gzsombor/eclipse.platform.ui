package org.eclipse.jface.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Point;
/**
 * A dialog that has a title area for displaying a title and an image as well as
 * a common area for displaying a description, a message, or an error message.
 * <p>
 * This dialog class may be subclassed.
 */
public class TitleAreaDialog extends Dialog {
	/**
	 * Image registry key for error message image.
	 */
	public static final String DLG_IMG_TITLE_ERROR = DLG_IMG_MESSAGE_ERROR;//$NON-NLS-1$

	/**
	 * Image registry key for banner image (value <code>"dialog_title_banner_image"</code>).
	 */
	public static final String DLG_IMG_TITLE_BANNER = "dialog_title_banner_image";//$NON-NLS-1$

	/**
	 * Message type constant used to display an info icon with the message.
	 * @since 2.0
	 * @deprecated
	 */
	public final static String INFO_MESSAGE = "INFO_MESSAGE"; //$NON-NLS-1$
	
	/**
	 * Message type constant used to display a warning icon with the message.
	 * @since 2.0
	 * @deprecated
	 */
	public final static String WARNING_MESSAGE = "WARNING_MESSAGE"; //$NON-NLS-1$

	// Space between the top of the title area and the title
	private static final int H_INDENT_TITLE = 7;
	// Space between the left of the title area and the title
	private static final int V_INDENT_TITLE = 8;
	// Space between an image and a label
	private static final int H_GAP_IMAGE = 5;
	// Space between the title bottom and message area top
	private static final int V_INDENT_MSG_AREA = 3;
	// Space between the left of the title area and the message
	private static final int H_INDENT_MSG = 11;
	// Space between the message area top and the top of the message label
	private static final int V_INDENT_MSG = 3;
	//Minimum height of the title image
	private static final int MIN_TITLE_IMAGE_HEIGHT = 64;
	//Minimum width of the title image
	private static final int MIN_TITLE_IMAGE_WIDTH = 64;
	//Minimun dialog width (in dialog units)
	private static final int MIN_DIALOG_WIDTH = 350;
	//Minimun dialog height (in dialog units)
	private static final int MIN_DIALOG_HEIGHT = 150;
	
	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(DLG_IMG_TITLE_BANNER, ImageDescriptor.createFromFile(TitleAreaDialog.class, "images/title_banner.gif"));//$NON-NLS-1$
	}

	private Label titleLabel;
	private Label titleImage;
	private Label fillerLabel;
	private Color titleAreaColor;
	private RGB titleAreaRGB;

	private String message = ""; //$NON-NLS-1$
	private String errorMessage;
	private Label messageLabel;

	private Label messageImageLabel;
	private Image messageImage;
	private Color normalMsgAreaBackground;
	private Color errorMsgAreaBackground;
	private Image errorMsgImage;
	private boolean showingError = false;
	private boolean titleImageLargest = true;

/**
 * Instantiate a new title area dialog.
 *
 * @param parentShell the parent SWT shell
 */
public TitleAreaDialog(Shell parentShell) {
	super(parentShell);
}

/*
 * @see Dialog.createContents(Composite)
 */
protected Control createContents(Composite parent) {
	
	// initialize the dialog units
	initializeDialogUnits(parent);

	
	FormLayout layout = new FormLayout();
	parent.setLayout(layout);
	FormData data = new FormData();
	data.top = new FormAttachment(0,0);
	data.bottom = new FormAttachment(100,100);
	parent.setLayoutData(data);
	
	Control top = createTitleArea(parent);
	
	//Now create a work area for the rest of the dialog
	Composite composite = new Composite(parent, SWT.NULL);
	GridLayout childLayout = new GridLayout();
	childLayout.marginHeight = 0;
	childLayout.marginWidth = 0;
	childLayout.verticalSpacing = 0;
	composite.setLayout(childLayout);
	
	FormData childData = new FormData();
	childData.top = new FormAttachment(top);
	childData.right = new FormAttachment(100,0);
	childData.left = new FormAttachment(0,0);
	childData.bottom = new FormAttachment(100,0);
	composite.setLayoutData(childData);
	
	composite.setFont(JFaceResources.getDialogFont());

	// initialize the dialog units
	initializeDialogUnits(composite);
	
	// create the dialog area and button bar
	dialogArea = createDialogArea(composite);
	buttonBar = createButtonBar(composite);
	
	return parent;
}

/**
 * Creates and returns the contents of the upper part 
 * of this dialog (above the button bar).
 * <p>
 * The <code>Dialog</code> implementation of this framework method
 * creates and returns a new <code>Composite</code> with
 * standard margins and spacing. Subclasses should override.
 * </p>
 *
 * @param the parent composite to contain the dialog area
 * @return the dialog area control
 */
protected Control createDialogArea(Composite parent) {
	// create the top level composite for the dialog area
	Composite composite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	layout.verticalSpacing = 0;
	layout.horizontalSpacing = 0;
	composite.setLayout(layout);
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	composite.setFont(parent.getFont());

	// Build the separator line
	Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
	titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	return composite;
}
/**
 * Creates the dialog's title area.
 *
 * @param parent the SWT parent for the title area widgets
 * @return Control with the highest x axis value.
 */
private Control createTitleArea(Composite parent) {
		
	// add a dispose listener
	parent.addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {

			if (titleAreaColor != null)
				titleAreaColor.dispose();
			if (errorMsgAreaBackground != null)
				errorMsgAreaBackground.dispose();
		}
	});
	

	// Determine the background color of the title bar
	Display display = parent.getDisplay();
	Color background;
	Color foreground;
	if (titleAreaRGB != null) {
		titleAreaColor = new Color(display, titleAreaRGB);
		background = titleAreaColor;
		foreground = null;
	} else {
		background = JFaceColors.getBannerBackground(display);
		foreground = JFaceColors.getBannerForeground(display);
	}	

	int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
	int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
	parent.setBackground(background);
	
	// Dialog image @ right
	titleImage = new Label(parent, SWT.CENTER);
	titleImage.setBackground(background);
	titleImage.setImage(JFaceResources.getImage(DLG_IMG_TITLE_BANNER));
	
	FormData imageData = new FormData();
	imageData.top = new FormAttachment(0,verticalSpacing);
	imageData.right = new FormAttachment(100,horizontalSpacing);
	titleImage.setLayoutData(imageData);
	
	// Title label @ top, left
	titleLabel = new Label(parent, SWT.LEFT);
	JFaceColors.setColors(titleLabel,foreground,background);
	titleLabel.setFont(JFaceResources.getBannerFont());
	titleLabel.setText(" ");//$NON-NLS-1$

	FormData titleData = new FormData();
	titleData.top = new FormAttachment(0,verticalSpacing);
	titleData.right = new FormAttachment(titleImage);
	titleData.left = new FormAttachment(0,horizontalSpacing);
	titleLabel.setLayoutData(titleData);
	
	// Message image @ bottom, left
	messageImageLabel = new Label(parent, SWT.CENTER);
	messageImageLabel.setBackground(background);

	// Message label @ bottom, center
	messageLabel = new Label(parent, SWT.WRAP);
	JFaceColors.setColors(messageLabel,foreground,background);
	messageLabel.setText(" \n "); // two lines//$NON-NLS-1$
	messageLabel.setFont(JFaceResources.getDialogFont());

	
	// Message image @ bottom, left
	fillerLabel = new Label(parent, SWT.CENTER);
	fillerLabel.setBackground(background);
	
	setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);
	
	determineTitleImageLargest();
	if(titleImageLargest)
		return titleImage;
	else
		return messageLabel;
	
}

/**
 * Determine if the title image is larger than the title message
 * and message area. This is used for layout decisions.
 */
private void determineTitleImageLargest (){
	
	int titleY = titleImage.computeSize(SWT.DEFAULT,SWT.DEFAULT).y;
	
	int labelY = titleLabel.computeSize(SWT.DEFAULT,SWT.DEFAULT).y;
	labelY += messageLabel.computeSize(SWT.DEFAULT,SWT.DEFAULT).y;
	
	titleImageLargest = titleY > labelY;
}
	

/**
 * Set the layout values for the messageLabel, messageImageLabel and 
 * fillerLabel for the case where there is a normal message.
 * @param verticalSpacing int The spacing between widgets on the vertical axis.
 * @param horizontalSpacing int The spacing between widgets on the horizontal axis.
 */

private void setLayoutsForNormalMessage(
	int verticalSpacing,
	int horizontalSpacing) {
	FormData messageImageData = new FormData();
	messageImageData.top = new FormAttachment(titleLabel,verticalSpacing);
	messageImageData.left = new FormAttachment(0,horizontalSpacing);
	messageImageLabel.setLayoutData(messageImageData);
	
	FormData messageLabelData = new FormData();
	messageLabelData.top = new FormAttachment(titleLabel,verticalSpacing);
	messageLabelData.right = new FormAttachment(titleImage);
	messageLabelData.left = new FormAttachment(messageImageLabel,horizontalSpacing);
	
	if(titleImageLargest)
		messageLabelData.bottom = new FormAttachment(titleImage,0,SWT.BOTTOM);
		
	messageLabel.setLayoutData(messageLabelData);
	
	FormData fillerData = new FormData();
	fillerData.left = new FormAttachment(0,horizontalSpacing);
	fillerData.top = new FormAttachment(messageImageLabel,0);
	fillerData.bottom = new FormAttachment(messageLabel,0,SWT.BOTTOM);
	fillerLabel.setLayoutData(fillerData);	
}
/**
 * The <code>TitleAreaDialog</code> implementation of this 
 * <code>Window</code> methods returns an initial size which
 * is at least some reasonable minimum.
 *
 * @return the initial size of the dialog
 */
protected Point getInitialSize() {
	Point shellSize = super.getInitialSize();
	return new Point(
		Math.max(convertHorizontalDLUsToPixels(MIN_DIALOG_WIDTH), shellSize.x),
		Math.max(convertVerticalDLUsToPixels(MIN_DIALOG_HEIGHT), shellSize.y));
}

/**
 * Retained for backward compatibility.
 * 
 * Returns the title area composite. There is no composite in this
 * implementation so the shell is returned.
 * @deprecated
 */
protected Composite getTitleArea() {
	return getShell();
}/**
 * Returns the title image label.
 * 
 * @return the title image label
 */
protected Label getTitleImageLabel() {
	return titleImage;
}
/**
 * Display the given error message. The currently displayed message
 * is saved and will be redisplayed when the error message is set
 * to <code>null</code>.
 *
 * @param newErrorMessage the newErrorMessage to display or <code>null</code>
 */
public void setErrorMessage(String newErrorMessage) {
	// Any change?
	if (errorMessage == null ? newErrorMessage == null : errorMessage.equals(newErrorMessage))
		return;
	
	errorMessage = newErrorMessage;
	if (errorMessage == null) {
		if (showingError) {
			// we were previously showing an error
			showingError = false;
			setMessageBackgrounds(false);
		}

		// show the message
		// avoid calling setMessage in case it is overridden to call setErrorMessage, 
		// which would result in a recursive infinite loop
		if (message == null)	//this should probably never happen since setMessage does this conversion....
			message = "";		//$NON-NLS-1$
		updateMessage(message);
		messageImageLabel.setImage(messageImage);
		setImageLabelVisible(messageImage != null);
		messageLabel.setToolTipText(message);

	} else {
		
		//Add in a space for layout purposes
		errorMessage = " " + errorMessage;
		updateMessage(errorMessage);
		messageLabel.setToolTipText(errorMessage);
		if (!showingError) {
			// we were not previously showing an error
			showingError = true;

			// lazy initialize the error background color and image
			if (errorMsgAreaBackground == null) {
				errorMsgAreaBackground = JFaceColors.getErrorBackground(messageLabel.getDisplay());
				errorMsgImage = JFaceResources.getImage(DLG_IMG_TITLE_ERROR);
			}

			// show the error	
			normalMsgAreaBackground = messageLabel.getBackground();
			setMessageBackgrounds(true);
			messageImageLabel.setImage(errorMsgImage);
			setImageLabelVisible(true);
		}
	}
	layoutForNewMessage();
}

/**
 * Re-layout the labels for the new message.
 */
private void layoutForNewMessage(){
	
	int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
	int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
	
	//If there are no images then layout as normal
	if (errorMessage == null && messageImage == null) {
		setImageLabelVisible(false);
		
		setLayoutsForNormalMessage(verticalSpacing,horizontalSpacing);
		
	} else {
		messageImageLabel.setVisible(true);
		fillerLabel.setVisible(true);
		
		/**
		 * Note that we do not use horizontalSpacing here 
		 * as when the background of the messages changes
		 * there will be gaps between the icon label and the
		 * message that are the background color of the shell.
		 * We add a leading space elsewhere to compendate for this.
		 */

		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(titleLabel, verticalSpacing);
		messageImageLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(messageImageLabel, 0);
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(messageLabel, 0, SWT.BOTTOM);
		data.right = new FormAttachment(messageImageLabel, 0, SWT.RIGHT);
		fillerLabel.setLayoutData(data);
		
		FormData messageLabelData = new FormData();
		messageLabelData.top = new FormAttachment(titleLabel,verticalSpacing);
		messageLabelData.right = new FormAttachment(titleImage);
		messageLabelData.left = new FormAttachment(messageImageLabel,0);
		
		if(titleImageLargest)
			messageLabelData.bottom = new FormAttachment(titleImage,0,SWT.BOTTOM);
			
		messageLabel.setLayoutData(messageLabelData);
		
		}
	getShell().layout(true);
	
}


/**
 * Set the message text. If the message line currently displays an error,
 * the message is saved and will be redisplayed when the error message is set
 * to <code>null</code>.
 * <p>
 * Shortcut for <code>setMessage(newMessage, IMessageProvider.NONE)</code>
 * </p> 
 * 
 * @param newMessage the message, or <code>null</code> to clear
 *   the message
 */
public void setMessage(String newMessage) {
	setMessage(newMessage, IMessageProvider.NONE);
}
/**
 * Sets the message for this dialog with an indication of what type
 * of message it is.
 * <p>
 * The valid message types are one of <code>NONE</code>, 
 * <code>INFORMATION</code>, <code>WARNING</code>, or <code>ERROR</code>.
 * </p>
 * <p>
 * Note that for backward compatibility, a message of type <code>ERROR</code> 
 * is different than an error message (set using <code>setErrorMessage</code>). 
 * An error message overrides the current message until the error message is 
 * cleared. This method replaces the current message and does not affect the 
 * error message.
 * </p>
 *
 * @param newMessage the message, or <code>null</code> to clear
 *   the message
 * @param newType the message type
 * @since 2.0
 */
public void setMessage(String newMessage, int newType) {
	Image newImage = null;
	
	if (newMessage != null) {
		switch (newType) {
			case IMessageProvider.NONE :
				break;
			case IMessageProvider.INFORMATION :
				newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_INFO);
				break;
			case IMessageProvider.WARNING :
				newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_WARNING);
				break;
			case IMessageProvider.ERROR :
				newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_ERROR);
				break;
		}
	}
	
	showMessage(newMessage, newImage);
}
/**
 * Show the new message
 */
private void showMessage(String newMessage, Image newImage) {
	// Any change?
	if (message.equals(newMessage) && messageImage == newImage)
		return;

	message = newMessage;
	if (message == null)
		message = "";//$NON-NLS-1$

	//If there is an image then add in a space to the message
	//for layout purposes
	if(newImage != null)
		message = " " + message; //$NON-NLS-1$
		
	messageImage = newImage;

	if (!showingError) {
		// we are not showing an error
		updateMessage(message);
		messageImageLabel.setImage(messageImage);
		setImageLabelVisible(messageImage != null);
		messageLabel.setToolTipText(message);
		layoutForNewMessage();
	}
}

/**
 * Update the contents of the messageLabel.
 * @param String the message to use
 */
private void updateMessage(String newMessage) {
	
	//Be sure there are always 2 lines for layout purposes
	if(newMessage != null && newMessage.indexOf('\n') == -1)
		newMessage = newMessage + "\n ";
		
	messageLabel.setText(newMessage);
}

/**
 * Sets the title to be shown in the title area of this dialog.
 *
 * @param newTitle the title show 
 */
public void setTitle(String newTitle) {
	if (titleLabel == null)
		return;
	String title = newTitle;
	if (title == null)
		title = "";//$NON-NLS-1$
	titleLabel.setText(title);
}
/**
 * Sets the title bar color for this dialog.
 *
 * @param color the title bar color
 */
public void setTitleAreaColor(RGB color) {
	titleAreaRGB = color;
}
/**
 * Sets the title image to be shown in the title area of this dialog.
 *
 * @param newTitle the title image show 
 */
public void setTitleImage(Image newTitleImage) {
	titleImage.setImage(newTitleImage);
	titleImage.setVisible(newTitleImage != null);
}

/**
 * Make the label used for displaying error images visible
 * depending on boolean.
 */
private void setImageLabelVisible(boolean visible){
	messageImageLabel.setVisible(visible);
	fillerLabel.setVisible(visible);
	
}

/**
 * Set the message backgrounds to be the error or normal color
 * depending on whether or not showingError is true.
 */
private void setMessageBackgrounds(boolean showingError){
	
	Color color;
	if(showingError)
		color = errorMsgAreaBackground;
	else
		color = normalMsgAreaBackground;
		
	messageLabel.setBackground(color);
	messageImageLabel.setBackground(color);
	fillerLabel.setBackground(color);
}
		
}
