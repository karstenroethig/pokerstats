package karstenroethig.pokerstats.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

public class UIUtils {

	public static Spinner createMoneySpinner( Composite parent, int maxCents ) {
		return createSpinner( parent, 2, 0, maxCents, 100 );
	}
	
	public static Spinner createIntegerSpinner( Composite parent, int maxValue ) {
		
		// Spinner für natürliche Zahlen
		return createSpinner( parent, 0, 0, maxValue, 1 );
	}
	
	public static Spinner createSpinner( Composite parent, int digits, int minValue, int maxValue, int increment ) {
		Spinner spinner = new Spinner( parent, SWT.BORDER );
		
		spinner.setDigits( digits );
		spinner.setMinimum( minValue );
		spinner.setMaximum( maxValue );
		spinner.setIncrement( increment );
		
		return spinner;
	}
}
