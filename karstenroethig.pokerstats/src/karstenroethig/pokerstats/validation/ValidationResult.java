package karstenroethig.pokerstats.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

	private List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
	
	public ValidationResult() {
	}
	
	public void addError( String message, String propertyId ) {
		errors.add( new ValidationMessage( message, propertyId ) );
	}
	
	public List<ValidationMessage> getErrors() {
		return errors;
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	public boolean isOK() {
		return errors.isEmpty();
	}
	
	public String createSingleLineMessage() {
		
		StringBuffer msg = new StringBuffer();
		boolean first = true;
		
		for( ValidationMessage validationMessage : getErrors() ) {
			
			if( first ) {
				first = false;
			} else {
				msg.append( "\n\n" );
			}
			
			msg.append( validationMessage.getMessage() );
		}
		
		return msg.toString();
	}
}
