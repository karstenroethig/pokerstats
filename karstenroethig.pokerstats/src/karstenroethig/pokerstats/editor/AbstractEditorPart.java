package karstenroethig.pokerstats.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

public abstract class AbstractEditorPart extends EditorPart {

	private boolean dirty = false;
	
	private ModifyListener listener = new ModifyListener() {
        public void modifyText( ModifyEvent event ) {
            markDirty();
        }
    };
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite( site );
		setInput( input );
		setPartName( input.getName() );
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	@Override
	public void doSave( IProgressMonitor monitor ) {
		dirty = false;
		firePropertyChange( IEditorPart.PROP_DIRTY );
	}
	
	@Override
	public void doSaveAs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public void markDirty() {
		dirty = true;
		firePropertyChange( IEditorPart.PROP_DIRTY );
	}
	
	protected void addDirtyMarker( Text text ) {
		
		if( text != null ) {
			text.addModifyListener( listener );
		}
	}
	
	protected void addDirtyMarker( Combo combo ) {
		
		if( combo != null ) {
			combo.addModifyListener( listener );
		}
	}
	
	protected void addDirtyMarker( Button button ) {
		
		if( button != null ) {
			button.addSelectionListener( new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					markDirty();
				}
			});
		}
	}
	
	protected void addDirtyMarker( Spinner spinner ) {
		
		if( spinner != null ) {
			spinner.addModifyListener( listener );
		}
	}
	
	protected void addDirtyMarker( DateTime dateTime ) {
		
		if( dateTime != null ) {
			dateTime.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					markDirty();
				}
			});
		}
	}
	
	protected void addDirtyMarker( ComboViewer comboViewer ) {
		
		if( comboViewer != null ) {
			comboViewer.addSelectionChangedListener( new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					markDirty();
				}
			});
		}
	}

}
