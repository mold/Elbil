package se.kth.ev.gmapsviz;

import java.util.Observable;
import java.util.Observer;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.kth.ev.differentiatedrange.CarData;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class EVGraph extends Fragment implements Observer {
	private static CarData cd;
	private GraphicalView mChart;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private XYSeries mCurrentSeries;
	private XYSeriesRenderer mCurrentRenderer;
	private EnergyEstimator ee;
	private Thread ee_t;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (cd == null)
			if (getActivity() instanceof MainActivity) {
				cd = ((MainActivity) getActivity()).cd;
				cd.addObserver(this);
			}
		if (ee_t == null) {
			ee = new EnergyEstimator();
			ee.addObserver(this);
			ee_t = new Thread(ee);
			ee_t.start();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		return inflater.inflate(R.layout.fragment_evgraph, container, false);
	}

	public void init() {
		mCurrentSeries = new XYSeries("Predicted energy consumption");
		mDataset.addSeries(mCurrentSeries);
		mCurrentRenderer = new XYSeriesRenderer();
		mRenderer.addSeriesRenderer(mCurrentRenderer);
	}

	@Override
	public void onPause() {
		super.onPause();
		LinearLayout layout = (LinearLayout) getView().findViewById(R.id.chart);
		layout.removeAllViews();
	}

	public void onResume() {
		super.onResume();

		getActivity().setRequestedOrientation(
				ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		LinearLayout layout = (LinearLayout) getView().findViewById(R.id.chart);
		if (mChart == null) {
			init();
			mChart = ChartFactory.getCubeLineChartView(getActivity(), mDataset,
					mRenderer, 0.3f);
			layout.addView(mChart);
		}
		if (!layout.equals(mChart.getParent())) {
			layout.removeView(mChart);
			layout.addView(mChart);
		} else
			mChart.repaint();
	}

	double soc = 0;
	long time_when = System.currentTimeMillis();
	boolean hasData;

	@Override
	public void update(Observable observable, Object data) {
		if (getView() == null || ee_t == null)
			return;
		if (observable instanceof EnergyEstimator) {
			updateViz();
		}
		if (observable instanceof CarData) {
			//updateViz();
		}
	}

	private void updateViz() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				double[] consumption = cd.determineConsumption(ee.data);
				mCurrentSeries.clear();
				double xval = ee.data.get(0).distance.value;
				for (int i = 0; i < consumption.length; i++) {
					mCurrentSeries.add(xval/1000.0f,
							consumption[i]);
					xval += ee.data.get(i).distance.value;
				}

				LinearLayout layout = (LinearLayout) getView().findViewById(
						R.id.chart);
				layout.removeAllViews();
				mChart = ChartFactory.getCubeLineChartView(getActivity(),
						mDataset, mRenderer, 0.5f);
				layout.addView(mChart);
			}
		});

	}
}
