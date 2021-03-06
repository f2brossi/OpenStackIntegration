/*
 * Copyright (c) 2014, daniele.ulrich@gmail.com, http://www.niceneasy.ch. All rights reserved.
 */
package ch.niceneasy.openstack.android.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import ch.niceneasy.openstack.android.R;
import ch.niceneasy.openstack.android.base.OpenstackListActivity;
import ch.niceneasy.openstack.android.base.TaskResult;
import ch.niceneasy.openstack.android.object.ObjectListViewActivity;
import ch.niceneasy.openstack.android.tenant.TenantListViewActivity;

import com.woorea.openstack.swift.Swift;
import com.woorea.openstack.swift.model.Container;

/**
 * The Class ContainerListViewActivity.
 * 
 * @author Daniele
 */
public class ContainerListViewActivity extends OpenstackListActivity {

	/** The camera pic request. */
	final int CAMERA_PIC_REQUEST = 2;

	/** The gallery pic request. */
	final int GALLERY_PIC_REQUEST = 3;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.niceneasy.openstack.android.base.OpenstackListActivity#onCreate(android
	 * .os.Bundle)
	 */
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Button button = (Button) findViewById(R.id.back);
		button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivity(new Intent(ContainerListViewActivity.this,
						TenantListViewActivity.class));
			}
		});
		getListView().setOnItemClickListener(new OnItemClickListener() {

			
			public void onItemClick(AdapterView<?> arg0, View arg1, int item,
					long arg3) {
				Intent showObjects = new Intent(ContainerListViewActivity.this,
						ObjectListViewActivity.class);
				getApplicationState().setSelectedContainer(
						(Container) getListAdapter().getItem(item));
				startActivity(showObjects);
			}
		});
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int item, long arg3) {
				final Container selectedContainer = (Container) getListAdapter()
						.getItem(item);
				AlertDialog alertDialog = new AlertDialog.Builder(
						ContainerListViewActivity.this).create();
				alertDialog.setTitle("delecte top folder");
				alertDialog
						.setMessage("are you sure that you want to delete this folder?");
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
						new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog,
									int which) {
								DeleteContainerTask deleteContainerTask = new DeleteContainerTask(
										selectedContainer);
								deleteContainerTask.execute();
								return;
							}
						});
				alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						"Cancel", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog,
									int which) {
								return;
							}
						});
				alertDialog.show();
				return true;
			}
		});
		setListAdapter(new ContainerListAdapter(this));

		// BEGIN_INCLUDE (inflate_set_custom_view)
		// Inflate a "Done/Cancel" custom action bar view.
		final LayoutInflater inflater = (LayoutInflater) getActionBar()
				.getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		final View customActionBarView = inflater.inflate(
				R.layout.ctionbar_custom_view_settings_cancel, null);
		customActionBarView.findViewById(R.id.menu).setOnClickListener(
				new View.OnClickListener() {
					
					public void onClick(View v) {
						PopupMenu menu = new PopupMenu(
								ContainerListViewActivity.this, v);
						menu.getMenuInflater().inflate(R.menu.homemenu,
								menu.getMenu());
						menu.show();
					}
				});
		// customActionBarView.findViewById(R.id.actionbar_cancel)
		// .setOnClickListener(new View.OnClickListener() {
		// 
		// public void onClick(View v) {
		// }
		// });

		// Show the custom action bar view and hide the normal Home icon and
		// title.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
						| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(customActionBarView,
				new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
		// END_INCLUDE (inflate_set_custom_view)

		GetContainerTask getContainerTask = new GetContainerTask();
		getContainerTask.execute();
	}

	/**
	 * The Class GetContainerTask.
	 */
	private class GetContainerTask extends
			AsyncTask<String, Object, TaskResult<List<Container>>> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		
		protected TaskResult<List<Container>> doInBackground(String... params) {
			try {
				Swift swift = getService().getSwift(
						getApplicationState().getSelectedTenant().getId());
				List<Container> tempList = swift.containers().list().execute()
						.getList();
				Comparator<Container> comparator = new Comparator<Container>() {

					
					public int compare(Container lhs, Container rhs) {
						return lhs.getName().compareTo(rhs.getName());
					}

				};
				Collections.sort(tempList, comparator);
				return new TaskResult<List<Container>>(tempList);
			} catch (Exception e) {
				return new TaskResult<List<Container>>(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		
		protected void onPostExecute(TaskResult<List<Container>> result) {
			super.onPostExecute(result);
			progressBar.setVisibility(View.GONE);
			if (result.isValid()) {
				((ContainerListAdapter) getListAdapter()).setContainers(result
						.getResult());
			} else {
				showErrorDialog(R.string.error_dlg, result.getException(),
						new Intent(ContainerListViewActivity.this,
								TenantListViewActivity.class));
			}
		}
	}

	/**
	 * Creates the container.
	 * 
	 * @param name
	 *            the name
	 */
	public void createContainer(String name) {
		progressBar.setVisibility(View.VISIBLE);
		CreateContainerTask createContainerTask = new CreateContainerTask(name);
		createContainerTask.execute();
	}

	/**
	 * The Class CreateContainerTask.
	 */
	private class CreateContainerTask extends
			AsyncTask<String, Object, TaskResult<List<Container>>> {

		/** The container name. */
		private String containerName;

		/**
		 * Instantiates a new creates the container task.
		 * 
		 * @param containerName
		 *            the container name
		 */
		private CreateContainerTask(String containerName) {
			this.containerName = containerName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		
		protected TaskResult<List<Container>> doInBackground(String... params) {
			try {
				Swift swift = getService().getSwift(
						getApplicationState().getSelectedTenant().getId());
				swift.containers().create(containerName).execute();
				Container container = new Container();
				container.setName(containerName);
				List<Container> tempList = new ArrayList<Container>(
						((ContainerListAdapter) getListAdapter())
								.getContainers());
				tempList.add(container);
				Comparator<Container> comparator = new Comparator<Container>() {

					
					public int compare(Container lhs, Container rhs) {
						return lhs.getName().compareTo(rhs.getName());
					}

				};
				Collections.sort(tempList, comparator);
				return new TaskResult<List<Container>>(tempList);
			} catch (Exception e) {
				return new TaskResult<List<Container>>(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		
		protected void onPostExecute(TaskResult<List<Container>> result) {
			super.onPostExecute(result);
			progressBar.setVisibility(View.GONE);
			if (result.isValid()) {
				((ContainerListAdapter) getListAdapter()).setContainers(result
						.getResult());
				setListAdapter(getListAdapter());
			} else {
				showErrorDialog(R.string.error_dlg, result.getException(),
						new Intent(ContainerListViewActivity.this,
								TenantListViewActivity.class));
			}
		}
	}

	/**
	 * The Class DeleteContainerTask.
	 */
	private class DeleteContainerTask extends
			AsyncTask<String, Object, TaskResult<List<Container>>> {

		/** The container. */
		private Container container;

		/**
		 * Instantiates a new delete container task.
		 * 
		 * @param container
		 *            the container
		 */
		private DeleteContainerTask(Container container) {
			this.container = container;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		
		protected TaskResult<List<Container>> doInBackground(String... params) {
			try {
				Swift swift = getService().getSwift(
						getApplicationState().getSelectedTenant().getId());
				swift.containers().delete(container.getName()).execute();
				List<Container> tempList = new ArrayList<Container>(
						((ContainerListAdapter) getListAdapter())
								.getContainers());
				tempList.remove(container);
				Comparator<Container> comparator = new Comparator<Container>() {

					
					public int compare(Container lhs, Container rhs) {
						return lhs.getName().compareTo(rhs.getName());
					}

				};
				Collections.sort(tempList, comparator);
				return new TaskResult<List<Container>>(tempList);
			} catch (Exception e) {
				return new TaskResult<List<Container>>(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		
		protected void onPostExecute(TaskResult<List<Container>> result) {
			super.onPostExecute(result);
			progressBar.setVisibility(View.GONE);
			if (result.isValid()) {
				((ContainerListAdapter) getListAdapter()).setContainers(result
						.getResult());
				setListAdapter(getListAdapter());
			} else {
				showErrorDialog(R.string.error_dlg, result.getException(),
						new Intent(ContainerListViewActivity.this,
								TenantListViewActivity.class));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.niceneasy.openstack.android.base.OpenstackListActivity#setContentView
	 * ()
	 */
	
	protected void setContentView() {
		setContentView(R.layout.list_containers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.addcontainer, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add:
			ContainerNamePromptDialog dlg = new ContainerNamePromptDialog();
			dlg.show(getFragmentManager(), "Container Name Prompter");
			return true;
		case R.id.camera:
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
			return true;
		case R.id.gallery:
			Intent photoPickerIntent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, GALLERY_PIC_REQUEST);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	// private void promptContainerName() {
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	// LayoutInflater inflater = this.getLayoutInflater();
	// builder.setView(inflater.inflate(R.layout.prompt_container_name, null))
	// .setPositiveButton(R.string.done,
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int id) {
	// //containerName = dialog.
	// }
	// })
	// .setNegativeButton(R.string.cancel,
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int id) {
	// dialog.cancel();
	// }
	// });
	// builder.create().show();
	// }

}
