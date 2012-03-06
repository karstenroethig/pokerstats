package karstenroethig.pokerstats.viewers;

import karstenroethig.pokerstats.model.Tournament;

import org.eclipse.jface.viewers.LabelProvider;

public class TournamentLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		
		if( element instanceof Tournament ) {
			
			Tournament tournament = (Tournament)element;
			
			return tournament.getDesciption();
		}
		
		return super.getText( element );
	}

}
