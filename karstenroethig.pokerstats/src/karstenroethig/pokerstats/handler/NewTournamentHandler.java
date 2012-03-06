package karstenroethig.pokerstats.handler;

import karstenroethig.pokerstats.editor.TournamentEditor;
import karstenroethig.pokerstats.editor.TournamentEditorInput;
import karstenroethig.pokerstats.model.Tournament;
import karstenroethig.pokerstats.model.TournamentModel;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class NewTournamentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Tournament tournament = TournamentModel.getInstance().createNewTournament();
		
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();
		TournamentEditorInput input = new TournamentEditorInput( tournament );
		
		try{
			page.openEditor( input, TournamentEditor.ID );
		}catch( PartInitException ex ) {
			throw new RuntimeException( ex );
		}
		
		return null;
	}

}
