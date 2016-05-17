package com.example.tanutk.jsonparsingdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tanutk.jsonparsingdemo.models.HSportModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private ListView lvHSport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config); // Do it on Application start


        lvHSport = (ListView) findViewById(R.id.lvHSport);

        Button btnHit = (Button) findViewById(R.id.hitBtn);



        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONTask().execute("http://sport.truelife.com/wrap_api2/news/getlist?method=getlist&offset=0&limit=20&format=json");
            }
        });


    }

    public class JSONTask extends AsyncTask<String, String, List<HSportModel>> {


        @Override
        protected List<HSportModel> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();
                JSONObject parentObject1 = new JSONObject(finalJson);
                JSONObject parentObject2 = parentObject1.getJSONObject("response");
                JSONObject parentObject3 = parentObject2.getJSONObject("data");
                JSONObject parentObject4 = parentObject3.getJSONObject("items");

                JSONArray parentArray = parentObject4.getJSONArray("item");

                List<HSportModel> hSportModelList = new ArrayList<>();

                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject fianlObject = parentArray.getJSONObject(i);

                    HSportModel hSportModel = new HSportModel();
                    hSportModel.setThumbnail(fianlObject.getString("thumbnail"));
                    hSportModel.setTitle(fianlObject.getString("title"));
                    hSportModel.setDescription(fianlObject.getString("description"));
                    hSportModel.setId(fianlObject.getString("id"));
                    hSportModel.setShare_url(fianlObject.getString("share_url"));
                    //Adding the final object to the list.
                    hSportModelList.add(hSportModel);
                }


                return hSportModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
                try {
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<HSportModel> result) {
            super.onPostExecute(result);
            //TODO need to set data to the list
            HSportAdapter adapter = new HSportAdapter(getApplicationContext(), R.layout.row, result);
            lvHSport.setAdapter(adapter);
        }



    }

    public class HSportAdapter extends ArrayAdapter {

        private List<HSportModel> hSportModelList;
        private int resource;
        private LayoutInflater inflater;

        public HSportAdapter(Context context, int resource, List<HSportModel> objects) {
            super(context, resource, objects);
            hSportModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(resource, null);

            }
            ImageView ivHSport;
            TextView tvTitleHSport;

            ivHSport = (ImageView) convertView.findViewById(R.id.ivHSport);
            tvTitleHSport = (TextView) convertView.findViewById(R.id.tvTitleHSport);

            // Then later, when you want to display image
            ImageLoader.getInstance().displayImage(hSportModelList.get(position).getThumbnail(), ivHSport); // Default options will be used

            //ivHSport.setImageBitmap(getBitmapFromURL(hSportModelList.get(position).getThumbnail()));
            tvTitleHSport.setText(hSportModelList.get(position).getTitle());
            return convertView;

        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }

}
