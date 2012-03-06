package karstenroethig.pokerstats.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class StatsEditorInput implements IEditorInput {

	public StatsEditorInput() {
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return "Statistiken";
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Statistiken";
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if( obj == null ) {
			return false;
		}
		
		if( obj == this || obj instanceof StatsEditorInput ) {
			return true;
		}
		
		return super.equals( obj );
	}

}
