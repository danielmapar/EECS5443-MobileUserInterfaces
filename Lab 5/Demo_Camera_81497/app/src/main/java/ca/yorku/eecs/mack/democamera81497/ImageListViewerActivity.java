package ca.yorku.eecs.mack.democamera81497;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

@SuppressWarnings("unused")
public class ImageListViewerActivity extends Activity implements AdapterView.OnItemClickListener
{
	final static String MYDEBUG = "MYDEBUG"; // for Log.i messages

	final static String IMAGE_INDEX_KEY = DemoCamera81497Activity.IMAGE_INDEX_KEY;
	final static String DIRECTORY_KEY = DemoCamera81497Activity.DIRECTORY_KEY;
	final static String IMAGE_FILENAMES_KEY = DemoCamera81497Activity.IMAGE_FILENAMES_KEY;
	
	//String[] imageFilenames;
	File directory;
	GridView gridView;
	TextView textView;
	ImageAdapter imageAdapter;
	File[] files;
	String[] filenames;
	
    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gridviewlayout);
    }

	@Override
	protected void onStart() {
		super.onStart();
		init();
	}

	private void init()
    {    	
		// data passed from the calling activity in startActivityForResult (see DemoCameraIntentActivity)
		Bundle b = getIntent().getExtras();
		//imageFilenames = b.getStringArray(IMAGE_FILENAMES_KEY);
		String directoryString = b.getString(DIRECTORY_KEY);

		// get the directory containing some images
		directory = new File(directoryString);
		if (!directory.exists())
		{
			Log.i(MYDEBUG, "No directory: " + directory.toString());
			super.onDestroy(); // cleanup
			this.finish(); // terminate
		}

		// Get a list of files in the directory, sorted by filename. See...
		files = directory.listFiles(new MyFilenameFilter(".jpg"));

		if (files.length == 0) {
			finish();
			return;
		}

		Arrays.sort(files, new Comparator<File>()
		{
			public int compare(File f1, File f2)
			{
				return f1.getName().compareTo(f2.getName());
			}
		});

		// make a String array of the filenames
		filenames = new String[files.length];
		for (int i = 0; i < files.length; ++i) {
			filenames[i] = files[i].getName();
		}

		// get references to the GridView and TextView
		gridView = (GridView)findViewById(R.id.gridview);
		textView = (TextView)findViewById(R.id.textview);

		// display the name of the directory in the text view (minus the full path)
		String[] s = directory.toString().split(File.separator);
		textView.setText(s[s.length - 1]);

		// create an ImageAdapter and give it the array of filenames and the directory
		imageAdapter = new ImageAdapter(this);
		imageAdapter.setFilenames(filenames, directory);

		/*
		 * Determine the display width and height. The column width is calculated so we have three
		 * columns when the screen is in portrait mode. We'll keep the same column width in
		 * landscape mode, but use as many columns as will fit. Including "-12" in the calculation
		 * accommodates 3 pixels of space on the left and right and between each column.
		 */
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int columnWidth = dm.widthPixels < dm.heightPixels ? dm.widthPixels / 3 - 12
				: dm.heightPixels / 3 - 12;
		imageAdapter.setColumnWidth(columnWidth);
		gridView.setColumnWidth(columnWidth);

		// give the ImageAdapter to the GridView (and load the images)
		gridView.setAdapter(imageAdapter);

		// attach a click listener to the GridView (to respond to finger taps)
		gridView.setOnItemClickListener(this);
    }

	/*
	 * If the user taps on an image in the GridView, create an Intent to launch a new activity to
	 * view the image in an ImageView. The image will respond to touch events (e.g., flings), so
	 * we'll bundle up the filenames array and the directory and pass the bundle to the activity.
	 */

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	{
		final Bundle b = new Bundle();
		b.putStringArray("imageFilenames", filenames);
		b.putString("directory", directory.toString());
		b.putInt("position", position);

		// start image viewer activity
		Intent i = new Intent(getApplicationContext(), ImageViewerActivity.class);
		i.putExtras(b);
		startActivity(i);
	}

	// A filter used with the list method (see above) to return only files with a specified
	// extension (e.g., ".jpg")
	class MyFilenameFilter implements FilenameFilter
	{
		String extension;

		MyFilenameFilter(String extensionArg)
		{
			this.extension = extensionArg;
		}

		@SuppressLint("DefaultLocale")
		public boolean accept(File f, String name)
		{
			// add toLowerCase to accept ".jpg" or ".JPG"
			return name.toLowerCase().endsWith(extension);
		}
	}

	@Override
	public void onBackPressed()
	{
		this.setResult(Activity.RESULT_OK);
		super.onBackPressed();
	}
}
