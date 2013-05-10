package hu.bute.gb.onlab.PhotoTools;

import hu.bute.gb.onlab.PhotoTools.entities.Equipment;
import hu.bute.gb.onlab.PhotoTools.fragment.EquipmentDetailFragment;
import hu.bute.gb.onlab.PhotoTools.fragment.EquipmentListFragment;
import hu.bute.gb.onlab.PhotoTools.fragment.MenuListFragment;
import hu.bute.gb.onlab.PhotoTools.helpers.EquipmentCategories;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.slidingmenu.lib.SlidingMenu;

public class EquipmentActivity extends SherlockFragmentActivity {

	//public List<Long> equipmentOnView = new ArrayList<Long>();
	public static final int EQUIPMENT_DELETE = 1;
	public static final int EQUIPMENT_ADD = 2;
	public EquipmentCategories categories;

	private MenuItem searchItem_ = null;
	private ViewGroup fragmentContainer_;
	private FragmentManager fragmentManager_;
	private EquipmentListFragment equipmentListFragment_;
	private SlidingMenu menu;

	private boolean tabletSize_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_equipment);

		fragmentContainer_ = (ViewGroup) findViewById(R.id.EquipmentFragmentContainer);
		fragmentManager_ = getSupportFragmentManager();
		equipmentListFragment_ = (EquipmentListFragment) fragmentManager_
				.findFragmentById(R.id.equipmentlist_fragment);

		// Configure the SlidingMenu
		menu = new SlidingMenu(this);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, MenuListFragment.newInstance(2, menu)).commit();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		/*if (getIntent().getExtras() != null) {
			Bundle arguments = getIntent().getExtras();
			long index = arguments.getLong("index");
			equipmentOnView.add(Long.valueOf(index));
			showEquipmentDetails(0);
		}*/
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Close Sliding menu if it was open
		if (menu != null) {
			menu.showContent(false);
		}
	}

	public void showEquipmentDetails(Equipment selectedEquipment) {
		if (fragmentContainer_ != null) {
			EquipmentDetailFragment detailFragment = (EquipmentDetailFragment) fragmentManager_
					.findFragmentById(R.id.EquipmentFragmentContainer);
			if (detailFragment == null
					|| detailFragment.getSelectedEquipmentId() != selectedEquipment.getID()) {
				detailFragment = EquipmentDetailFragment.newInstance(selectedEquipment);
				FragmentTransaction fragmentTransaction = fragmentManager_.beginTransaction();
				fragmentTransaction.replace(R.id.EquipmentFragmentContainer, detailFragment);
				fragmentTransaction.commit();
			}
		}
		else {
			Intent intent = new Intent(this, EquipmentDetailActivity.class);
			intent.putExtra(EquipmentDetailFragment.KEY_EQUIPMENT, selectedEquipment);
			startActivityForResult(intent, EQUIPMENT_DELETE); // Listen for delete event
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			// Location deleted
			switch (requestCode) {
			case EQUIPMENT_DELETE:
				if (resultCode == RESULT_OK) {
					equipmentListFragment_.refreshList();
				}
				break;
			case EQUIPMENT_ADD:
				equipmentListFragment_.refreshList();
				break;
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent myIntent = new Intent();
			myIntent.setClass(EquipmentActivity.this, MainActivity.class);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(myIntent);
			finish();
			return true;
		case R.id.action_search:
			InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			return true;
		case R.id.action_new_equipment:
			Intent newIntent = new Intent();
			newIntent.setClass(EquipmentActivity.this, EquipmentEditActivity.class);
			newIntent.putExtra("edit", false);
			startActivityForResult(newIntent, 2);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.equipment, menu);
		
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				equipmentListFragment_.search(newText);
				return false;
			}
		});
		
		searchItem_ = (MenuItem) menu.getItem(0);
		searchItem_.setOnActionExpandListener(new OnActionExpandListener() {
			
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				equipmentListFragment_.searchFilter = null;
				equipmentListFragment_.refreshList();
				return true;
			}
		});
		
		/*final EditText editTextSearch = (EditText) menu.findItem(R.id.action_search).getActionView();
		editTextSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before,
					int count) {
				//equipmentListFragment_.populateList(charSequence);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		MenuItem searchItem = (MenuItem) menu.getItem(0);
		searchItem.setOnActionExpandListener(new OnActionExpandListener() {
			
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				editTextSearch.requestFocus();
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				editTextSearch.setText("");
				//equipmentListFragment_.populateList(null);
				return true;
			}
		});*/
		
		return super.onCreateOptionsMenu(menu);
	}
}