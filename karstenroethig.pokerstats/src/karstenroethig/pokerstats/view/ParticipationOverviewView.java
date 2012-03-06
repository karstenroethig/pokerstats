package karstenroethig.pokerstats.view;

import java.util.List;

import karstenroethig.pokerstats.editor.ParticipationEditor;
import karstenroethig.pokerstats.editor.ParticipationEditorInput;
import karstenroethig.pokerstats.model.Participation;
import karstenroethig.pokerstats.model.ParticipationModel;
import karstenroethig.pokerstats.util.CompareToUtils;
import karstenroethig.pokerstats.util.DateUtils;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnPixelData;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class ParticipationOverviewView extends ViewPart {

	public static final String ID = "karstenroethig.pokerstats.view.ParticipationOverviewView";
	
	private static final int SORT_DIRECTION_DEFAULT = -1;
	
	private static final int COL_DATE = 0;
	
	private static final int COL_PLACE = 1;
	
	private static final int COL_PARTICIPANTS = 2;
	
	private static final int COL_TOURNAMENT = 3;
	
	private TableColumn[] columns = new TableColumn[4];
	
	private int[] sortDirections = new int[columns.length];
	
	private TableViewer participationsTableViewer;
	
	private Action actionNew;
	
	private Action actionEdit;

	@Override
	public void createPartControl(Composite parent) {
		
		participationsTableViewer = new TableViewer( parent, SWT.FULL_SELECTION | SWT.BORDER );
		
		Table table = participationsTableViewer.getTable();
		table.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		table.setHeaderVisible( true );
		table.setLinesVisible( true );
		
		createColumns( table );
		
		participationsTableViewer.setContentProvider( new ParticipationOverviewContentProvider() );
		participationsTableViewer.setLabelProvider( new ParticipationOverviewLabelProvider() );
		participationsTableViewer.setInput( ParticipationModel.getInstance().getParticipations() );
		
		// Bei Auswahl (Markierung): Action im Kontextmenü de-/aktivieren
		participationsTableViewer.addSelectionChangedListener( new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				if( event.getSelection() != null && !event.getSelection().isEmpty() ) {
					actionEdit.setEnabled( true );
				} else {
					actionEdit.setEnabled( false );
				}
				
			}
		});
		
		// Doppelklick auf Eintrag öffnet den Editor
		participationsTableViewer.addDoubleClickListener( new IDoubleClickListener() {
			
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
		
		columns[COL_DATE].setMoveable( false );
		columns[COL_DATE].setText( "Datum" );
		SelectionListener listener = new SortSelectionAdapter( COL_DATE );
		columns[COL_DATE].addSelectionListener( listener );
		listener.widgetSelected( null );
		
		columns[COL_PLACE].setMoveable( false );
		columns[COL_PLACE].setText( "Platz" );
		columns[COL_PLACE].addSelectionListener( new SortSelectionAdapter( COL_PLACE ) );
		
		columns[COL_PARTICIPANTS].setMoveable( false );
		columns[COL_PARTICIPANTS].setText( "Teilnehmer" );
		columns[COL_PARTICIPANTS].addSelectionListener( new SortSelectionAdapter( COL_PARTICIPANTS ) );
		
		columns[COL_TOURNAMENT].setMoveable( false );
		columns[COL_TOURNAMENT].setText( "Turnier" );
		columns[COL_TOURNAMENT].addSelectionListener( new SortSelectionAdapter( COL_TOURNAMENT ) );
		
		TableLayout layout = new TableLayout();
		
		layout.addColumnData( new ColumnPixelData( 80 ) );
		layout.addColumnData( new ColumnPixelData( 80 ) );
		layout.addColumnData( new ColumnPixelData( 80 ) );
		layout.addColumnData( new ColumnPixelData( 500 ) );
		
		table.setLayout( layout );
	}
	
	private void createActions() {
		
		actionNew = new Action() {
			
			@Override
			public void run() {

				Participation participation = ParticipationModel.getInstance().createNewParticipation();
				
				IWorkbenchPage page = getViewSite().getPage();
				ParticipationEditorInput input = new ParticipationEditorInput( participation );
				
				try{
					page.openEditor( input, ParticipationEditor.ID );
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

				if( participationsTableViewer.getSelection() == null
						|| participationsTableViewer.getSelection().isEmpty() ) {
					return;
				}
				
				IStructuredSelection selection = (IStructuredSelection)participationsTableViewer.getSelection();
				Participation participation = (Participation)selection.getFirstElement();
				
				IWorkbenchPage page = getViewSite().getPage();
				ParticipationEditorInput input = new ParticipationEditorInput( participation );
				
				try{
					page.openEditor( input, ParticipationEditor.ID );
				}catch( PartInitException ex ) {
					throw new RuntimeException( ex );
				}
			}
		};
		actionEdit.setText( "Bearbeiten" );
		actionEdit.setEnabled( false );
	}
	
	private ViewerSorter getSorter( final int columnIndex, final int sortDirection ) {

		return new ViewerSorter() {
			
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				
				if(e1 instanceof Participation == false || e2 instanceof Participation ==false ) {
					return 0;
				}
				
				Participation participation1 = (Participation)e1;
				Participation participation2 = (Participation)e2;
				
				int ret = 0;
				
				if( columnIndex == COL_DATE ) {
					ret = CompareToUtils.compare( participation1.getDate(),
							participation2.getDate() );
				} else if( columnIndex == COL_TOURNAMENT ) {
					ret = CompareToUtils.compare( participation1.getTournament().getDesciption(),
							participation2.getTournament().getDesciption() );
				} else if( columnIndex == COL_PLACE ) {
					ret = CompareToUtils.compare( participation1.getPlace(),
							participation2.getPlace() );
				} else if( columnIndex == COL_PARTICIPANTS ) {
					ret = CompareToUtils.compare( participation1.getParticipants(),
							participation2.getParticipants() );
				}
				
				return ret * sortDirection;
			}
		};
	}
	
	private class ParticipationOverviewContentProvider implements IStructuredContentProvider {

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
				return ((List<Participation>) inputElement).toArray( new Participation[0] );
			}

			return null;
		}
		
	}
	
	public void refresh() {
		
		if( participationsTableViewer != null ) {
			participationsTableViewer.refresh();
		}
	}
	
	private class ParticipationOverviewLabelProvider implements ITableLabelProvider {

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

			if( element instanceof Participation ) {
				Participation participation = (Participation)element;
				
				if( columnIndex == COL_DATE ) {
					return DateUtils.fomatDate( participation.getDate() );
				}else if( columnIndex == COL_TOURNAMENT ) {
					return participation.getTournament().getDesciption();
				}else if( columnIndex == COL_PLACE ) {
					return participation.getPlace().toString();
				}else if( columnIndex == COL_PARTICIPANTS ) {
					return participation.getParticipants().toString();
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
		public void widgetSelected( SelectionEvent e ) {

			for( int i = 0; i < sortDirections.length; i++ ) {
				
				if( i == columnIndex ) {
					sortDirections[i] = sortDirections[i] * -1;
				} else {
					sortDirections[i] = SORT_DIRECTION_DEFAULT;
				}
				
				ViewerSorter sorter = getSorter( columnIndex, sortDirections[columnIndex] );
				participationsTableViewer.setSorter( sorter );
				participationsTableViewer.getTable().setSortColumn( columns[columnIndex] );
				
				if( sortDirections[columnIndex] < 0 ) {
					participationsTableViewer.getTable().setSortDirection( SWT.DOWN );
				} else {
					participationsTableViewer.getTable().setSortDirection( SWT.UP );
				}
			}
		}
	}
}
