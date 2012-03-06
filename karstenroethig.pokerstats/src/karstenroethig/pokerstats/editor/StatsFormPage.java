package karstenroethig.pokerstats.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.List;

import karstenroethig.pokerstats.enums.StatsTimeUnitEnum;
import karstenroethig.pokerstats.model.PrizeScaling;
import karstenroethig.pokerstats.model.Tournament;
import karstenroethig.pokerstats.model.TournamentModel;
import karstenroethig.pokerstats.util.Colors;
import karstenroethig.pokerstats.util.Images;
import karstenroethig.pokerstats.view.TournamentOverviewView;
import karstenroethig.pokerstats.viewers.TournamentContentProvider;
import karstenroethig.pokerstats.viewers.TournamentLabelProvider;
import net.ffxml.swtforms.builder.PanelBuilder;
import net.ffxml.swtforms.layout.CellConstraints;
import net.ffxml.swtforms.layout.FormLayout;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

public class StatsFormPage extends FormPage {
	
	private static final int COL_EMPTY = 0;
	
	private static final int COL_TIME = 1;
	
	private static final int COL_BUYIN = 2;
	
	private static final int COL_PRIZEMONEY = 3;
	
	private static final int COL_BENEFIT = 4;
	
	private ComboViewer comboViewerTournament;
	
	private Combo comboView;

	public StatsFormPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		
		Composite formBody = managedForm.getForm().getBody();
		
		// Hintergrundfarbe auf weiß setzen
		formBody.setBackground( Colors.getColor( Colors.WHITE ) );
		formBody.setBackgroundMode( SWT.INHERIT_FORCE );
		
		/*
		 * Erstellen der Eingabefelder
		 */
		// Turnier
		Link linkTournament = new Link( formBody, SWT.NONE );
		linkTournament.setText( "<a>Turnier</a>" );
		linkTournament.addListener( SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent( Event event ) {
				
				if( comboViewerTournament == null
		    			|| comboViewerTournament.getSelection() == null
		    			|| comboViewerTournament.getSelection().isEmpty() ) {
					return;
		    	}
		    	
		    	IStructuredSelection selection = ( IStructuredSelection )comboViewerTournament.getSelection();
		    	Tournament tournament = (Tournament)selection.getFirstElement();
				IWorkbenchPage page = getEditorSite().getPage();
				TournamentEditorInput input = new TournamentEditorInput( tournament );
				
				try{
					page.openEditor( input, TournamentEditor.ID );
				}catch( PartInitException ex ) {
					throw new RuntimeException( ex );
				}
			}
		});
		
		Combo comboTournament = createComboTournaments( formBody );
		
		// Anzeige
		comboView = new Combo( formBody, SWT.READ_ONLY );
		comboView.setItems( StatsTimeUnitEnum.getValues() );
		
		// Aktualisieren-Button
		final Button buttonRefresh = new Button( formBody, SWT.PUSH );
		buttonRefresh.setImage( Images.getImage( Images.ICON_REFRESH ) );
		buttonRefresh.setToolTipText( "Statistik aktualisieren" );
		buttonRefresh.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				buttonRefresh.setEnabled( false );
			}
		});
		
		// Tabs
		CTabFolder tabFolder = new CTabFolder( formBody, SWT.BORDER );
		
		addTableTabItem( tabFolder, "Tabelle" );
//		addBarChartTabItem( tabFolder, "Balkendiagramm" );
		addStackedBarChartTabItem( tabFolder, "Balkendiagramm" );
		addDifferenceChartTabItem( tabFolder, "Graph" );
