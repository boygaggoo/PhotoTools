package hu.bute.gb.onlab.PhotoTools.fragment;

import hu.bute.gb.onlab.PhotoTools.R;
import hu.bute.gb.onlab.PhotoTools.entities.Location;
import hu.bute.gb.onlab.PhotoTools.helpers.Coordinate;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class LocationsDetailFragment extends SherlockFragment {

	public static final String KEY_LOCATION = "location";

	private Activity activity_;
	private Location location_;
	private boolean tabletSize_ = false;

	private ImageView imageViewMap_;
	private TextView textViewAddress_;
	private TextView textViewLatitude_;
	private TextView textViewLongitude_;
	private CheckBox checkBoxCarEntry_;
	private CheckBox checkBoxPowerSource_;
	private TextView textViewNotes_;
	
	private ViewGroup fragmentContainer_;
	private FragmentManager fragmentManager_;
	private LocationsMapFragment locationsMapFragment_;

	public static LocationsDetailFragment newInstance(Location location) {
		LocationsDetailFragment result = new LocationsDetailFragment();

		Bundle arguments = new Bundle();
		arguments.putParcelable(KEY_LOCATION, location);
		result.setArguments(arguments);

		return result;
	}

	public static LocationsDetailFragment newInstance(Bundle bundle) {
		LocationsDetailFragment result = new LocationsDetailFragment();
		result.setArguments(bundle);
		return result;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activity_ = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			if (getArguments() != null) {
				location_ = getArguments().getParcelable(KEY_LOCATION);
			}
		}
		else if (savedInstanceState != null) {
			location_ = savedInstanceState.getParcelable(KEY_LOCATION);
		}

		tabletSize_ = getResources().getBoolean(R.bool.isTablet);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		View view = inflater.inflate(R.layout.fragment_locations_detail, container, false);
		
		fragmentContainer_ = (ViewGroup) view.findViewById(R.id.mapFragmentContainer);
		fragmentManager_ = getChildFragmentManager();
		ArrayList<Location> locations = new ArrayList<Location>();
		locations.add(location_);
		locationsMapFragment_ = LocationsMapFragment.newInstance(false, locations);
		FragmentTransaction fragmentTransaction = fragmentManager_.beginTransaction();
		fragmentTransaction.replace(R.id.mapFragmentContainer, locationsMapFragment_);
		fragmentTransaction.commit();

		textViewAddress_ = (TextView) view.findViewById(R.id.textViewAddress);
		textViewLatitude_ = (TextView) view.findViewById(R.id.textViewLatitude);
		textViewLongitude_ = (TextView) view.findViewById(R.id.textViewLongitude);
		checkBoxCarEntry_ = (CheckBox) view.findViewById(R.id.checkBoxCarEntry);
		checkBoxPowerSource_ = (CheckBox) view.findViewById(R.id.checkBoxPowerSource);
		textViewNotes_ = (TextView) view.findViewById(R.id.textViewNotes);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		onLocationChanged(location_);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_LOCATION, location_);
		super.onSaveInstanceState(outState);
	}

	public void onLocationChanged(Location location) {
		location_ = location;
		if (location_ != null) {
			// Set the activity title on phones
			if (!tabletSize_) {
				activity_.setTitle(location_.getName());
			}
			textViewAddress_.setText(location_.getAddress());
			Coordinate coordinate = location_.getCoordinate();
			textViewLatitude_.setText(Double.toString(coordinate.getLatitude()));
			textViewLongitude_.setText(Double.toString(coordinate.getLongitude()));
			checkBoxCarEntry_.setChecked(location_.hasCarEntry());
			checkBoxPowerSource_.setChecked(location_.hasPowerSource());
			textViewNotes_.setText(location_.getNotes());
		}
	}

	public long getSelectedLocationId() {
		return location_.getID();
	}
}
