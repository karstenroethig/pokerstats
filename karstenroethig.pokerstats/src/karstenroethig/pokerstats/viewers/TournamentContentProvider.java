package karstenroethig.pokerstats.viewers;

import java.util.List;

import karstenroethig.pokerstats.model.Tournament;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TournamentContentProvider implements IStructuredContentProvider {

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
