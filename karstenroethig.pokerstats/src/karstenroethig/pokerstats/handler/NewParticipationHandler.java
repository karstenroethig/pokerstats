package karstenroethig.pokerstats.handler;

import karstenroethig.pokerstats.editor.ParticipationEditor;
import karstenroethig.pokerstats.editor.ParticipationEditorInput;
import karstenroethig.pokerstats.model.Participation;
import karstenroethig.pokerstats.model.ParticipationModel;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class NewParticipationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Participation participation = ParticipationModel.getInstance().createNewParticipation();
		
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();
		ParticipationEditorInput input = new ParticipationEditorInput( participation );
		
		try{
			page.openEditor( input, ParticipationEditor.ID );
		}catch( PartInitException ex ) {
			throw new RuntimeException( ex );
		}
		
		return null;
	}

}
