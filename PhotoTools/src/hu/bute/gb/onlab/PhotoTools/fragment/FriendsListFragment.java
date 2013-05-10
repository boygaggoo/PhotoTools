package hu.bute.gb.onlab.PhotoTools.fragment;

import hu.bute.gb.onlab.PhotoTools.FriendsActivity;
import hu.bute.gb.onlab.PhotoTools.R;
import hu.bute.gb.onlab.PhotoTools.application.PhotoToolsApplication;
import hu.bute.gb.onlab.PhotoTools.datastorage.DatabaseLoader;
import hu.bute.gb.onlab.PhotoTools.datastorage.DbConstants;
import hu.bute.gb.onlab.PhotoTools.datastorage.DummyModel;
import hu.bute.gb.onlab.PhotoTools.entities.Friend;
import hu.bute.gb.onlab.PhotoTools.helpers.FriendsAdapter;
import hu.bute.gb.onlab.PhotoTools.helpers.SeparatedListAdapter;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class FriendsListFragment extends SherlockListFragment {

	// Log tag
	public static final String TAG = "FriendsListFragment";
	public String searchFilter = null;
	public SeparatedListAdapter listAdapter = null;
	public boolean isAllSelected = true;
	public boolean isEmpty = true;

	private DummyModel model_;
	private ArrayList<String> usedCharacters_;
	private FriendsActivity activity_;
	private int selectedPosition_ = 0;

	// State
	private LocalBroadcastManager broadcastManager;

	// DBloader
	private DatabaseLoader databaseLoader;

	private GetAllTask[] getAllTask;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activity_ = (FriendsActivity) activity;
		model_ = DummyModel.getInstance();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		broadcastManager = LocalBroadcastManager.getInstance(getActivity());
		databaseLoader = PhotoToolsApplication.getDatabaseLoader();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setSelection(selectedPosition_);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// K�db�l regisztraljuk az adatbazis modosulasara figyelmezteto
		// Receiver-t
		IntentFilter filter = new IntentFilter(DbConstants.ACTION_DATABASE_CHANGED);
		broadcastManager.registerReceiver(updateDatabaseReceiver, filter);
		// Frissitjuk a lista tartalmat, ha visszater a user
		refreshList();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		// Kiregisztraljuk az adatbazis modosulasara figyelmezteto Receiver-t
		broadcastManager.unregisterReceiver(updateDatabaseReceiver);
		if (getAllTask != null) {
			for (GetAllTask task : getAllTask) {
				if (task != null) {
					task.cancel(false);
				}
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Ha van Cursor rendelve az Adapterhez, lezarjuk
		if (listAdapter != null) {
			for (String character : usedCharacters_) {
				FriendsAdapter adapter = (FriendsAdapter) listAdapter
						.getSectionAdapter(character);
				if (adapter != null && adapter.getCursor() != null) {
					adapter.getCursor().close();
				}
			}
		}
	}

	/*private class FriendItem {
		public String tag;
		public boolean hasLentItem = false;
		public int iconRes;

		public FriendItem(String tag, boolean hasLentItem, int iconRes) {
			this.tag = tag;
			this.hasLentItem = hasLentItem;
			this.iconRes = iconRes;
		}
	}

	public class FriendAdapter extends ArrayAdapter<FriendItem> {

		public FriendAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.friendsrow, null);
			}
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			// Only put up sign if has lent item(s)
			ImageView sign = (ImageView) convertView.findViewById(R.id.row_sign);
			if (!getItem(position).hasLentItem) {
				sign.setVisibility(View.INVISIBLE);
			}

			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);

			return convertView;
		}

	}*/

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("selectedPosition", selectedPosition_);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
		if (!isEmpty) {
			activity_.showFriendDetails(position);
			selectedPosition_ = position;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		activity_ = null;
	}

	public void search(CharSequence searchFilter) {
		if (isAllSelected) {
			//allFriendsSelected(searchFilter);
		}
		else {
			//lentToSelected(searchFilter);
		}

		if (isEmpty) {
			ArrayAdapter<String> emptyAdapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1);
			emptyAdapter.add(getResources().getString(R.string.search_no_result));
			setListAdapter(emptyAdapter);
		}
	}

	public void refreshList() {
		usedCharacters_ = databaseLoader.getUsedCharacters();
		if (usedCharacters_ != null) {
			listAdapter = new SeparatedListAdapter(getActivity());
			for (String character : usedCharacters_) {
				FriendsAdapter friendAdapter = new FriendsAdapter(getActivity()
				.getApplicationContext(), null);
				listAdapter.addSection(character, friendAdapter);
			}
			setListAdapter(listAdapter);
			
			if (getAllTask == null) {
				getAllTask = new GetAllTask[usedCharacters_.size()];
			}
			
			for (int i = 0; i < getAllTask.length; i++) {
				GetAllTask task = getAllTask[i];
				if (task != null) {
					task.cancel(false);
				}
				task = new GetAllTask(i, usedCharacters_.get(i));
				task.execute();
			}
		}
		else {
			isEmpty = true;
			ArrayAdapter<String> emptyAdapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1);
			emptyAdapter.add(getResources().getString(R.string.empty_friendlist));
			setListAdapter(emptyAdapter);
			listAdapter = null;
		}
	}
	
	private class GetAllTask extends AsyncTask<Void, Void, Cursor> {
		private static final String TAG = "GetAllTask";
		private int index_;
		private String category_;
		
		public GetAllTask(int index, String category){
			index_ = index;
			category_ = category;
		}

		@Override
		protected Cursor doInBackground(Void... params) {
			try {
				Cursor result = null;
				if (searchFilter != null) {
					if (isAllSelected) {
						result = databaseLoader.getAllFriendsByCategoryAndFilter(category_,
								searchFilter);
					}
					else {
						result = databaseLoader.getFriendsLentToByCategoryAndFilter(category_,
								searchFilter);
					}
				}
				else {
					if (isAllSelected) {
						result = databaseLoader.getAllFriendsByCategory(category_);
					}
					else {
						result = databaseLoader.getFriendsLentToByCategory(category_);
					}
				}
				if (!isCancelled()) {
					return result;
				}
				else {
					Log.d(TAG, "Cancelled, closing cursor");
					if (result != null) {
						result.close();
					}
					return null;
				}
			}
			catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			Log.d(TAG, "Fetch completed, displaying cursor results!");
			if (result != null) {
				isEmpty = false;
				try {				
				
					FriendsAdapter friendAdapter = (FriendsAdapter) listAdapter
							.getSectionAdapter(category_);
					friendAdapter.changeCursor(result);
					friendAdapter.notifyDataSetChanged();
					setListAdapter(listAdapter);
					getAllTask[index_] = null;
				}
				catch (Exception e) {
					Log.d("friend", "hiba!");
				}
			}
			if (listAdapter.isEmpty()) {
				isEmpty = true;
				ArrayAdapter<String> emptyAdapter = new ArrayAdapter<String>(getActivity(),
						android.R.layout.simple_list_item_1);
				emptyAdapter.add(getResources().getString(R.string.no_lent_to));
				setListAdapter(emptyAdapter);
				//listAdapter = null;
			}
		}
	}
	
	private BroadcastReceiver updateDatabaseReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshList();
		}
	};

	/*public void allFriendsSelected(CharSequence searchFilter) {
		activity_.friendsOnView.clear();
		isEmpty = true;

		// Create the friend list with custom adapter
		SeparatedListAdapter adapter = new SeparatedListAdapter(getActivity());
		for (Map.Entry<String, TreeSet<Friend>> alphabet : model_.friends.entrySet()) {

			FriendAdapter friendAdapter = new FriendAdapter(getActivity());

			TreeSet<Friend> current = alphabet.getValue();
			boolean addedFriend = false;
			activity_.friendsOnView.add(Long.valueOf(0));
			for (Friend friend : current) {

				// Filter by search term if searched
				if (searchFilter == null
						|| friend.getFullNameFirstLast().toLowerCase()
								.contains(searchFilter.toString().toLowerCase())) {
					// Put warning sign if has lent items
					if (friend.getLentItems() != null) {
						friendAdapter.add(new FriendItem(friend.getFirstName() + " "
								+ friend.getLastName(), true, R.drawable.android_contact));
						activity_.friendsOnView.add(Long.valueOf(friend.getID()));
					}
					else {
						friendAdapter.add(new FriendItem(friend.getFirstName() + " "
								+ friend.getLastName(), false, R.drawable.android_contact));
						activity_.friendsOnView.add(Long.valueOf(friend.getID()));
					}
					addedFriend = true;
				}
			}
			// Only add section, if has child items
			if (addedFriend) {
				adapter.addSection(alphabet.getKey(), friendAdapter);
				isEmpty = false;
			}
			else {
				activity_.friendsOnView.remove(activity_.friendsOnView.size() - 1);
			}
		}
		setListAdapter(adapter);
	}

	public void lentToSelected(CharSequence searchFilter) {
		activity_.friendsOnView.clear();
		isEmpty = true;

		// Create the friend list with custom adapter
		SeparatedListAdapter adapter = new SeparatedListAdapter(getActivity());
		for (Map.Entry<String, TreeSet<Friend>> alphabet : model_.friends.entrySet()) {

			FriendAdapter friendAdapter = new FriendAdapter(getActivity());

			TreeSet<Friend> current = alphabet.getValue();
			boolean addedFriend = false;
			activity_.friendsOnView.add(Long.valueOf(0));
			for (Friend friend : current) {

				// Only add friends with lent items
				// Filter by search term if searched
				if ((searchFilter == null || friend.getFullNameFirstLast().toLowerCase()
						.contains(searchFilter.toString().toLowerCase()))
						&& friend.getLentItems() != null) {
					friendAdapter.add(new FriendItem(friend.getFirstName() + " "
							+ friend.getLastName(), false, R.drawable.android_contact));
					activity_.friendsOnView.add(Long.valueOf(friend.getID()));
					addedFriend = true;
				}

			}
			if (addedFriend) {
				adapter.addSection(alphabet.getKey(), friendAdapter);
				isEmpty = false;
			}
			else {
				activity_.friendsOnView.remove(activity_.friendsOnView.size() - 1);
			}
		}
		if (!isEmpty) {
			setListAdapter(adapter);
		}
		else {
			ArrayAdapter<String> emptyAdapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1);
			emptyAdapter.add(getResources().getString(R.string.no_lent_to));
			setListAdapter(emptyAdapter);
		}

	}*/
}