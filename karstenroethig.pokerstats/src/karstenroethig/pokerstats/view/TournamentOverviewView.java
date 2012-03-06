package karstenroethig.pokerstats.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import karstenroethig.pokerstats.editor.ParticipationEditor;
import karstenroethig.pokerstats.editor.ParticipationEditorInput;
import karstenroethig.pokerstats.editor.TournamentEditor;
import karstenroethig.pokerstats.editor.TournamentEditorInput;
import karstenroethig.pokerstats.model.Participation;
import karstenroethig.pokerstats.model.ParticipationModel;
import karstenroethig.pokerstats.model.Tournament;
import karstenroethig.pokerstats.model.TournamentModel;
import karstenroethig.pokerstats.util.CompareToUtils;
import karstenroethig.pokerstats.util.MoneyUtils;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class TournamentOverviewView extends ViewPart {

	public static final String ID = "karstenroethig.pokerstats.view.TournamentOverviewView";
	
	private static final int SORT_DIRECTION_DEFAULT = -1;
	
	private static final int COL_DESCRIPTION = 0;
	
	private static final int COL_BUYIN = 1;
	
	private static Set<ComboViewer> tournamentComboViewers = new HashSet<ComboViewer>();
	
	private TableColumn[] columns = new TableColumn[2];
	
	private int[] sortDirections = new int[columns.length];
	
	private TableViewer tournamentsTableViewer;
	
	private Action actionNew;
	
	private Action actionEdit;
	
	private Action actionNewParticipation;

	@Override
	public void createPartControl(Composite parent) {
		
		tournamentsTableViewer = new TableViewer( parent, SWT.FULL_SELECTION | SWT.BORDER );
		
		Table table = tournamentsTableViewer.getTable();
		table.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		table.setHeaderVisible( true );
		table.setLinesVisible( true );
		
		createColumns( table );
		
		tournamentsTableViewer.setContentProvider( new TournamentOverviewContentProvider() );
		tournamentsTableViewer.setLabelProvider( new TournamentOverviewLabelProvider() );
		tournamentsTableViewer.setInput( TournamentModel.getInstance().getTournaments() );
		
		// Bei Auswahl (Markierung): Action im Kontextmenü de-/aktivieren
		tournamentsTableViewer.addSelectionChangedListener( new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				if( event.getSelection() != null && !event.getSelection().isEmpty() ) {
					actionEdit.setEnabled( true );
					actionNewParticipation.setEnabled( true );
				} else {
					actionEdit.setEnabled( false );
					actionNewParticipation.setEnabled( false );
				}
				
			}
		});
		
		// Doppelklick auf Eintrag öffnet den Editor
		tournamentsTableViewer.addDoubleClickListener( new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				actionEdit.run();
			}
		});
		
		// Kontextmenü erstellen
		createActions();
		
		MenuManager menuManager = new MenuManager();
		IMenuListener menuListener = new IMenuListener() {
			
			public void menuAboutToShow( IMenuManager manager ) {
				manager.add( actionNew );
				manager.add( actionEdit );
				manager.add( new Separator() );
				manager.add( actionNewParticipation );
			}
		};
		
		menuManager.addMenuListener( menuListener );
		menuManager.setRemoveAllWhenShown( true );
		
		Menu menu = menuManager.createContextMenu( table );
		table.setMenu( menu );
	}

	@Override
	public void setFocus() {
	}
	
	private void createColumns( Table table ) {
		
		for( int i = 0; i < columns.length; i++ ) {
			columns[i] = new TableColumn( table, SWT.NONE );
			sortDirections[i] = SORT_DIRECTION_DEFAULT;
		}
		
		columns[COL_DESCRIPTION].setMoveable( false );
		columns[COL_DESCRIPTION].setText( "Bezeichnung" );
		columns[COL_DESCRIPTION].addSelectionListener( new SortSelectionAdapter( COL_DESCRIPTION ) );
		
		columns[COL_BUYIN].setMoveable( false );
		columns[COL_BUYIN].setText( "Buy-in" );
		columns[COL_BUYIN].addSelectionListener( new SortSelectionAdapter( COL_BUYIN ) );
		
		TableLayout layout = new TableLayout();
		
		layout.addColumnData( new ColumnPixelData( 500 ) );
		layout.addColumnData( new ColumnPixelData( 100 ) );
		
		table.setLayout( layout );
		
	}
	
	private void createActions() {
		
		actionNew = new Action() {
			
			@Override
			public void run() {

				Tournament tournament = TournamentModel.getInstance().createNewTournament();
				
				IWorkbenchPage page = getViewSite().getPage();
				TournamentEditorInput input = new TournamentEditorInput( tournament );
				
				try{
					page.openEditor( input, TournamentEditor.ID );
				}catch( PartInitException ex ) {
					throw new RuntimeException( ex );
				}
			}
		};
		actionNew.setText( "Neu" );
		actionNew.setEnabled( true );
		
		actionEdit = new Action() {
			
			@Override
			public void run() {

				if( tournamentsTableViewer.getSelection() == null
						|| tournamentsTableViewer.getSelection().isEmpty() ) {
					return;
				}
				
				IStructuredSelection selection = (IStructuredSelection)tournamentsTableViewer.getSelection();
				Tournament tournament = (Tournament)selection.getFirstElement();
				
				IWorkbenchPage page = getViewSite().getPage();
				TournamentEditorInput input = new TournamentEditorInput( tournament );
				
				try{
					page.openEditor( input, TournamentEditor.ID );
				}catch( PartInitException ex ) {
					throw new RuntimeException( ex );
				}
			}
		};
		actionEdit.setText( "Bearbeiten" );
		actionEdit.setEnabled( false );
		
		actionNewParticipation = new Action() {
			
			@Override
			public void run() {

				if( tournamentsTableViewer.getSelection() == null
						|| tournamentsTableViewer.getSelection().isEmpty() ) {
					return;
				}
				
				IStructuredSelection selection = (IStructuredSelection)tournamentsTableViewer.getSelection();
				Tournament tournament = (Tournament)selection.getFirstElement();
				Participation participation = ParticipationModel.getInstance().createNewParticipation( tournament );
				
				IWorkbenchPage page = getViewSite().getPage();
				ParticipationEditorInput input = new ParticipationEditorInput( participation );
				
				try{
					page.openEditor( input, ParticipationEditor.ID );
				}catch( PartInitException ex ) {
					throw new RuntimeException( ex );
				}
			}
		};
		actionNewParticipation.setText( "Neue Teilnahme" );
		actionNewParticipation.setEnabled( false );
	}
	
	private ViewerSorter getSorter( final int columnIndex, final int sortDirection ) {

		return new ViewerSorter() {
			
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				
				if(e1 instanceof Tournament == false || e2 instanceof Tournament ==false ) {
					return 0;
				}
				
				Tournament tournament1 = (Tournament)e1;
				Tournament tournament2 = (Tournament)e2;
				
				int ret = 0;
				
				if( columnIndex == COL_DESCRIPTION ) {
					ret = CompareToUtils.compare( tournament1.getDesciption(),
							tournament2.getDesciption() );
				} else if( columnIndex == COL_BUYIN ) {
					Long buyin1 = tournament1.getBuyinPrize() + tournament1.getBuyinFee();
					Long buyin2 = tournament2.getBuyinPrize() + tournament2.getBuyinFee();
					
					ret = CompareToUtils.compare( buyin1, buyin2 );
				}
				
				return ret * sortDirection;
			}
		};
	}
	
	public void refresh() {
		
		if( tournamentsTableViewer != null ) {
			tournamentsTableViewer.refresh();
		}
		
		refreshTournamentComboViewers();
	}
	
	public static void registerTournamentComboViewer( ComboViewer comboViewer ) {
		checkTournamentComboViewers();
		tournamentComboViewers.add( comboViewer );
	}
	
	private static void checkTournamentComboViewers() {
		
		Set<ComboViewer> removeComboViewers = new HashSet<ComboViewer>();
		
		for( ComboViewer comboViewer : tournamentComboViewers ) {
			
			if( comboViewer.getControl().isDisposed() ) {
				removeComboViewers.add( comboViewer );
			}
		}
		
		tournamentComboViewers.removeAll( removeComboViewers );
		
	}
	
	private static void refreshTournamentComboViewers() {
		checkTournamentComboViewers();
		
		for( ComboViewer comboViewer : tournamentComboViewers ) {
			comboViewer.refresh();
		}
	}
	
	private class TournamentOverviewContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			
			if( inputElement instanceof List ) {
				return ((List<Tournament>) inputElement).toArray( new Tournament[0] );
			}

			return null;
		}
		
	}
	
	private class TournamentOverviewLabelProvider implements ITableLabelProvider {

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

			if( element instanceof Tournament ) {
				Tournament tournament = (Tournament)element;
				
				if( columnIndex == COL_DESCRIPTION ) {
					return tournament.getDesciption();
				}else if( columnIndex == COL_BUYIN ) {
					Long amount = tournament.getBuyinPrize() + tournament.getBuyinFee();
					
					return MoneyUtils.formatAmount( amount, true );
				}
			}
			
			return null;
		}
		
	}
	
	private class SortSelectionAdapter extends SelectionAdapter {
		
		private int columnIndex;
		
		public SortSelectionAdapter( int columnIndex ) {
			this.columnIndex = columnIndex;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {

			for( int i = 0; i < sortDirections.length; i++ ) {
				
				if( i == columnIndex ) {
					sortDirections[i] = sortDirections[i] * -1;
				} else {
					sortDirections[i] = SORT_DIRECTION_DEFAULT;
				}
				
				ViewerSorter sorter = getSorter( columnIndex, sortDirections[columnIndex] );
				tournamentsTableViewer.setSorter( sorter );
				tournamentsTableViewer.getTable().setSortColumn( columns[columnIndex] );
				
				if( sortDirections[columnIndex] < 0 ) {
					tournamentsTableViewer.getTable().setSortDirection( SWT.DOWN );
				} else {
					tournamentsTableViewer.getTable().setSortDirection( SWT.UP );
				}
			}
		}
	}
}
