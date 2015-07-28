package co.adrianblan.cheddar;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Adrian on 2015-07-28.
 */
public class FeedFragment extends Fragment {

    private FeedAdapter feedAdapter;
    private ArrayList<Long> submissionIDs;
    private Firebase baseUrl;
    private Firebase storiesUrl;
    private ArrayList<AsyncTask> asyncTasks = new ArrayList<AsyncTask>();

    public static FeedFragment newInstance() {
        FeedFragment f = new FeedFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        setRetainInstance(true);
        initImageLoader();

        // Init API stuff
        Firebase.setAndroidContext(getActivity());
        baseUrl = new Firebase("https://hacker-news.firebaseio.com/v0/");
        storiesUrl = baseUrl.child(getArguments().getString("url"));

        feedAdapter = new FeedAdapter();
        // Gets all the submissions and populates the list with them
        updateSubmissions();
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, container,false);

        ListView listView = (ListView) rootView.findViewById(R.id.feed_list);
        listView.setAdapter(feedAdapter);

        return rootView;
    }

    // Sometimes we just might want to reset all submissions
    public void resetSubmissions(){

        //We dont want to be able to spam resets
        if(!feedAdapter.isEmpty()) {

            //First we need to cancel all asynctasks
            while(!asyncTasks.isEmpty()){

                if(asyncTasks.get(0).getStatus().equals(AsyncTask.Status.RUNNING)){
                    asyncTasks.get(0).cancel(true);
                }

                asyncTasks.remove(0);
            }

            // We also cancel all image fetching
            ImageLoader.getInstance().stop();

            submissionIDs = null;
            feedAdapter.clear();
            feedAdapter.notifyDataSetChanged();
            updateSubmissions();
        }
    }

    // Fetches a large number of submissions, and updates them individually
    public void updateSubmissions(){

        // If we don't have submissions loaded, we must first load them
        if(submissionIDs == null) {

            // Updates the list of 500 submission IDs
            storiesUrl.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    submissionIDs = (ArrayList<Long>) snapshot.getValue();

                    // Because we are doing this asynchronously, it's easier to update submissions directly
                    updateSubmissions();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    System.err.println("Could not retrieve posts! " + firebaseError);
                }
            });
        } else {

            // From the top 500 submissions, we only load a few at a time
            for (int i = feedAdapter.getCount(); i < feedAdapter.getCount() + 25 && i < submissionIDs.size(); i++) {

                // But we must first add each submission to the view manually
                updateSingleSubmission(baseUrl.child("/item/" + submissionIDs.get(i)));
            }
        }
    }

    // Gets an url to a single submission and updates it in the feedadapter
    public void updateSingleSubmission(Firebase submission){

        submission.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // We retrieve all objects into a hashmap
                Map<String, Object> ret = (Map<String, Object>) snapshot.getValue();

                if(ret == null){
                    return;
                }

                String url = (String) ret.get("url");
                if(url == ""){url = "http://hackernews.com";}

                URL site = null;
                try {
                    site = new URL(url);
                } catch (MalformedURLException e) {
                    System.err.println(e);
                    return;
                }

                FeedItem f = initNewFeedItem(ret, site);
                feedAdapter.add(f);
                feedAdapter.notifyDataSetChanged();

                if(!url.contains("hackernews.com")) {
                    // Asynchronously updates images for the feed item
                    updateSubmissionThumbnail(site.getHost(), f);
                    updateSubmissionFavicon(site.getHost(), f);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.err.println("Could not retrieve post! " + firebaseError);
            }
        });
    }

    // Takes the raw API data and the URL, returns a new feed item
    public FeedItem initNewFeedItem(Map<String, Object> ret, URL site){

        FeedItem f = new FeedItem();

        // Gets readable date
        Date past = new Date((Long) ret.get("time") * 1000);
        Date now = new Date();
        String time = getPrettyDate(past, now);

        // Set titles and other data
        f.setTitle((String) ret.get("title"));
        f.setSubtitle2(Long.toString((Long) ret.get("score")) + " points \u2022 0 comments \u2022 " + time);

        String domain = site.getHost().replace("www.", "");
        f.setSubtitle1(domain);

        // We show the first letter of the url on the thumbnail
        if(domain.equals("hackernews.com")){
            f.setLetter("HN");
        } else {
            f.setLetter(domain.substring(0, 1));
        }

        return f;
    }

    // Recieves a host url, and the position of the feed item
    // Fetches a remote server for the url to the best thumbnail to use
    public void updateSubmissionThumbnail(String url, FeedItem f){

        final FeedItem fi = f;

        class ThumbnailUrlTask extends AsyncTask<String, Integer, String> {

            @Override
            protected String doInBackground(String... url) {
                // Gets url to a good thumbnail from a site
                ThumbnailExtractor ts = new ThumbnailExtractor();
                return ts.getThumbnailUrl(url[0]);
            }
            @Override
            protected void onProgressUpdate(Integer... i) {
            }

            @Override
            protected void onPostExecute(String thumbnailUrl) {

                // Loads high resolution icon
                ImageLoader imageLoader = ImageLoader.getInstance();

                imageLoader.loadImage(thumbnailUrl, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String url, View view, Bitmap loadedImage) {

                        if(loadedImage == null){
                            return;
                        }

                        // It's possible the list has changed during the async task
                        // So we make sure the item still exists
                        int position = feedAdapter.getPosition(fi);
                        if(position == -1){
                            return;
                        }

                        feedAdapter.getItem(position).setThumbnail(loadedImage);
                        feedAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        // Asynchronously fetches the URL to the thumbnail
        // We cannot use execute() since it only allows one thread at a time
        ThumbnailUrlTask task = new ThumbnailUrlTask();
        asyncTasks.add(task);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "http://icons.better-idea.org/api/icons?url=" + url);
    }

    // Recieves a host url, and the position of the feed item
    // Updates a low resolution thumbnail for that submission
    public void updateSubmissionFavicon(String url, FeedItem f){

        final FeedItem fi = f;
        ImageLoader imageLoader = ImageLoader.getInstance();

        // Loads low resolution favicon
        imageLoader.loadImage("http://www.google.com/s2/favicons?domain=" + url, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String url, View view, Bitmap loadedImage) {

                //We don't want to show the default image
                if (hashBitmap(loadedImage) == 56355950541L) {
                    return;
                }

                int position = feedAdapter.getPosition(fi);

                if (position == -1) {
                    return;
                }

                feedAdapter.getItem(position).setFavicon(loadedImage);
                feedAdapter.notifyDataSetChanged();
            }
        });
    }

    // Converts the difference between two dates into a pretty date
    // There's probably a joke in there somewhere
    public String getPrettyDate(Date past, Date now){

        if(TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime()) > 0){
            return TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime()) + " days";
        } else if(TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime()) > 0){
            return TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime()) + " hrs";
        } if(TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime()) > 0){
            return TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime()) + " mins";
        } else {
            return TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime()) + " sec";
        }
    }

    //This is the shittiest hash ever, but we only need one image so
    public long hashBitmap(Bitmap bmp){

        long hash = 0;
        for(int x = 0; x < bmp.getWidth(); x++){
            for (int y = 0; y < bmp.getHeight(); y++){
                hash += Math.abs(bmp.getPixel(x,y));
            }
        }
        return hash;
    }

    // Initializes the image loader with what is probably reasonable values
    public void initImageLoader(){

        // Returns if we have already initialized the ImageLoader
        if(ImageLoader.getInstance().isInited()){
            return;
        }

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(getActivity());
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(5 * 1024 * 1024); // 5 MiB
        config.tasksProcessingOrder(QueueProcessingType.FIFO);

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.refresh) {
            resetSubmissions();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}