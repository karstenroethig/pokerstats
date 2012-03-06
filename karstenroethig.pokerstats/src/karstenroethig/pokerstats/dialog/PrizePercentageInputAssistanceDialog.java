package karstenroethig.pokerstats.dialog;

import karstenroethig.pokerstats.enums.PrizeScalingTypeEnum;
import karstenroethig.pokerstats.util.MoneyUtils;
import karstenroethig.pokerstats.util.UIUtils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class PrizePercentageInputAssistanceDialog extends Dialog {

	private int maxPlace;
	
	private int selectionFrom = -1;
	
	private int selectionTo = -1;
	
	private PrizeScalingTypeEnum prizeScalingType;
	
	private Integer value = null;
	
	private Spinner spinnerFromPlace;
	
	private Spinner spinnerToPlace;
	
	private Text textPercentage;

    public PrizePercentageInputAssistanceDialog( Shell parentShell,
    		PrizeScalingTypeEnum prizeScalingType, int maxPlace, int selectionFrom ) {
        super( parentShell );
        
        this.prizeScalingType = prizeScalingType;
        this.maxPlace = maxPlace;
        this.selectionFrom = selectionFrom;
    }

    @Override
    protected void configureShell( Shell newShell ) {
        super.configureShell( newShell );
        newShell.setText( "Eingabeunterstützung Prozentsätze" );
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }

    @Override
    protected Control createDialogArea( Composite parent ) {
    	
        Composite composite = ( Composite ) super.createDialogArea( parent );
        
        composite.setLayout( new GridLayout( 7, false ) );

        GridData gridData = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gridData );

        new Label( composite, SWT.NONE ).setText( "Plätze" );
        
        spinnerFromPlace = UIUtils.createIntegerSpinner( composite, maxPlace );
        spinnerFromPlace.setSelection( selectionFrom );
        
        new Label( composite, SWT.NONE ).setText( "bis" );
        
        spinnerToPlace = UIUtils.createIntegerSpinner( composite, maxPlace );
        
        new Label( composite, SWT.NONE ).setText( "->" );
        
        textPercentage = new Text( composite, SWT.BORDER );
        
        if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
        	new Label( composite, SWT.NONE ).setText( "%" );
        }

        return composite;
    }
    
    @Override
    protected void okPressed() {
    	
    	if( spinnerFromPlace != null ) {
    		selectionFrom = spinnerFromPlace.getSelection();
    	}
    	
    	if( spinnerToPlace != null ) {
    		selectionTo = spinnerToPlace.getSelection();
    	}
    	
    	if( textPercentage != null ) {
    		
    		if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
    			value = MoneyUtils.parsePercentage( textPercentage.getText() );
    		} else if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT ) {
    			value = MoneyUtils.parseAmount( textPercentage.getText() );
    		}
    	}
    	
    	if( selectionFrom == -1 ) {
    		MessageDialog.openError( getShell(), "Eingabefehler", "Für die Platzierungen muss ein Mindestwert angegeben werden." );
    		
    		return;
    	}
    	
    	if( selectionTo == -1 ) {
    		MessageDialog.openError( getShell(), "Eingabefehler", "Für die Platzierungen muss ein Maximalwert angegeben werden." );
    		
    		return;
    	}
    	
    	if( selectionTo > maxPlace ) {
    		MessageDialog.openError( getShell(), "Eingabefehler", "Der Maximalwert für die Platzierungen ist zu groß." );
    		
    		return;
    	}
    	
    	if( selectionFrom > selectionTo ) {
    		MessageDialog.openError( getShell(), "Eingabefehler", "Der Mindestwert für die Platzierung muss kleiner sein als der Maximalwert." );
    		
    		return;
    	}
    	
    	if( value == null ) {
    		
    		if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
    			MessageDialog.openError( getShell(), "Eingabefehler", "Es muss ein Prozentsatz angegeben werden." );
    		} else if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT ) {
    			MessageDialog.openError( getShell(), "Eingabefehler", "Es muss ein Betrag angegeben werden." );
    		} else {
    			MessageDialog.openError( getShell(), "Eingabefehler", "Es muss ein Wert angegeben werden." );
    		}
    		
    		return;
    	}
    	
    	super.okPressed();
    }

	public int getSelectionFrom() {
		return selectionFrom;
	}

	public int getSelectionTo() {
		return selectionTo;
	}

	public Integer getValue() {
		return value;
	}
}
