package karstenroethig.pokerstats.editor;

import karstenroethig.pokerstats.dialog.ExceptionDialog;
import karstenroethig.pokerstats.dialog.ValidationResultDialog;
import karstenroethig.pokerstats.model.Participation;
import karstenroethig.pokerstats.model.ParticipationModel;
import karstenroethig.pokerstats.model.SaveResult;
import karstenroethig.pokerstats.model.Tournament;
import karstenroethig.pokerstats.model.TournamentModel;
import karstenroethig.pokerstats.util.Colors;
import karstenroethig.pokerstats.util.DateUtils;
import karstenroethig.pokerstats.util.Images;
import karstenroethig.pokerstats.util.MoneyUtils;
import karstenroethig.pokerstats.util.UIUtils;
import karstenroethig.pokerstats.validation.ValidationResult;
import karstenroethig.pokerstats.view.ParticipationOverviewView;
import karstenroethig.pokerstats.view.TournamentOverviewView;
import karstenroethig.pokerstats.viewers.TournamentContentProvider;
import karstenroethig.pokerstats.viewers.TournamentLabelProvider;
import net.ffxml.swtforms.builder.PanelBuilder;
import net.ffxml.swtforms.layout.CellConstraints;
import net.ffxml.swtforms.layout.FormLayout;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public class ParticipationEditor extends AbstractEditorPart {
	
	public static final String ID = "karstenroethig.pokerstats.editor.ParticipationEditor";
	
	private ParticipationEditorInput input;
	
	private ComboViewer comboViewerTournament;
	
	private DateTime dateTimeDate;
	
	private Spinner spinnerParticipants;
	
	private Spinner spinnerPlace;
	
	private Spinner spinnerRebuys;
	
	private Spinner spinnerRebuysOthers;
	
	private Spinner spinnerAddons;
	
	private Spinner spinnerAddonsOthers;
	
	private Label labelBuyin;
	
	private Label labelPrizeMoneyTotal;
	
	private Label labelPrizeMoney;
	
	private Label labelBenefit;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		
		if( input instanceof ParticipationEditorInput == false ) {
			throw new RuntimeException( "Wrong input" );
		}
		
		this.input = (ParticipationEditorInput)input;
		this.input.reload();
		super.init( site, input );
	}

	@Override
	public void createPartControl(Composite parent) {
		
		// Hintergrundfarbe auf weiß setzen
		parent.setBackground( Colors.getColor( Colors.WHITE ) );
		parent.setBackgroundMode( SWT.INHERIT_FORCE );
		
		/*
		 * Erstellen der Eingabefelder
		 */
		// Turnier
		Link linkTournament = new Link( parent, SWT.NONE );
		linkTournament.setText( "<a>Turnier</a>" );
		linkTournament.addListener( SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent( Event event ) {
				
				if( comboViewerTournament == null
		    			|| comboViewerTournament.getSelection() == null
		    			|| comboViewerTournament.getSelection().isEmpty() ) {
					return;
		    	}
		    	
		    	IStructuredSelection selection = ( IStructuredSelection )comboViewerTournament.getSelection();
		    	Tournament tournament = (Tournament)selection.getFirstElement();
				IWorkbenchPage page = getEditorSite().getPage();
				TournamentEditorInput input = new TournamentEditorInput( tournament );
				
				try{
					page.openEditor( input, TournamentEditor.ID );
				}catch( PartInitException ex ) {
					throw new RuntimeException( ex );
				}
			}
		});
		
		Combo comboTournament = createComboTournaments( parent );
		
		// Datum
		dateTimeDate = new DateTime( parent, SWT.BORDER | SWT.DROP_DOWN | SWT.DATE );
		
		// Teilnehmer
		spinnerParticipants = UIUtils.createIntegerSpinner( parent, Participation.MAX_PARTICIPANTS );
		
		// Platzierung
		spinnerPlace = UIUtils.createIntegerSpinner( parent, Participation.MAX_PARTICIPANTS );
		
		// Rebuys
		spinnerRebuys = UIUtils.createIntegerSpinner( parent, Participation.MAX_REBUYS );
		spinnerRebuysOthers = UIUtils.createIntegerSpinner( parent, Participation.MAX_REBUYS );
		spinnerAddons = UIUtils.createIntegerSpinner( parent, Participation.MAX_REBUYS );
		spinnerAddonsOthers = UIUtils.createIntegerSpinner( parent, Participation.MAX_REBUYS );
		setRebuyEnabled( false );
		
		// Calc-Button
		Button buttonCalc = new Button( parent, SWT.PUSH );
		buttonCalc.setImage( Images.getImage( Images.ICON_CALCULATOR ) );
		buttonCalc.setToolTipText( "Auswertung berechnen" );
		buttonCalc.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				calc();
			}
		});
		
		// Buy-in
		labelBuyin = new Label( parent, SWT.NONE );
		
		// Preispool
		labelPrizeMoneyTotal = new Label( parent, SWT.NONE );
		
		// Preisgeld
		labelPrizeMoney = new Label( parent, SWT.NONE );
		
		// Gewinn
		labelBenefit = new Label( parent, SWT.NONE );
		
		/*
		 * Layout der Eingabefelder
		 */
		String colSpec = "3dlu, l:p, 3dlu, f:p, 3dlu, p, f:p:g, 3dlu";
		String rowSpec = "3dlu, p, 2dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 10dlu, p, p, 5dlu, p, 7dlu, p, 7dlu, p, 7dlu, p, 3dlu";
		FormLayout layout = new FormLayout( colSpec, rowSpec );
		PanelBuilder builder = new PanelBuilder( layout, parent );
		CellConstraints cc = new CellConstraints();
		
		builder.addSeparator( "Gespieltes Turnier", cc.xyw( 2, 2, 6 ) );
		
		// Turnier
		int row = 4;
		builder.add( linkTournament, cc.xy( 2, row ) );
		builder.add( comboTournament, cc.xyw( 4, row, 4 ) );
		
		// Datum
		row = 6;
		builder.addLabel( "Datum", cc.xy( 2, row ) );
		builder.add( dateTimeDate, cc.xy( 4, row ) );
		
		// Teilnehmer
		row = 8;
		builder.addLabel( "Teilnehmer", cc.xy( 2, row ) );
		builder.add( spinnerParticipants, cc.xy( 4, row ) );
		
		// Platzierung
		row = 10;
		builder.addLabel( "Platzierung", cc.xy( 2, row ) );
		builder.add( spinnerPlace, cc.xy( 4, row ) );
		
		// Rebuys
		row = 12;
		builder.addLabel( "Rebuys (eigene/andere)", cc.xy( 2, row ) );
		builder.add( spinnerRebuys, cc.xy( 4, row ) );
		builder.add( spinnerRebuysOthers, cc.xy( 6, row ) );
		
		row = 14;
		builder.addLabel( "Add-ons (eigene/andere)", cc.xy( 2, row ) );
		builder.add( spinnerAddons, cc.xy( 4, row ) );
		builder.add( spinnerAddonsOthers, cc.xy( 6, row ) );
		
		builder.addSeparator( "Auswertung", cc.xyw( 2, 16, 6 ) );
		
		// Calc-Button
		builder.add( buttonCalc, cc.xy( 2, 17 ) );
		
		// Buy-in
		row = 19;
		builder.addLabel( "Buy-in", cc.xy( 2, row ) );
		builder.add( labelBuyin, cc.xyw( 4, row, 4 ) );
		
		// Preispool
		row = 21;
		builder.addLabel( "Preispool", cc.xy( 2, row ) );
		builder.add( labelPrizeMoneyTotal, cc.xyw( 4, row, 4 ) );
		
		// Preisgeld
		row = 23;
		builder.addLabel( "Preisgeld", cc.xy( 2, row ) );
		builder.add( labelPrizeMoney, cc.xyw( 4, row, 4 ) );
		
		// Gewinn
		row = 25;
		builder.addLabel( "Gewinn", cc.xy( 2, row ) );
		builder.add( labelBenefit, cc.xyw( 4, row, 4 ) );
		
		/*
		 * Füllen der Eingabefelder
		 */
		fillControls();
		
		/*
		 * Dirty-Marker hinzufügen
		 */
		addDirtyMarker( comboViewerTournament );
		addDirtyMarker( dateTimeDate );
		addDirtyMarker( spinnerParticipants );
		addDirtyMarker( spinnerPlace );
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

		/*
		 * Werte aus Feldern übernehmen
		 */
		Participation participation = readControls();
		
		/*
		 * Automatische Auswertung, falls noch nicht geschehen
		 */
		if( participation.getBuyinPrize() == null
				|| participation.getBuyinFee() == null
				|| participation.getPrizeMoneyTotal() == null
				|| participation.getPrizeMoney() == null
				|| participation.getBenefit() == null ) {
			
			if( ParticipationModel.getInstance().calcSummaryProperties( participation ) ) {
				fillSummaryControls( participation );
			}
		}
		
		/*
		 * Validierung
		 */
		ValidationResult validationResult = ParticipationModel.getInstance().validate( participation );
		
		if( validationResult.hasErrors() ) {
			String title = "Validierungsfehler";
			String message = "Die Turnier-Teilnahme kann nicht gespeichert werden, da folgende Fehler aufgetreten sind:";
			
			ValidationResultDialog.openError( getSite().getShell(), title, message, validationResult);
			
			return;
		}
		
		/*
		 * Speichern
		 */
		SaveResult saveResult = ParticipationModel.getInstance().save( participation );
		
		if( saveResult.isSaveSuccessful() == false ) {
			String title = "Fehler beim Speichern";

			if( saveResult.getThrowable() != null ) {
				ExceptionDialog.openExceptionDialog( getSite().getShell(), title, saveResult.getErrorMessage(), saveResult.getThrowable() );
			} else {
				MessageDialog.openError( getSite().getShell(), title, saveResult.getErrorMessage() );
			}
			
			return;
		}
		
		/*
		 * Übersicht der Turnier-Teilnahmen aktualisieren
		 */
		IWorkbenchPage page = getEditorSite().getPage();
		IViewPart viewPart = page.findView( ParticipationOverviewView.ID );
		
		if( viewPart != null && viewPart instanceof ParticipationOverviewView ) {
			ParticipationOverviewView view = (ParticipationOverviewView)viewPart;

			view.refresh();
		}
		
		super.doSave( monitor );
	}

	@Override
	public void setFocus() {
	}
	
	private Combo createComboTournaments( Composite parent ) {
		
		comboViewerTournament = new ComboViewer( parent );
		comboViewerTournament.setContentProvider( new TournamentContentProvider() );
		comboViewerTournament.setLabelProvider( new TournamentLabelProvider() );
		comboViewerTournament.getCombo().setVisibleItemCount( 10 );
		comboViewerTournament.setInput( TournamentModel.getInstance().getTournaments() );
		
		comboViewerTournament.addSelectionChangedListener( new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged( SelectionChangedEvent event ) {
				
				IStructuredSelection selection = ( IStructuredSelection )event.getSelection();
				
				if( selection != null ) {
					Tournament tournament = (Tournament)selection.getFirstElement();
					
					setRebuyEnabled( tournament.getRebuy() != null && tournament.getRebuy() == true );
				}
			}
		});
		
		TournamentOverviewView.registerTournamentComboViewer( comboViewerTournament );
		
		return comboViewerTournament.getCombo();
	}
	
	private void fillControls() {
		
		Participation participation = input.getParticipation();
		
		if( participation == null ) {
			return;
		}
		
		// Turnier
		if( participation.getTournament() != null ) {
			comboViewerTournament.setSelection( new StructuredSelection( participation.getTournament() ) );
			
			setRebuyEnabled(
					participation.getTournament().getRebuy() != null
					&& participation.getTournament().getRebuy() == true
				);
		}
		
		// Datum
		if( participation.getDate() != null ) {
			int[] dateValues = DateUtils.getDateValues( participation.getDate() );
			
			dateTimeDate.setYear( dateValues[0] );
			dateTimeDate.setMonth( dateValues[1] );
			dateTimeDate.setDay( dateValues[2] );
		}
		
		// Teilnehmer
		if( participation.getParticipants() != null ) {
			spinnerParticipants.setSelection( participation.getParticipants() );
		}
		
		// Platzierung
		if( participation.getPlace() != null ) {
			spinnerPlace.setSelection( participation.getPlace() );
		}
		
		// Rebuy
		if( participation.getRebuys() != null ) {
			spinnerRebuys.setSelection( participation.getRebuys() );
		}

		if( participation.getRebuysOthers() != null ) {
			spinnerRebuysOthers.setSelection( participation.getRebuysOthers() );
		}
		
		if( participation.getAddons() != null ) {
			spinnerAddons.setSelection( participation.getAddons() );
		}
		
		if( participation.getAddonsOthers() != null ) {
			spinnerAddonsOthers.setSelection( participation.getAddonsOthers() );
		}
		
		// Werte für die Auswertung übernehmen
		fillSummaryControls( participation );
	}
	
	private void fillSummaryControls( Participation participation ) {
		
		if( participation == null ) {
			return;
		}
		
		// Buy-in
		if( participation.getBuyinPrize() != null && participation.getBuyinFee() != null ) {
			long buyin = participation.getBuyinPrize() + participation.getBuyinFee();
			
			labelBuyin.setText( MoneyUtils.formatAmount( buyin, true ) );
		} else {
			labelBuyin.setText( "???" );
		}
		
		// Preispool
		if( participation.getPrizeMoneyTotal() != null ) {
			labelPrizeMoneyTotal.setText( MoneyUtils.formatAmount( participation.getPrizeMoneyTotal(), true ) );
		} else {
			labelPrizeMoneyTotal.setText( "???" );
		}
		
		// Preisgeld
		if( participation.getPrizeMoney() != null ) {
			labelPrizeMoney.setText( MoneyUtils.formatAmount( participation.getPrizeMoney(), true ) );
		} else {
			labelPrizeMoney.setText( "???" );
		}
		
		// Gewinn
		labelBenefit.setForeground( labelBenefit.getParent().getForeground() );
		
		if( participation.getBenefit() != null ) {
			labelBenefit.setText( MoneyUtils.formatAmount( participation.getBenefit(), true ) );
			
			if( participation.getBenefit() < 0 ) {
				labelBenefit.setForeground( Colors.getColor( Colors.RED ) );
			}
		} else {
			labelBenefit.setText( "???" );
		}
	}
	
	private Participation readControls() {
		
		Participation participation = input.getParticipation();
		Tournament tournament = null;
		
		if( comboViewerTournament != null
    			&& comboViewerTournament.getSelection() != null
    			&& comboViewerTournament.getSelection().isEmpty() == false ) {
			IStructuredSelection selection = ( IStructuredSelection )comboViewerTournament.getSelection();
			tournament = (Tournament)selection.getFirstElement();
	    	participation.setTournament( tournament ); 
    	}
		
		if( dateTimeDate != null ) {
			int day = dateTimeDate.getDay();
			int month = dateTimeDate.getMonth();
			int year = dateTimeDate.getYear();
			
			participation.setDate( DateUtils.createDate( year, month, day ) );
		}
		
		if( spinnerParticipants != null ) {
			participation.setParticipants( spinnerParticipants.getSelection() );
		}
		
		if( spinnerPlace != null ) {
			participation.setPlace( spinnerPlace.getSelection() );
		}
		
		if( spinnerRebuys != null ) {
			participation.setRebuys( spinnerRebuys.getSelection() );
		}
		
		if( spinnerRebuysOthers != null ) {
			participation.setRebuysOthers( spinnerRebuysOthers.getSelection() );
		}
		
		if( spinnerAddons != null ) {
			participation.setAddons( spinnerAddons.getSelection() );
		}
		
		if( spinnerAddonsOthers != null ) {
			participation.setAddonsOthers( spinnerAddonsOthers.getSelection() );
		}
		
		return participation;
	}
	
	private void setRebuyEnabled( boolean enabled ) {
		
		if( spinnerRebuys != null ) {
			spinnerRebuys.setEnabled( enabled );
			
			if( !enabled ) {
				spinnerRebuys.setSelection( 0 );
			}
		}
		
		if( spinnerRebuysOthers != null ) {
			spinnerRebuysOthers.setEnabled( enabled );
			
			if( !enabled ) {
				spinnerRebuysOthers.setSelection( 0 );
			}
		}
		
		if( spinnerAddons != null ) {
			spinnerAddons.setEnabled( enabled );
			
			if( !enabled ) {
				spinnerAddons.setSelection( 0 );
			}
		}
		
		if( spinnerAddonsOthers != null ) {
			spinnerAddonsOthers.setEnabled( enabled );
			
			if( !enabled ) {
				spinnerAddonsOthers.setSelection( 0 );
			}
		}
	}
	
	private void calc() {
		
		Participation participation = readControls();
		
		if( ParticipationModel.getInstance().calcSummaryProperties( participation ) ) {
			fillSummaryControls( participation );
			
			markDirty();
		}
	}

}
