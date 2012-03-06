package karstenroethig.pokerstats.dialog;

import karstenroethig.pokerstats.validation.ValidationResult;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ValidationResultDialog extends Dialog {

    private String  title;
    
    private String message;
    
    private ValidationResult validationResult;

    public ValidationResultDialog( Shell parentShell, String title, String message, ValidationResult validationResult ) {
        super( parentShell );
        
        this.title = title;
        this.message = message;
        this.validationResult = validationResult;
    }

    public static void openError( Shell parentShell, String  title, String  message, ValidationResult validationResult ) {
    	ValidationResultDialog dialog = new ValidationResultDialog( parentShell, title, message, validationResult );

        dialog.open();
    }

    @Override
    protected void configureShell( Shell newShell ) {
        super.configureShell( newShell );
        newShell.setText( title );
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
    }

    @Override
    protected Control createDialogArea( Composite parent ) {
    	
    	Image errorImage = parent.getDisplay().getSystemImage( SWT.ICON_ERROR);
    	
        Composite composite = ( Composite ) super.createDialogArea( parent );
        Composite panel = new Composite( composite, SWT.NONE );
        panel.setLayout( new GridLayout( 2, false ) );

        GridData gridData = new GridData( /*GridData.FILL_BOTH*/ );
        panel.setLayoutData( gridData );

        Label labelImage = new Label( panel, SWT.NONE );
    	labelImage.setImage( errorImage );
        
        if ( message != null ) {
        	new Label( panel, SWT.NONE ).setText( message );
        } else {
            new Label( panel, SWT.NONE );
        }

        // Leerzellen
        new Label( panel, SWT.NONE );
        new Label( panel, SWT.NONE );
        
        if( validationResult != null && validationResult.hasErrors() ) {
        	gridData = new GridData( GridData.FILL_BOTH );
            gridData.widthHint = 450;
            gridData.heightHint = 200;
            gridData.horizontalSpan = 2;
            
            Text textResult = new Text( panel, SWT.READ_ONLY | SWT.BORDER | SWT.MULTI
            		| SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL );
            textResult.setLayoutData( gridData );
            textResult.setText( validationResult.createSingleLineMessage() );
        }

        return composite;
    }
}
