package karstenroethig.pokerstats.dialog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ExceptionDialog extends Dialog {

	static DateFormat dateFormat = DateFormat.getDateTimeInstance();

    private String  message;

    private Throwable throwable;

    private String  title;

    public ExceptionDialog( Shell parentShell, String title, String message, Throwable throwable ) {
        super( parentShell );
        
        this.title = title;
        this.message = message;
        this.throwable = throwable;
    }

    public static int openExceptionDialog( Shell parentShell, String  title, String  message, Throwable throwable ) {
    	ExceptionDialog dialog = new ExceptionDialog( parentShell, title, message, throwable );

        return dialog.open();
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
    	
        Composite composite = ( Composite ) super.createDialogArea( parent );
        Composite panel = new Composite( composite, SWT.NONE );
        panel.setLayout( new GridLayout( 2, false ) );

        GridData gridData = new GridData( /*GridData.FILL_BOTH*/ );
        panel.setLayoutData( gridData );

        if ( message != null ) {
            new Label( panel, SWT.NONE );
            new Label( panel, SWT.NONE ).setText( "Folgender Fehler ist aufgetreten: \n\n" + message );
        } else {
            new Label( panel, SWT.NONE );
            new Label( panel, SWT.NONE ).setText( "Es ist ein Fehler aufgetreten." );
        }

        // Leerzellen
        new Label( panel, SWT.NONE );
        new Label( panel, SWT.NONE );

        if( throwable != null ) {
            gridData = new GridData( GridData.FILL_BOTH );
            gridData.widthHint = 450;
            gridData.heightHint = 200;
            new Label( panel, SWT.NONE ).setText( "Exception" );
            Text throwableInfo = new Text( panel, SWT.READ_ONLY | SWT.BORDER | SWT.MULTI
            		| SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL );

            throwableInfo.setLayoutData( gridData );

            StringWriter stringWriter = new StringWriter();
            PrintWriter  printWriter = new PrintWriter( stringWriter );
            printWriter.println( message );
            printWriter.println();
            throwable.printStackTrace( printWriter );
            throwableInfo.setText( stringWriter.toString() );
        }

        return composite;
    }
}
