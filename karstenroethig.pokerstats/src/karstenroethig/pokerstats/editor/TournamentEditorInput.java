package karstenroethig.pokerstats.editor;

import karstenroethig.pokerstats.model.Tournament;
import karstenroethig.pokerstats.model.TournamentModel;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class TournamentEditorInput implements IEditorInput {

	private Tournament tournament;
	
	public TournamentEditorInput( Tournament tournament ) {
		this.tournament = tournament;
	}
	
	public Tournament getTournament() {
		return tournament;
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
		
		if( tournament != null
				&& StringUtils.isNotBlank( tournament.getDesciption() ) ) {
			return tournament.getDesciption();
		}
		
		return "Unbenannt";
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Turnier: " + getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if( obj == null ) {
			return false;
		}
		
		if( obj == this ) {
			return true;
		}
		
		if( tournament != null && obj instanceof TournamentEditorInput ) {
			TournamentEditorInput input = ( TournamentEditorInput )obj;
			Tournament tournament2 = input.getTournament();
			
			if( tournament == null || tournament2 == null ) {
				return false;
			}
			
			if( tournament.getId() != null && tournament2.getId() != null ) {
				return tournament.getId().equals( tournament2.getId() );
			}
			
			return false;
		}
		
		return super.equals( obj );
	}
	
	public void reload() {
		
		Long tournamentId = tournament.getId();

        if( tournamentId != null ) {
        	tournament = TournamentModel.getInstance().loadTournament( tournamentId );
        }
	}

}
