package karstenroethig.pokerstats;

import karstenroethig.pokerstats.view.ParticipationOverviewView;
import karstenroethig.pokerstats.view.TournamentOverviewView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
	
	public static final String ID = "karstenroethig.pokerstats.perspective";
	
	public static final String ID_FOLDER_OVERVIEW = ID + ".folder.overview";

	public void createInitialLayout(IPageLayout layout) {

		IFolderLayout folderOverview = layout.createFolder( ID_FOLDER_OVERVIEW,
				IPageLayout.TOP, 0.30f, IPageLayout.ID_EDITOR_AREA );
		
		folderOverview.addView( TournamentOverviewView.ID );
		folderOverview.addView( ParticipationOverviewView.ID );
		
		layout.getViewLayout( TournamentOverviewView.ID ).setCloseable( false );
		layout.getViewLayout( ParticipationOverviewView.ID ).setCloseable( false );
	}
}
