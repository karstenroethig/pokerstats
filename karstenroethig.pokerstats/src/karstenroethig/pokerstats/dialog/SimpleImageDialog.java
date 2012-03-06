package karstenroethig.pokerstats.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SimpleImageDialog extends Dialog {

	private String title;
	
	private Image image;
	
	public SimpleImageDialog( Shell parentShell, String title, Image image ) {
		super( parentShell );
		
		this.title = title;
		this.image = image;
	}
	
	public static void openDialog( Shell parentShell, String title, Image image ) {
		new SimpleImageDialog( parentShell, title, image ).open();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText( title );
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	}
	
	@Override
	protected Control createContents( Composite parent ) {
		
		Composite composite = new Composite( parent, SWT.NONE );
		
		composite.setLayout( new FillLayout() );
		composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        
        if( image != null ) {
        	Label label = new Label( composite, SWT.NONE );
        	
        	label.setImage( image );
        }
        
        return composite;
	}
}
