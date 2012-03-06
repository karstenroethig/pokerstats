package karstenroethig.pokerstats.model;

public class SaveResult {
	
	public static final SaveResult SUCCESS = new SaveResult();

	private String errorMessage;
	
	private Throwable throwable;
	
	private SaveResult(){
	}
	
	public SaveResult( String errorMessage, Throwable throwable ) {
		this.errorMessage = errorMessage;
		this.throwable = throwable;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public Throwable getThrowable() {
		return throwable;
	}
	
	public boolean isSaveSuccessful() {
		return this == SUCCESS;
	}
	
}
