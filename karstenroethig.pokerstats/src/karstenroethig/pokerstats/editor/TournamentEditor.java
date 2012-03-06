package karstenroethig.pokerstats.editor;

import java.util.List;

import karstenroethig.pokerstats.dialog.ChooseTournamentDialog;
import karstenroethig.pokerstats.dialog.ExceptionDialog;
import karstenroethig.pokerstats.dialog.PrizePercentageInputAssistanceDialog;
import karstenroethig.pokerstats.dialog.SimpleImageDialog;
import karstenroethig.pokerstats.dialog.ValidationResultDialog;
import karstenroethig.pokerstats.enums.PrizeScalingTypeEnum;
import karstenroethig.pokerstats.model.PrizePercentage;
import karstenroethig.pokerstats.model.PrizeScaling;
import karstenroethig.pokerstats.model.SaveResult;
import karstenroethig.pokerstats.model.Tournament;
import karstenroethig.pokerstats.model.TournamentModel;
import karstenroethig.pokerstats.util.Colors;
import karstenroethig.pokerstats.util.Images;
import karstenroethig.pokerstats.util.MoneyUtils;
import karstenroethig.pokerstats.util.UIUtils;
import karstenroethig.pokerstats.validation.ValidationResult;
import karstenroethig.pokerstats.view.TournamentOverviewView;
import net.ffxml.swtforms.builder.PanelBuilder;
import net.ffxml.swtforms.layout.CellConstraints;
import net.ffxml.swtforms.layout.FormLayout;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public class TournamentEditor extends AbstractEditorPart {
	
	public static final String ID = "karstenroethig.pokerstats.editor.TournamentEditor";
	
	private static final int COL_EMPTY = 0;
	
	private static final int COL_1_PARTICIPANTS_FROM = 1;
	
	private static final int COL_1_PARTICIPANTS_TO = 2;
	
	private static final int COL_1_PARTICIPANTS_IN_PRIZES = 3;
	
	private static final int COL_2_PLACE = 1;
	
	private static final int COL_2_PERCENTAGE = 2;
	
	private TournamentEditorInput input;
	
	private Text textDescription;
	
	private Spinner spinnerBuyinPrize;
	
	private Spinner spinnerBuyinFee;
	
	private Button checkRebuy;
	
	private Combo comboPrizeScalingType;
	
	private TableViewer tableViewerPrizeScalings;
	
	private TableViewer tableViewerPrizePercentages;
	
	private Action actionDelete;
	
	private Action actionInputAssistance;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		
		if( input instanceof TournamentEditorInput == false ) {
			throw new RuntimeException( "Wrong input" );
		}
		
		this.input = (TournamentEditorInput)input;
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
		// Bezeichnung
		textDescription = new Text( parent, SWT.BORDER );
		textDescription.setTextLimit( Tournament.MAX_LENGTH_DESCRIPTION );
		
		// Buyin
		spinnerBuyinPrize = UIUtils.createMoneySpinner( parent, Tournament.MAX_BUYIN_PRIZE );
		spinnerBuyinPrize.setToolTipText( "Buy-in (Preisgeldanteil)" );
		
		Label labelBuyin2 = new Label( parent, SWT.NONE );
		labelBuyin2.setText( "+" );
		
		spinnerBuyinFee = UIUtils.createMoneySpinner( parent, Tournament.MAX_BUYIN_FEE );
		spinnerBuyinFee.setToolTipText( "Buy-in (Gebührenanteil)" );
		
		// Optionen
		checkRebuy = new Button( parent, SWT.CHECK );
		checkRebuy.setText( "Rebuy möglich" );
		
		// Art der Preisverteilung
		comboPrizeScalingType = new Combo( parent, SWT.READ_ONLY );
		comboPrizeScalingType.setItems( PrizeScalingTypeEnum.getValues() );
		comboPrizeScalingType.addModifyListener( new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				tableViewerPrizePercentages.refresh();
			}
		});
		
		// Tabelle für Staffelung der bezahlten Plätze
		Table tablePrizeScalings = createTablePrizeScalings( parent );
		
		// Tabelle für Preisanteil der einzelnen Platzierungen
		Table tablePrizePercentages = createTablePrizePercentages( parent );
		
		/*
		 * Layout der Eingabefelder
		 */
		String colSpec = "3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, f:p:g(0.4), 5dlu, f:p:g(0.6), 3dlu";
		String rowSpec = "3dlu, p, 2dlu, p, 3dlu, p, 3dlu, p, 10dlu, p, 2dlu, p, 3dlu, f:p:g, 3dlu";
		FormLayout layout = new FormLayout( colSpec, rowSpec );
		PanelBuilder builder = new PanelBuilder( layout, parent );
		CellConstraints cc = new CellConstraints();
		
		builder.addSeparator( "Turnier", cc.xyw( 2, 2, 10 ) );
		
		// Bezeichnung
		int row = 4;
		builder.addLabel( "Bezeichnung", cc.xy( 2, row ) );
		builder.add( textDescription, cc.xyw( 4, row, 8 ) );
		
		// Buy-in
		row = 6;
		builder.addLabel( "Buy-in", cc.xy( 2, row ) );
		builder.add( spinnerBuyinPrize, cc.xy( 4, row ) );
		builder.add( labelBuyin2, cc.xy( 6, row ) );
		builder.add( spinnerBuyinFee, cc.xy( 8, row ) );
		
		// Optionen
		row = 8;
		builder.addLabel( "Optionen", cc.xy( 2, row ) );
		builder.add( checkRebuy, cc.xyw( 4, row, 8 ) );
		
		// Preisverteilung
		builder.addSeparator( "Preisverteilung", cc.xyw( 2, 10, 10 ) );
		builder.addLabel( "Preisgeld", cc.xy( 2, 12 ) );
		builder.add( comboPrizeScalingType, cc.xyw( 4, 12, 4 ) );
		builder.add( tablePrizeScalings, cc.xyw( 2, 14, 8 ) );
		builder.add( tablePrizePercentages, cc.xy( 11, 14 ) );
		
		/*
		 * Füllen der Eingabefelder
		 */
		fillControls();
		
		/*
		 * Dirty-Marker hinzufügen
		 */
		addDirtyMarker( textDescription );
		addDirtyMarker( spinnerBuyinPrize );
		addDirtyMarker( spinnerBuyinFee );
		addDirtyMarker( checkRebuy );
		addDirtyMarker( comboPrizeScalingType );
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

		/*
		 * Werte aus Feldern übernehmen
		 */
		Tournament tournament = input.getTournament();
		boolean first = tournament.getId() == null;
		
		if( textDescription != null ) {
			tournament.setDesciption( StringUtils.trim( textDescription.getText() ) );
		}
		
		if( spinnerBuyinPrize != null ) {
			tournament.setBuyinPrize( spinnerBuyinPrize.getSelection() * 1L );
		}
		
		if( spinnerBuyinFee != null ) {
			tournament.setBuyinFee( spinnerBuyinFee.getSelection() * 1L );
		}
		
		if( checkRebuy != null ) {
			tournament.setRebuy( checkRebuy.getSelection() );
		}
		
		if( comboPrizeScalingType != null ) {
			PrizeScalingTypeEnum prizeScalingType = PrizeScalingTypeEnum.getByDescription( comboPrizeScalingType.getText() );
			
			if( prizeScalingType != null ) {
				tournament.setPrizeScalingType( prizeScalingType.getKey() );
			} else {
				tournament.setPrizeScalingType( null );
			}
		}
		
		/*
		 * Validierung
		 */
		ValidationResult validationResult = TournamentModel.getInstance().validate( tournament );
		
		if( validationResult.hasErrors() ) {
			String title = "Validierungsfehler";
			String message = "Das Turnier kann nicht gespeichert werden, da folgende Fehler aufgetreten sind:";
			
			ValidationResultDialog.openError( getSite().getShell(), title, message, validationResult);
			
			return;
		}
		
		/*
		 * Speichern
		 */
		SaveResult saveResult = TournamentModel.getInstance().save( tournament );
		
		if( saveResult.isSaveSuccessful() == false ) {
			String title = "Fehler beim Speichern";

			if( saveResult.getThrowable() != null ) {
				ExceptionDialog.openExceptionDialog( getSite().getShell(), title, saveResult.getErrorMessage(), saveResult.getThrowable() );
			} else {
				MessageDialog.openError( getSite().getShell(), title, saveResult.getErrorMessage() );
			}
			
			return;
		} else if( first && StringUtils.equals( tournament.getDesciption(), "Bazinga!") ) {
			// Easteregg
			SimpleImageDialog.openDialog( getSite().getShell(), "Bazinga!", Images.getImage( Images.IMAGE_BAZINGA ) );
		}
		
		/*
		 * Übersicht der Turniere aktualisieren
		 */
		IWorkbenchPage page = getEditorSite().getPage();
		IViewPart viewPart = page.findView( TournamentOverviewView.ID );
		
		if( viewPart != null && viewPart instanceof TournamentOverviewView ) {
			TournamentOverviewView view = (TournamentOverviewView)viewPart;

			view.refresh();
		}
		
		super.doSave( monitor );
	}

	@Override
	public void setFocus() {
	}
	
	private Table createTablePrizeScalings( Composite parent ) {
		
		tableViewerPrizeScalings = new TableViewer( parent, SWT.FULL_SELECTION | SWT.BORDER );
		
		Table table = tableViewerPrizeScalings.getTable();
		table.setHeaderVisible( true );
		table.setLinesVisible( true );
		
		tableViewerPrizeScalings.setContentProvider( new PrizeScalingContentProvider() );
		tableViewerPrizeScalings.setLabelProvider( new PrizeScalingLabelProvider() );
		
		TextCellEditor textCellEditor = new TextCellEditor( table );
		tableViewerPrizeScalings.setCellEditors( new CellEditor[] {
				null, textCellEditor, textCellEditor, textCellEditor
		} );
		tableViewerPrizeScalings.setColumnProperties( new String[] {
				StringUtils.EMPTY, "participantsFrom", "participantsTo", "participantsInPrizes"
		} );
		tableViewerPrizeScalings.setCellModifier( new PrizeScalingCellModifier() );
		
		/*
		 * Spalten hinzufügen
		 */
		TableColumn[] columns = new TableColumn[4];
		
		for( int i = 0; i < columns.length; i++ ) {
			columns[i] = new TableColumn( table, SWT.RIGHT );
		}
		
		// Leere Spalte, weil da das Ausrichten nach rechts Probleme macht
		columns[COL_EMPTY].setMoveable( false );
		columns[COL_EMPTY].setResizable( false );
		columns[COL_EMPTY].setText( StringUtils.EMPTY );
		
		// Teilnehmer von
		columns[COL_1_PARTICIPANTS_FROM].setMoveable( false );
		columns[COL_1_PARTICIPANTS_FROM].setText( "Teilnehmer von" );
		
		// Teilnehmer bis
		columns[COL_1_PARTICIPANTS_TO].setMoveable( false );
		columns[COL_1_PARTICIPANTS_TO].setText( "Teilnehmer bis" );
		
		// bezahlte Plätze
		columns[COL_1_PARTICIPANTS_IN_PRIZES].setMoveable( false );
		columns[COL_1_PARTICIPANTS_IN_PRIZES].setText( "bezahlte Plätze" );
		
		TableLayout layout = new TableLayout();
		
		layout.addColumnData( new ColumnPixelData( 0 ) );
		layout.addColumnData( new ColumnPixelData( 100 ) );
		layout.addColumnData( new ColumnPixelData( 100 ) );
		layout.addColumnData( new ColumnPixelData( 100 ) );
		
		table.setLayout( layout );
		
		/*
		 * Bei Auswahl (Markierung):
		 * Tabelle für Preisanteil der einzelnen Platzierungen aktualisieren
		 */
		tableViewerPrizeScalings.addSelectionChangedListener( new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				// Tabelle für Preisanteil der einzelnen Platzierungen aktualisieren
				if( ( event.getSelection() != null ) && !event.getSelection().isEmpty() ) {
					
                    IStructuredSelection selection = ( IStructuredSelection )event.getSelection();
                    PrizeScaling prizeScaling = ( PrizeScaling )selection.getFirstElement();
                    
                    tableViewerPrizePercentages.setInput( prizeScaling.getPrizePercentages() );
                    tableViewerPrizePercentages.refresh();
                    
                    actionDelete.setEnabled( true );
                } else {
                	actionDelete.setEnabled( false );
                }
				
			}
		});
		
		/*
		 * Kontextmenü erstellen
		 */
		final Action actionAdd = new Action( "Hinzufügen" ) {
			
			@Override
			public void run() {

				Tournament tournament = input.getTournament();
				PrizeScaling prizeScaling = TournamentModel.getInstance().createNewPrizeScaling( tournament );
				
				tableViewerPrizeScalings.refresh();
				tableViewerPrizeScalings.setSelection( new StructuredSelection( prizeScaling ) );
				
				markDirty();
			}
		};
		actionAdd.setEnabled( true );
		
		actionDelete = new Action( "Entfernen" ) {
			
			@Override
			public void run() {
				
				if( tableViewerPrizeScalings == null || tableViewerPrizeScalings.getSelection().isEmpty() ) {
					return;
				}
				
				IStructuredSelection selection = (IStructuredSelection)tableViewerPrizeScalings.getSelection();
				PrizeScaling prizeScaling = (PrizeScaling)selection.getFirstElement();
				
				TournamentModel.getInstance().removePrizeScaling( prizeScaling );
				
				tableViewerPrizeScalings.refresh();
				tableViewerPrizePercentages.setInput( null );
				
				markDirty();
			}
		};
		actionDelete.setEnabled( false );
		
		final Action actionClone = new Action( "Von anderem Turnier übernehmen" ) {
			
			@Override
			public void run() {
				
				if( tableViewerPrizePercentages == null ) {
					return;
				}
				
				ChooseTournamentDialog dialog = new ChooseTournamentDialog( 
						getSite().getShell(), input.getTournament() );
				
				if( dialog.open() == Dialog.OK ) {
					
					Tournament tournamentSrc = dialog.getTournament();
					Tournament tournamentDest = input.getTournament();
					
					TournamentModel.getInstance().clonePrizeScalings( tournamentSrc, tournamentDest );
					
					tableViewerPrizeScalings.refresh();

					// erste Zeile selektieren, damit die zweite Tabelle angezeigt wird
					if( tournamentDest.getPrizeScalings() != null && tournamentDest.getPrizeScalings().isEmpty() == false ) {
						
						PrizeScaling prizeScaling = tournamentDest.getPrizeScalings().get( 0 );
						
						tableViewerPrizeScalings.setSelection( new StructuredSelection( prizeScaling ) );
					}
					
					markDirty();
				}
			}
		};
		actionClone.setEnabled( true );

		MenuManager menuManager = new MenuManager();
		IMenuListener menuListener = new IMenuListener() {
			
			public void menuAboutToShow( IMenuManager manager ) {
				manager.add( actionAdd );
				manager.add( actionDelete );
				manager.add( new Separator() );
				manager.add( actionClone );
			}
		};
		
		menuManager.addMenuListener( menuListener );
		menuManager.setRemoveAllWhenShown( true );
		
		Menu menu = menuManager.createContextMenu( table );
		table.setMenu( menu );
		
		return table;
	}
	
	private Table createTablePrizePercentages( Composite parent ) {
		
		tableViewerPrizePercentages = new TableViewer( parent, SWT.FULL_SELECTION | SWT.BORDER );
		
		Table table = tableViewerPrizePercentages.getTable();
		table.setHeaderVisible( true );
		table.setLinesVisible( true );
		
		tableViewerPrizePercentages.setContentProvider( new PrizePercentageContentProvider() );
		tableViewerPrizePercentages.setLabelProvider( new PrizePercentageLabelProvider() );
		
		TextCellEditor textCellEditor = new TextCellEditor( table );
		tableViewerPrizePercentages.setCellEditors( new CellEditor[] {
				null, textCellEditor, textCellEditor
		} );
		tableViewerPrizePercentages.setColumnProperties( new String[] {
				StringUtils.EMPTY, "place", "percentage"
		} );
		tableViewerPrizePercentages.setCellModifier( new PrizePercentageCellModifier() );
		
		/*
		 * Spalten hinzufügen
		 */
		TableColumn[] columns = new TableColumn[3];
		
		for( int i = 0; i < columns.length; i++ ) {
			columns[i] = new TableColumn( table, SWT.RIGHT );
		}
		
		// Leere Spalte, weil da das Ausrichten nach rechts Probleme macht
		columns[COL_EMPTY].setMoveable( false );
		columns[COL_EMPTY].setResizable( false );
		columns[COL_EMPTY].setText( StringUtils.EMPTY );
		
		// Platz
		columns[COL_2_PLACE].setMoveable( false );
		columns[COL_2_PLACE].setText( "Platz" );
		
		// Preisgeld
		columns[COL_2_PERCENTAGE].setMoveable( false );
		columns[COL_2_PERCENTAGE].setText( "Preisgeld" );
		
		TableLayout layout = new TableLayout();
		
		layout.addColumnData( new ColumnPixelData( 0 ) );
		layout.addColumnData( new ColumnPixelData( 100 ) );
		layout.addColumnData( new ColumnPixelData( 100 ) );
		
		table.setLayout( layout );
		
		/*
		 * Bei Auswahl (Markierung): Kontextmenü aktualisieren
		 */
		tableViewerPrizePercentages.addSelectionChangedListener( new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				if( ( event.getSelection() != null ) && !event.getSelection().isEmpty() ) {
                    actionInputAssistance.setEnabled( true );
                } else {
                	actionInputAssistance.setEnabled( false );
                }
				
			}
		});
		
		/*
		 * Kontextmenü erstellen
		 */
		actionInputAssistance = new Action( "Eingabeünterstützung" ) {
			
			@Override
			public void run() {

				if( tableViewerPrizePercentages == null || tableViewerPrizePercentages.getSelection().isEmpty() ) {
					return;
				}
				
				IStructuredSelection selection = (IStructuredSelection)tableViewerPrizePercentages.getSelection();
				PrizePercentage prizePercentage = (PrizePercentage)selection.getFirstElement();
				PrizeScalingTypeEnum prizeScalingType = PrizeScalingTypeEnum.getByDescription( comboPrizeScalingType.getText() );
				
				PrizePercentageInputAssistanceDialog dialog = new PrizePercentageInputAssistanceDialog( 
						getSite().getShell(),
						prizeScalingType,
						prizePercentage.getPrizeScaling().getPrizePercentages().size(),
						prizePercentage.getPlace() );
				
				if( dialog.open() == Dialog.OK ) {
					int from = dialog.getSelectionFrom();
					int to = dialog.getSelectionTo();
					Integer value = dialog.getValue();
					
					if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
						TournamentModel.getInstance().updatePercentages( prizePercentage.getPrizeScaling(), from, to, value );
					} else if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT ) {
						TournamentModel.getInstance().updateAmounts( prizePercentage.getPrizeScaling(), from, to, value );
					}
					
					tableViewerPrizePercentages.refresh();
					
					markDirty();
				}
				
			}
		};
		actionInputAssistance.setEnabled( false );

		MenuManager menuManager = new MenuManager();
		IMenuListener menuListener = new IMenuListener() {
			
			public void menuAboutToShow( IMenuManager manager ) {
				manager.add( actionInputAssistance );
			}
		};
		
		menuManager.addMenuListener( menuListener );
		menuManager.setRemoveAllWhenShown( true );
		
		Menu menu = menuManager.createContextMenu( table );
		table.setMenu( menu );
		
		return table;
	}
	
	private void fillControls() {
		
		Tournament tournament = input.getTournament();
		
		if( tournament == null ) {
			return;
		}
		
		// Bezeichnung
		if( StringUtils.isNotBlank( tournament.getDesciption() ) ) {
			textDescription.setText( tournament.getDesciption() );
		}
		
		// Buy-in (Preisgeldanteil)
		if( tournament.getBuyinPrize() != null ) {
			spinnerBuyinPrize.setSelection( tournament.getBuyinPrize().intValue() );
		}
		
		// Buy-in (Gebührenanteil)
		if( tournament.getBuyinFee() != null ) {
			spinnerBuyinFee.setSelection( tournament.getBuyinFee().intValue() );
		}
		
		// Rebuy
		if( tournament.getRebuy() != null ) {
			checkRebuy.setSelection( tournament.getRebuy() );
		}
		
		// Preisverteilung
		if( tournament.getPrizeScalingType() != null ) {
			comboPrizeScalingType.setText( PrizeScalingTypeEnum.getByKey( tournament.getPrizeScalingType() ).getDescription() );
		} else {
			comboPrizeScalingType.setText( PrizeScalingTypeEnum.PERCENTAGE.getDescription() );
		}
		
		tableViewerPrizeScalings.setInput( tournament.getPrizeScalings() );
		tableViewerPrizeScalings.refresh();
		
		// erste Zeile selektieren, damit die zweite Tabelle angezeigt wird
		if( tournament.getPrizeScalings() != null && tournament.getPrizeScalings().isEmpty() == false ) {
			
			PrizeScaling prizeScaling = tournament.getPrizeScalings().get( 0 );
			
			tableViewerPrizePercentages.setInput( prizeScaling.getPrizePercentages() );
			tableViewerPrizePercentages.refresh();
		}
	}
	
	private class PrizeScalingContentProvider implements IStructuredContentProvider {

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
				return ((List<PrizeScaling>) inputElement).toArray( new PrizeScaling[0] );
			}

			return null;
		}
		
	}
	
	private class PrizeScalingLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			
			if( element instanceof PrizeScaling ) {
				
				PrizeScaling prizeScaling = (PrizeScaling)element;
				
				if( columnIndex == COL_1_PARTICIPANTS_FROM ) {
					
					if( prizeScaling.getParticipantsFrom() != null ) {
						return prizeScaling.getParticipantsFrom().toString();
					}
				}else if( columnIndex == COL_1_PARTICIPANTS_TO ) {
					
					if( prizeScaling.getParticipantsTo() != null ) {
						return prizeScaling.getParticipantsTo().toString();
					}
				}else if( columnIndex == COL_1_PARTICIPANTS_IN_PRIZES ) {
					
					if( prizeScaling.getParticipantsInPrizes() != null ) {
						return prizeScaling.getParticipantsInPrizes().toString();
					}
				}
			}
			
			return "???";
		}
		
	}
	
	private class PrizeScalingCellModifier implements ICellModifier {

		@Override
		public boolean canModify(Object element, String property) {
			
			if( element instanceof PrizeScaling ) {
				return true;
			}
			
			return false;
		}

		@Override
		public Object getValue(Object element, String property) {
			
			if( element instanceof PrizeScaling ) {
				
				PrizeScaling prizeScaling = (PrizeScaling)element;
				
				if( StringUtils.equals( property, "participantsFrom" ) ) {
					
					if( prizeScaling.getParticipantsFrom() != null ) {
						return prizeScaling.getParticipantsFrom().toString();
					}
					
				} else if( StringUtils.equals( property, "participantsTo" ) ) {
					
					if( prizeScaling.getParticipantsTo() != null ) {
						return prizeScaling.getParticipantsTo().toString();
					}
					
				} else if( StringUtils.equals( property, "participantsInPrizes" ) ) {
					
					if( prizeScaling.getParticipantsInPrizes() != null ) {
						return prizeScaling.getParticipantsInPrizes().toString();
					}
				}
				
				return StringUtils.EMPTY;
			}
			
			return null;
		}

		@Override
		public void modify(Object element, String property, Object value) {
			
			if( element instanceof Item ) {
                element = ( ( Item )element ).getData();
            }
			
			if( element instanceof PrizeScaling ) {
				
				PrizeScaling prizeScaling = (PrizeScaling)element;
				Integer valueInt = null;
				
				if( value instanceof String ) {
					
					String valueStr = (String)value;
					
					if( StringUtils.isNotBlank( valueStr ) && StringUtils.isNumeric( valueStr ) ) {
						valueInt = Integer.parseInt( valueStr );
					}
				}
				
				if( StringUtils.equals( property, "participantsFrom" ) && valueInt != null ) {
					
					if( valueInt.equals( prizeScaling.getParticipantsFrom() ) ) {
						return;
					}
					
					prizeScaling.setParticipantsFrom( valueInt );
					markDirty();
					
				} else if( StringUtils.equals( property, "participantsTo" ) && valueInt != null ) {
					
					if( valueInt.equals( prizeScaling.getParticipantsTo() ) ) {
						return;
					}
					
					prizeScaling.setParticipantsTo( valueInt );
					markDirty();
					
				} else if( StringUtils.equals( property, "participantsInPrizes" ) && valueInt != null ) {
					
					if( valueInt.equals( prizeScaling.getParticipantsInPrizes() ) ) {
						return;
					}
					
					prizeScaling.setParticipantsInPrizes( valueInt );
					
					// Elemente für zweite Tabelle erzeugen/entfernen
					TournamentModel.getInstance().refreshPrizePercentageList( prizeScaling );
					tableViewerPrizePercentages.refresh();
					
					markDirty();
				}
				
				tableViewerPrizeScalings.refresh( element, true );
			}
		}
		
	}
	
	private class PrizePercentageContentProvider implements IStructuredContentProvider {

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
				return ((List<PrizePercentage>) inputElement).toArray( new PrizePercentage[0] );
			}

			return null;
		}
		
	}
	
	private class PrizePercentageLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			
			if( element instanceof PrizePercentage ) {
				
				PrizePercentage prizePercentage = (PrizePercentage)element;
				
				if( columnIndex == COL_2_PLACE ) {
					
					if( prizePercentage.getPlace() != null ) {
						return prizePercentage.getPlace().toString();
					}
				}else if( columnIndex == COL_2_PERCENTAGE ) {
					
					PrizeScalingTypeEnum prizeScalingType = PrizeScalingTypeEnum.getByDescription( comboPrizeScalingType.getText() );
					
					if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE
							&& prizePercentage.getPercentage() != null ) {
						return MoneyUtils.formatPercentage( prizePercentage.getPercentage(), true );
					} else if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT
							&& prizePercentage.getAmount() != null ) {
						return MoneyUtils.formatAmount( prizePercentage.getAmount(), true );
					}
				}
			}
			
			return "???";
		}
		
	}
	
	private class PrizePercentageCellModifier implements ICellModifier {

		@Override
		public boolean canModify(Object element, String property) {
			
			if( element instanceof PrizePercentage ) {
				
				if( StringUtils.equals( property, "place" ) ) {
					return false;
				}
				
				return true;
			}
			
			return false;
		}

		@Override
		public Object getValue(Object element, String property) {
			
			if( element instanceof PrizePercentage ) {
				
				PrizePercentage prizePercentage = (PrizePercentage)element;
				
				if( StringUtils.equals( property, "place" ) ) {
					
					if( prizePercentage.getPlace() != null ) {
						return prizePercentage.getPlace().toString();
					}
					
				} else if( StringUtils.equals( property, "percentage" ) ) {
					
					PrizeScalingTypeEnum prizeScalingType = PrizeScalingTypeEnum.getByDescription( comboPrizeScalingType.getText() );
					
					if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE
							&& prizePercentage.getPercentage() != null ) {
						return MoneyUtils.formatPercentage( prizePercentage.getPercentage(), false );
					} else if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT
							&& prizePercentage.getAmount() != null ) {
						return MoneyUtils.formatAmount( prizePercentage.getAmount(), false );
					}
					
				}
				
				return StringUtils.EMPTY;
			}
			
			return null;
		}

		@Override
		public void modify(Object element, String property, Object value) {
			
			if( element instanceof Item ) {
                element = ( ( Item )element ).getData();
            }
			
			if( element instanceof PrizePercentage ) {
				
				PrizeScalingTypeEnum prizeScalingType = PrizeScalingTypeEnum.getByDescription( comboPrizeScalingType.getText() );
				PrizePercentage prizePercentage = (PrizePercentage)element;
				Integer valueInt = null;
				
				if( value instanceof String ) {
					
					String valueStr = (String)value;
					
					if( StringUtils.equals( property, "percentage" ) && prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
						valueInt = MoneyUtils.parsePercentage( valueStr );
					} else if( StringUtils.equals( property, "percentage" ) && prizeScalingType == PrizeScalingTypeEnum.AMOUNT ) {
						valueInt = MoneyUtils.parseAmount( valueStr );
					} else if( StringUtils.isNotBlank( valueStr ) && StringUtils.isNumeric( valueStr ) ) {
						valueInt = Integer.parseInt( valueStr );
					}
					
					
				}
				
				if( StringUtils.equals( property, "place" ) && valueInt != null ) {
					
					if( valueInt.equals( prizePercentage.getPlace() ) ) {
						return;
					}
					
					prizePercentage.setPlace( valueInt );
					markDirty();
					
				} else if( StringUtils.equals( property, "percentage" ) && valueInt != null ) {
					
					if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
						
						if( valueInt.equals( prizePercentage.getPercentage() ) ) {
							return;
						}
						
						prizePercentage.setPercentage( valueInt );
						markDirty();
						
					} else if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT ) {
						
						Long valueLong = valueInt.longValue();
						
						if( valueLong.equals( prizePercentage.getAmount() ) ) {
							return;
						}
						
						prizePercentage.setAmount( valueLong );
						markDirty();
					}
					
				}
				
				tableViewerPrizePercentages.refresh( element, true );
			}
		}
		
	}

}
