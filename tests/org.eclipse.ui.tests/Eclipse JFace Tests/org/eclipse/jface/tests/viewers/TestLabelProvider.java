package org.eclipse.jface.tests.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;

public class TestLabelProvider extends LabelProvider {
	
	static Image fgImage= null;
	
/**
 *
 */
public static Image getImage() {
	if (fgImage == null)
		fgImage = ImageDescriptor.createFromFile(TestLabelProvider.class, "images/java.gif").createImage();
	return fgImage;
}
	public Image getImage(Object element) {
		return getImage();
	}
	public String getText(Object element) {
		String label= element.toString();
		return label+ " <rendered>";
	}
}
