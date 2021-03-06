package de.bjrn.budgetbook.view.swing.evaluations;

import de.bjrn.budgetbook.logic.Utils;
import de.bjrn.budgetbook.model.AccountTransaction;
import de.bjrn.budgetbook.model.AccountTransactionList;
import de.bjrn.budgetbook.model.Category;
import de.bjrn.budgetbook.model.TimeWindow;
import de.bjrn.budgetbook.view.i18.I18;
import de.bjrn.budgetbook.view.swing.BBViewEvaluations;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public abstract class EvaluationsViewCategoryMonth extends EvaluationsViewPerMonth {
	private static final long serialVersionUID = 1L;
	
	private List<Paint> colors;
	
	public EvaluationsViewCategoryMonth(BBViewEvaluations view, List<AccountTransaction> txs, EvaluationConfig config) {
		super(view, txs, config);
	}

	@Override
	protected CategoryDataset createDataset() {
		DefaultCategoryDataset ds = new DefaultCategoryDataset();
		colors = new Vector<>();
		for (Category cat : getCategories()) {
			colors.add(cat.getColor());
			Object[][] data = createDatasetData(cat);
			if (data != null) {
		        String label = getName(cat);
				for (int i = 0; i < data[0].length; i++) {
		        	Double val = (Double)data[1][i];
			        ds.addValue((double) val, label, (TimeWindow) data[0][i]);
		        }
			}
		}
        return ds;
	}
	
	@Override
	protected JFreeChart createChart() {
		JFreeChart chart = createChart(null, I18.tLabel("Time"), I18.tLabel("Amount"), createDataset(), PlotOrientation.VERTICAL, true, true, false);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		AbstractRenderer renderer = (AbstractRenderer)plot.getRenderer();
		int series=0;
		for (Category cat : getCategories()) {
			renderer.setSeriesPaint(series, cat.getColor());
			series++;
		}
		return chart;
	}

	protected abstract JFreeChart createChart(String title, String domainAxisLabel, String rangeAxisLabel,
			CategoryDataset dataset, PlotOrientation orientation, boolean legend, boolean tooltips, boolean urls);

	private Object[][] createDatasetData(Category cat) {
		AccountTransactionList txsFiltered = new AccountTransactionList();
		Set<Long> catsRecursive = cat.equals(parentCategory) ? Set.of(cat.getLongId()) : getCategoriesRecursive(cat);
		for (AccountTransaction tx : txs) {
			if (catsRecursive.contains(tx.getCategory())) {
				txsFiltered.add(tx);
			}
		}
		if (txsFiltered.isEmpty()) {
			return null;
		}
		Object[][] data = new Object[2][getMonths()];
		boolean content = false;
		for (int m = 0; m < getMonths(); m++) {
			LocalDate start = getBase().plusMonths(m);
			LocalDate end = start.plusMonths(1);
        	String dateText = start.getMonthValue() + "/" + (start.getYear() % 100);
			data[0][m] = new TimeWindow(dateText, start, end);
			long amount = new AccountTransactionList(txsFiltered, start, end).getAmount(config.isOutgoings());
			content |= amount != 0;
			double val = Utils.round(amount / 100.0, 2);
			data[1][m] = val;
		}
		return content ? data : null;
	}

}
