package se.kth.ev.gmapsviz;

import java.util.Observable;
import java.util.Observer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ElvizFragment extends Fragment implements Observer {
	ElvizSurface canvas;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
      View v = inflater.inflate(R.layout.fragment_elviz, container, false);
      canvas = (ElvizSurface) v.findViewById(R.id.elviz_surf);
      return v;
    }

	@Override
	public void update(Observable observable, Object data) {
		if(canvas != null)
		canvas.update(observable, data);
	}
	
}
