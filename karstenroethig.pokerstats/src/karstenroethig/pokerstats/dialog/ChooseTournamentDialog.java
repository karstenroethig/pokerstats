package karstenroethig.pokerstats.dialog;


import java.util.List;

import karstenroethig.pokerstats.model.Tournament;
import karstenroethig.pokerstats.model.TournamentModel;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ChooseTournamentDialog extends Dialog {

	private Tournament tournamentExclude;
	
	private Tournament tournament = null;
	
	private ComboViewer comboViewerTournament;

    public ChooseTournamentDialog( Shell parentShell, Tournament tournamentExclude ) {
        super( parentShell );
        
        this.tournamentExclude = tournamentExclude;
    }

    @Override
    protected void configureShell( Shell newShell ) {
        super.configureShell( newShell );
        newShell.setText( "Auswahl Turnier" );
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }

    @Override
    protected Control createDialogArea( Composite parent ) {
    	
        Composite composite = ( Composite ) super.createDialogArea( parent );
        
        composite.setLayout( new GridLayout( 2, false ) );

        GridData gridData = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gridData );

        new Label( composite, SWT.NONE ).setText( "Turnier auswählen" );
        
        comboViewerTournament = new ComboViewer( composite );
        
		comboViewerTournament.setContentProvider( new TournamentContentProvider() );
		comboViewerTournament.setLabelProvider( new TournamentLabelProvider() );
		comboViewerTournament.setFilters( new ViewerFilter[]{ new TournamentViewerFilter() });
		comboViewerTournament.getCombo().setVisibleItemCount( 10 );
		comboViewerTournament.setInput( TournamentModel.getInstance().getTournaments() );
		
		return composite;
    }
    
    @Override
    protected void okPressed() {
    	
    	if( comboViewerTournament == null
    			|| comboViewerTournament.getSelection() == null
    			|| comboViewerTournament.getSelection().isEmpty() ) {
    		MessageDialog.openError( getShell(), "Eingabefehler", "Es muss ein Turnier ausgewählt werden." );
    		
    		return;
    	}
    	
    	IStructuredSelection selection = ( IStructuredSelection )comboViewerTournament.getSelection();
    	tournament = (Tournament)selection.getFirstElement();
    	
    	super.okPressed();
    }

    public Tournament getTournament() {
    	return tournament;
    }
	
	private class TournamentContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public Object[] getElements(Object inputElement) {
			
			if( inputElement instanceof List ) {
				return ((List<Tournament>) inputElement).toArray( new Tournament[0] );
			}

			return null;
		}
		
	}
	
	private class TournamentLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			
			if( element instanceof Tournament ) {
				
				Tournament tournament = (Tournament)element;
				
				return tournament.getDesciption();
			}
			
			return super.getText( element );
		}
		
	}
	
	private class TournamentViewerFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			
			if( element instanceof Tournament ) {
				
				Tournament tournament = (Tournament)element;
				
				return !tournament.equals( tournamentExclude );
			}
			
			return false;
		}
		
	}
}