//		addTabItem( tabFolder, "Balkendiagramm", Colors.getColor( 0, 255, 0 ) );
//		addTabItem( tabFolder, "Graph", Colors.getColor( 0, 0, 255 ) );
		
		tabFolder.setSelection( 0 );
		
		/*
		 * Layout der Komponenten
		 */
		String colSpec = "3dlu, l:p, 3dlu, p, 3dlu, f:p:g, 3dlu";
		String rowSpec = "3dlu, p, 3dlu, p, 3dlu, p, 3dlu, f:p:g, 3dlu";
		FormLayout layout = new FormLayout( colSpec, rowSpec );
		PanelBuilder builder = new PanelBuilder( layout, formBody );
		CellConstraints cc = new CellConstraints();
		
		// Turnier
		int row = 2;
		builder.add( linkTournament, cc.xy( 2, row ) );
		builder.add( comboTournament, cc.xyw( 4, row, 3 ) );
		
		// Anzeige
		row = 4;
		builder.addLabel( "Anzeige", cc.xy( 2, row ) );
		builder.add( comboView, cc.xy( 4, row ) );
		
		// Aktualisieren-Button
		row = 6;
		builder.add( buttonRefresh, cc.xy( 2, row) );
		
		// Tabs
		row = 8;
		builder.add( tabFolder, cc.xyw( 2, row, 5 ) );
	}
	
	private Combo createComboTournaments( Composite parent ) {
		
		comboViewerTournament = new ComboViewer( parent );
		comboViewerTournament.setContentProvider( new TournamentContentProvider() );
		comboViewerTournament.setLabelProvider( new TournamentLabelProvider() );
		comboViewerTournament.getCombo().setVisibleItemCount( 10 );
		comboViewerTournament.setInput( TournamentModel.getInstance().getTournaments() );
		
		TournamentOverviewView.registerTournamentComboViewer( comboViewerTournament );
		
		return comboViewerTournament.getCombo();
	}
	
	private void addTableTabItem( CTabFolder tabFolder, String text ) {
		
		TableViewer tableViewerStats = new TableViewer( tabFolder, SWT.FULL_SELECTION | SWT.BORDER );
		Table table = tableViewerStats.getTable();
		
		table.setHeaderVisible( true );
		table.setLinesVisible( true );
		
		tableViewerStats.setContentProvider( new StatsContentProvider() );
		tableViewerStats.setLabelProvider( new StatsLabelProvider() );
		
		/*
		 * Spalten hinzufügen
		 */
		TableColumn[] columns = new TableColumn[5];
		
		for( int i = 0; i < columns.length; i++ ) {
			columns[i] = new TableColumn( table, SWT.RIGHT );
		}
		
		// Leere Spalte, weil da das Ausrichten nach rechts Probleme macht
		columns[COL_EMPTY].setMoveable( false );
		columns[COL_EMPTY].setResizable( false );
		columns[COL_EMPTY].setText( StringUtils.EMPTY );
		
		// Zeit
		columns[COL_TIME].setMoveable( false );
		columns[COL_TIME].setText( "Zeit" );
		
		// Buy-in
		columns[COL_BUYIN].setMoveable( false );
		columns[COL_BUYIN].setText( "Buy-in" );
		
		// Preisgeld
		columns[COL_PRIZEMONEY].setMoveable( false );
		columns[COL_PRIZEMONEY].setText( "Preisgeld" );
		
		// Gewinn
		columns[COL_BENEFIT].setMoveable( false );
		columns[COL_BENEFIT].setText( "Gewinn" );
		
		TableLayout layout = new TableLayout();
		
		layout.addColumnData( new ColumnPixelData( 0 ) );
		layout.addColumnData( new ColumnPixelData( 80 ) );
		layout.addColumnData( new ColumnPixelData( 120 ) );
		layout.addColumnData( new ColumnPixelData( 120 ) );
		layout.addColumnData( new ColumnPixelData( 120 ) );
		
		table.setLayout( layout );
		
		CTabItem item = new CTabItem( tabFolder, SWT.NONE );
		
		item.setText ( text );
		item.setControl( table );
	}
	
	private void addBarChartTabItem( CTabFolder tabFolder, String text ) {
		
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		// row keys ...
		String series1 = "First";
		String series2 = "Second";
		
		// column keys ...
		String category1 = "Label 1";
		String category2 = "Label 2";
		String category3 = "Label 3";
		
		dataset.addValue( 1.0, series1, category1 );
		dataset.addValue( 4.0, series1, category2 );
		dataset.addValue( 3.0, series1, category3 );
		
		dataset.addValue( 5.0, series2, category1 );
		dataset.addValue( 7.0, series2, category2 );
		dataset.addValue( 6.0, series2, category3 );
		
		JFreeChart chart = ChartFactory.createBarChart( "BarChart", // chart title
				"Labels", // domain axis label
				"Values", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips
				false // URLs?
				);
		
		CategoryPlot plot = (CategoryPlot)chart.getPlot();
		plot.setBackgroundPaint( Color.lightGray );
		plot.setDomainGridlinePaint( Color.white );
		plot.setDomainGridlinesVisible( true );
		plot.setRangeGridlinePaint( Color.white );
		
		ChartComposite chartComposite = new ChartComposite( tabFolder, SWT.NONE, chart, true );
		
		CTabItem item = new CTabItem( tabFolder, SWT.NONE );
		
		item.setText ( text );
		item.setControl( chartComposite );
	}
	
	private void addStackedBarChartTabItem( CTabFolder tabFolder, String text ) {
		
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		// row keys ...
		String series1 = "Series 1";
		String series2 = "Series 2";
		String series3 = "Series 3";
		String series4 = "Series 4";
		
		// column keys ...
		String category1 = "Jan";
		String category2 = "Feb";
		String category3 = "Mar";
		
		dataset.addValue( 10.0D, series1, category1 );
		dataset.addValue( 12.0D, series1, category2 );
		dataset.addValue( 13.0D, series1, category3 );
		
		dataset.addValue( 4.0D, series2, category1 );
		dataset.addValue( 3.0D, series2, category2 );
		dataset.addValue( 2.0D, series2, category3 );
		
		dataset.addValue( 2.0D, series3, category1 );
		dataset.addValue( 3.0D, series3, category2 );
		dataset.addValue( 2.0D, series3, category3 );
		
		dataset.addValue( 2.0D, series4, category1 );
		dataset.addValue( 3.0D, series4, category2 );
		dataset.addValue( 4.0D, series4, category3 );
		
		JFreeChart chart = ChartFactory.createStackedBarChart(
				"Stacked Bar Chart Demo 3",
				"Category",
				"Value",
				dataset,
				PlotOrientation.VERTICAL,
				true,
				false,
				false);
		
	    CategoryPlot plot = (CategoryPlot)chart.getPlot();
	    
	    ExtendedStackedBarRenderer extendedStackedBarRenderer = new ExtendedStackedBarRenderer();
	    extendedStackedBarRenderer.setBaseItemLabelsVisible( true );
	    extendedStackedBarRenderer.setBaseItemLabelGenerator( new StandardCategoryItemLabelGenerator() );
	    extendedStackedBarRenderer.setBaseToolTipGenerator( new StandardCategoryToolTipGenerator() );
	    plot.setRenderer( extendedStackedBarRenderer );
	    
	    NumberAxis numberAxis = (NumberAxis)plot.getRangeAxis();
	    numberAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
	    numberAxis.setLowerMargin( 0.15D );
	    numberAxis.setUpperMargin( 0.15D );
	    numberAxis.setNumberFormatOverride( NumberFormat.getPercentInstance() );
		
		ChartComposite chartComposite = new ChartComposite( tabFolder, SWT.NONE, chart, true );
		
		CTabItem item = new CTabItem( tabFolder, SWT.NONE );
		
		item.setText ( text );
		item.setControl( chartComposite );
	}
	
	private void addDifferenceChartTabItem( CTabFolder tabFolder, String text ) {
		
		TimeSeries timeSeries1 = new TimeSeries( "Random 1" );
		TimeSeries timeSeries2 = new TimeSeries( "Random 2" );
		
		double d1 = 0.0D;
		double d2 = 0.0D;
		
		Day day = new Day();
		
		for( int i = 0; i < 200; i++ ) {
			
			d1 = d1 + Math.random() - 0.5D;
			d2 = d2 + Math.random() - 0.5D;
			
			timeSeries1.add( day, d1 );
			timeSeries2.add( day, d2 );
			
			day = (Day)day.next();
		}
		
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
		timeSeriesCollection.addSeries( timeSeries1 );
		timeSeriesCollection.addSeries( timeSeries2 );
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart( "Difference Chart Demo 1",
				"Time",
				"Value",
				timeSeriesCollection,
				true,
				true,
				false );
		
		XYPlot plot = (XYPlot)chart.getPlot();
		plot.setDomainPannable( true );
		
		XYDifferenceRenderer xyDifferenceRenderer = new XYDifferenceRenderer( Color.green, Color.red, false );
		xyDifferenceRenderer.setRoundXCoordinates( true );
		
		plot.setDomainCrosshairLockedOnData( true );
		plot.setRangeCrosshairLockedOnData( true );
		plot.setDomainCrosshairVisible( true );
		plot.setRangeCrosshairVisible( true );
		plot.setRenderer( xyDifferenceRenderer );
		
		DateAxis dateAxis = new DateAxis( "Time" );
		dateAxis.setLowerMargin( 0.0D );
		dateAxis.setUpperMargin( 0.0D );
		plot.setDomainAxis( dateAxis );
		plot.setForegroundAlpha( 0.5F );
		
		ChartComposite chartComposite = new ChartComposite( tabFolder, SWT.NONE, chart, true );
		
		CTabItem item = new CTabItem( tabFolder, SWT.NONE );
		
		item.setText ( text );
		item.setControl( chartComposite );
	}
	
	private void addTabItem( CTabFolder tabFolder, String text, org.eclipse.swt.graphics.Color color ) {
		
		CTabItem item = new CTabItem( tabFolder, SWT.NONE );
		
		item.setText ( text );
		
		Label label = new Label( tabFolder, SWT.NONE );
		label.setBackground( color );
		
		item.setControl( label );
	}
	
	private class StatsContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public Object[] getElements(Object inputElement) {
			
			if( inputElement instanceof List ) {
				return ((List<PrizeScaling>) inputElement).toArray( new PrizeScaling[0] );
			}

			return null;
		}
		
	}
	
	private class StatsLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			
			if( element instanceof PrizeScaling ) {
				
				PrizeScaling prizeScaling = (PrizeScaling)element;
				
				if( columnIndex == COL_TIME ) {
					
					if( prizeScaling.getParticipantsFrom() != null ) {
						return prizeScaling.getParticipantsFrom().toString();
					}
				} else if( columnIndex == COL_BUYIN ) {
					
					if( prizeScaling.getParticipantsTo() != null ) {
						return prizeScaling.getParticipantsTo().toString();
					}
				} else if( columnIndex == COL_PRIZEMONEY ) {
					
					if( prizeScaling.getParticipantsInPrizes() != null ) {
						return prizeScaling.getParticipantsInPrizes().toString();
					}
				} else if( columnIndex == COL_BENEFIT ) {
					
					if( prizeScaling.getParticipantsInPrizes() != null ) {
						return prizeScaling.getParticipantsInPrizes().toString();
					}
				}
			}
			
			return "???";
		}
		
	}
	
	private class ExtendedStackedBarRenderer extends StackedBarRenderer {
		
		private boolean showPositiveTotal = true;
		
		private boolean showNegativeTotal = true;
		
		private Font totalLabelFont = new Font( "SansSerif", 1, 12 );
		
		private NumberFormat totalFormatter = NumberFormat.getInstance();
		
		public NumberFormat getTotalFormatter() {
			return this.totalFormatter;
		}
		
		public void setTotalFormatter( NumberFormat paramNumberFormat ) {
			if( paramNumberFormat == null ) {
				throw new IllegalArgumentException( "Null format not permitted." );
			}
			
			this.totalFormatter = paramNumberFormat;
		}
		
		public void drawItem( Graphics2D paramGraphics2D,
				CategoryItemRendererState paramCategoryItemRendererState,
				Rectangle2D paramRectangle2D,
				CategoryPlot paramCategoryPlot,
				CategoryAxis paramCategoryAxis,
				ValueAxis paramValueAxis,
				CategoryDataset paramCategoryDataset,
				int paramInt1,
				int paramInt2,
				int paramInt3 ) {
			
			Number localNumber1 = paramCategoryDataset.getValue( paramInt1, paramInt2 );
			
			if( localNumber1 == null ) {
				return;
			}

			double d1 = localNumber1.doubleValue();
			PlotOrientation localPlotOrientation = paramCategoryPlot.getOrientation();
			double d2 = paramCategoryAxis.getCategoryMiddle( paramInt2,
					getColumnCount(),
					paramRectangle2D,
					paramCategoryPlot.getDomainAxisEdge()) - paramCategoryItemRendererState.getBarWidth() / 2.0D;
			double d3 = 0.0D;
			double d4 = 0.0D;
			double d6;
			
			for( int i = 0; i < paramInt1; i++ ) {
				
				Number localNumber2 = paramCategoryDataset.getValue( i, paramInt2 );
				
				if (localNumber2 == null) {
					continue;
				}
				
				d6 = localNumber2.doubleValue();
				
				if( d6 > 0.0D ) {
					d3 += d6;
				} else {
					d4 += d6;
				}
			}
			
			RectangleEdge localRectangleEdge = paramCategoryPlot.getRangeAxisEdge();
			double d5;
			
			if( d1 > 0.0D ) {
				d5 = paramValueAxis.valueToJava2D( d3, paramRectangle2D, localRectangleEdge );
				d6 = paramValueAxis.valueToJava2D( d3 + d1, paramRectangle2D, localRectangleEdge );
			} else {
				d5 = paramValueAxis.valueToJava2D( d4, paramRectangle2D, localRectangleEdge );
				d6 = paramValueAxis.valueToJava2D( d4 + d1, paramRectangle2D, localRectangleEdge );
			}
			
			double d7 = Math.min( d5, d6 );
			double d8 = Math.max( Math.abs( d6 - d5 ), getMinimumBarLength() );
			Rectangle2D.Double localDouble = null;
			
			if( localPlotOrientation == PlotOrientation.HORIZONTAL ) {
				localDouble = new Rectangle2D.Double(d7, d2, d8, paramCategoryItemRendererState.getBarWidth());
			} else {
				localDouble = new Rectangle2D.Double( d2, d7, paramCategoryItemRendererState.getBarWidth(), d8 );
			}
			
			Paint localPaint = getItemPaint( paramInt1, paramInt2 );
			paramGraphics2D.setPaint( localPaint );
			paramGraphics2D.fill( localDouble );
			
			if( isDrawBarOutline() && paramCategoryItemRendererState.getBarWidth() > 3.0D ) {
				paramGraphics2D.setStroke( getItemStroke( paramInt1, paramInt2 ) );
				paramGraphics2D.setPaint( getItemOutlinePaint( paramInt1, paramInt2 ) );
				paramGraphics2D.draw( localDouble );
			}
			
			CategoryItemLabelGenerator localCategoryItemLabelGenerator = getItemLabelGenerator( paramInt1, paramInt2 );
			
			if( localCategoryItemLabelGenerator != null && isItemLabelVisible( paramInt1, paramInt2 ) ) {
				drawItemLabel( paramGraphics2D,
						paramCategoryDataset,
						paramInt1,
						paramInt2,
						paramCategoryPlot,
						localCategoryItemLabelGenerator,
						localDouble,
						d1 < 0.0D );
			}
			
			double d9;
			float f1;
			float f2;
			Object localObject;
			
			if( d1 > 0.0D ) {
				
				if( this.showPositiveTotal && isLastPositiveItem( paramCategoryDataset, paramInt1, paramInt2 ) ) {
					
					paramGraphics2D.setPaint( Color.black );
					paramGraphics2D.setFont( this.totalLabelFont );
					d9 = calculateSumOfPositiveValuesForCategory( paramCategoryDataset, paramInt2 );
					f1 = (float)localDouble.getCenterX();
					f2 = (float)localDouble.getMinY() - 4.0F;
					localObject = TextAnchor.BOTTOM_CENTER;
					
					if( localPlotOrientation == PlotOrientation.HORIZONTAL ) {
						
						f1 = (float)localDouble.getMaxX() + 4.0F;
						f2 = (float)localDouble.getCenterY();
						localObject = TextAnchor.CENTER_LEFT;
					}
					
					TextUtilities.drawRotatedString( this.totalFormatter.format(d9),
							paramGraphics2D, f1, f2, (TextAnchor)localObject, 0.0D, TextAnchor.CENTER );
				}
			} else if( this.showNegativeTotal && isLastNegativeItem( paramCategoryDataset, paramInt1, paramInt2 ) ) {
				
				paramGraphics2D.setPaint( Color.black );
				paramGraphics2D.setFont( this.totalLabelFont );
				d9 = calculateSumOfNegativeValuesForCategory( paramCategoryDataset, paramInt2 );
				f1 = (float)localDouble.getCenterX();
				f2 = (float)localDouble.getMaxY() + 4.0F;
				localObject = TextAnchor.TOP_CENTER;
				
				if( localPlotOrientation == PlotOrientation.HORIZONTAL ) {
					
					f1 = (float)localDouble.getMinX() - 4.0F;
					f2 = (float)localDouble.getCenterY();
					localObject = TextAnchor.CENTER_RIGHT;
				}
				
				TextUtilities.drawRotatedString( this.totalFormatter.format(d9),
						paramGraphics2D, f1, f2, (TextAnchor)localObject, 0.0D, TextAnchor.CENTER );
			}
			
			if( paramCategoryItemRendererState.getInfo() != null ) {
				
				EntityCollection localEntityCollection = paramCategoryItemRendererState.getEntityCollection();
				
				if( localEntityCollection != null ) {
					
					String str1 = null;
					CategoryToolTipGenerator localCategoryToolTipGenerator = getToolTipGenerator( paramInt1, paramInt2 );
					
					if( localCategoryToolTipGenerator != null ) {
						str1 = localCategoryToolTipGenerator.generateToolTip( paramCategoryDataset, paramInt1, paramInt2 );
					}
					
					String str2 = null;
					
					if( getItemURLGenerator( paramInt1, paramInt2 ) != null ) {
						str2 = getItemURLGenerator( paramInt1, paramInt2 ).generateURL( paramCategoryDataset, paramInt1, paramInt2 );
					}
					
					localObject = new CategoryItemEntity( localDouble,
							str1,
							str2,
							paramCategoryDataset,
							paramCategoryDataset.getRowKey( paramInt1 ),
							paramCategoryDataset.getColumnKey( paramInt2 ) );
					localEntityCollection.add( (ChartEntity)localObject );
				}
			}
		}
		
		private boolean isLastPositiveItem( CategoryDataset paramCategoryDataset, int paramInt1, int paramInt2 ) {
			
			int i = 1;
			Number localNumber = paramCategoryDataset.getValue( paramInt1, paramInt2 );
			
			if( localNumber == null ) {
				return false;
			}
			
			for( int j = paramInt1 + 1; j < paramCategoryDataset.getRowCount(); j++ ) {
				
				localNumber = paramCategoryDataset.getValue(j, paramInt2);
				
				if( localNumber == null ) {
					continue;
				}
				
				i = ( i != 0 ) && ( localNumber.doubleValue() <= 0.0D ) ? 1 : 0;
			}
			
			return i == 1;
		}
		
		private boolean isLastNegativeItem( CategoryDataset paramCategoryDataset, int paramInt1, int paramInt2 ) {
			
			int i = 1;
			Number localNumber = paramCategoryDataset.getValue( paramInt1, paramInt2 );
			
			if( localNumber == null ) {
				return false;
			}
			
			for( int j = paramInt1 + 1; j < paramCategoryDataset.getRowCount(); j++ ) {
				
				localNumber = paramCategoryDataset.getValue( j, paramInt2 );
				
				if( localNumber == null ) {
					continue;
				}
				
				i = ( i != 0 ) && ( localNumber.doubleValue() >= 0.0D ) ? 1 : 0;
			}
			
			return i == 1;
		}
		
		private double calculateSumOfPositiveValuesForCategory( CategoryDataset paramCategoryDataset, int paramInt ) {
			
			double d1 = 0.0D;
			
			for( int i = 0; i < paramCategoryDataset.getRowCount(); i++ ) {
				
				Number localNumber = paramCategoryDataset.getValue( i, paramInt );
				
				if( localNumber == null ) {
					continue;
				}
				
				double d2 = localNumber.doubleValue();
				
				if( d2 <= 0.0D ) {
					continue;
				}
				
				d1 += d2;
			}
			
			return d1;
		}
		
		private double calculateSumOfNegativeValuesForCategory( CategoryDataset paramCategoryDataset, int paramInt ) {
			
			double d1 = 0.0D;
			
			for( int i = 0; i < paramCategoryDataset.getRowCount(); i++ ) {
				
				Number localNumber = paramCategoryDataset.getValue( i, paramInt );
				
				if( localNumber == null ) {
					continue;
				}
				
				double d2 = localNumber.doubleValue();
				
				if( d2 >= 0.0D ) {
					continue;
				}
				
				d1 += d2;
			}
			
			return d1;
		}
	}
}
