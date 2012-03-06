package karstenroethig.pokerstats.handler;

import karstenroethig.pokerstats.editor.StatsEditor;
import karstenroethig.pokerstats.editor.StatsEditorInput;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenStatsEditorHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();
		StatsEditorInput input = new StatsEditorInput();
		
		try{
			page.openEditor( input, StatsEditor.ID );
		}catch( PartInitException ex ) {
			throw new RuntimeException( ex );
		}
		
		return null;
	}

}
