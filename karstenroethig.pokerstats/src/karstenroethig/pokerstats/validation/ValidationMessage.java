package karstenroethig.pokerstats.validation;

public class ValidationMessage {

	private String message;
	
	private String propertyId;
	
	public ValidationMessage( String message, String propertyId ) {
		this.message = message;
		this.propertyId = propertyId;
	}

	public String getMessage() {
		return message;
	}

	public String getPropertyId() {
		return propertyId;
	}
}
