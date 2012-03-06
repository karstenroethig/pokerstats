package karstenroethig.pokerstats.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

public class StatsEditor extends FormEditor {

	public static final String ID = "karstenroethig.pokerstats.editor.StatsEditor";
	
	@Override
	protected void addPages() {

		try{
			addPage( new FormPage( this, ID + ".page1", "Gesamt" ) );
			addPage( new StatsFormPage( this, ID + ".page2", "pro Turnier" ) );
		}catch( PartInitException ex ) {
			throw new RuntimeException( ex );
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doSaveAs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
