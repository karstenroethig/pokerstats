package karstenroethig.pokerstats.editor;

import karstenroethig.pokerstats.model.Participation;
import karstenroethig.pokerstats.model.ParticipationModel;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class ParticipationEditorInput implements IEditorInput {

	private Participation participation;
	
	public ParticipationEditorInput( Participation participation ) {
		this.participation = participation;
	}
	
	public Participation getParticipation() {
		return participation;
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
		
		if( participation != null
				&& participation.getTournament() != null
				&& StringUtils.isNotBlank( participation.getTournament().getDesciption() ) ) {
			return "Teilnahme: " + participation.getTournament().getDesciption();
		}
		
		return "Teilnahme: Unbekannt";
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if( obj == null ) {
			return false;
		}
		
		if( obj == this ) {
			return true;
		}
		
		if( participation != null && obj instanceof ParticipationEditorInput ) {
			ParticipationEditorInput input = ( ParticipationEditorInput )obj;
			Participation participation2 = input.getParticipation();
			
			if( participation == null || participation2 == null ) {
				return false;
			}
			
			if( participation.getId() != null && participation2.getId() != null ) {
				return participation.getId().equals( participation2.getId() );
			}
			
			return false;
		}
		
		return super.equals( obj );
	}
	
	public void reload() {
		
		Long participationId = participation.getId();

        if( participationId != null ) {
        	participation = ParticipationModel.getInstance().loadParticipation( participationId );
        }
	}

}
