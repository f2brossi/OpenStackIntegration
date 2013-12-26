package ch.niceneasy.openstack.android.container;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import ch.niceneasy.openstack.android.R;

import com.woorea.openstack.swift.model.Container;

public class ContainerListAdapter extends BaseAdapter {

	private List<Container> containers = new ArrayList<Container>();

	private Context context;

	public ContainerListAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return containers.size();
	}

	@Override
	public Object getItem(int location) {
		return containers.get(location);
	}

	@Override
	public long getItemId(int location) {
		return containers.get(location).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView textView;
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.list_item_with_folder_image, null);
			textView = ((TextView) convertView.findViewById(R.id.text1));
			textView.setPadding(20, 10, 10, 10);
			textView.setTextAppearance(context, R.style.menuText);
		}
		((TextView) convertView.findViewById(R.id.text1))
				.setText(((Container) getItem(position)).getName());
		return convertView;
	}

	public void setContainers(List<Container> containers) {
		this.containers = containers;
	}

	public List<Container> getContainers() {
		return this.containers;
	}

}