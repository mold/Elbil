package se.kth.ev.gmapsviz;

import java.util.Observable;
import java.util.Observer;

import com.kth.ev.differentiatedrange.CarData;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ElvizFragment extends Fragment implements Observer {
	ElvizSurface canvas;
	CarData cd;
	RouteDataFetcher rdf;
	Thread t_rdf;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (cd == null)
			if (getActivity() instanceof MainActivity) {
				cd = ((MainActivity) getActivity()).cd;
				cd.addObserver(this);
			}
		if (t_rdf == null) {
			rdf = new RouteDataFetcher();
			rdf.addObserver(this);
			t_rdf = new Thread(rdf);
			t_rdf.start();
		}
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
      View v = inflater.inflate(R.layout.fragment_elviz, container, false);
      canvas = (ElvizSurface) v.findViewById(R.id.elviz_surf);
      return v;
    }

	@Override
	public void update(Observable observable, Object data) {
		if(canvas != null){
			if(observable instanceof RouteDataFetcher){
				canvas.addEvData(cd, rdf);
				canvas.redraw();
			}
		}
	}
	
}
