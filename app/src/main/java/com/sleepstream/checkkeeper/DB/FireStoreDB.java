package com.sleepstream.checkkeeper.DB;

import android.os.AsyncTask;;
import android.os.Environment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.sleepstream.checkkeeper.R;

import java.io.IOException;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class FireStoreDB  extends AsyncTask<String, Void, Void> {

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param objects The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Void doInBackground(String... objects) {

     /*   try {
            GoogleCredential credential =
                    GoogleCredential.getApplicationDefault();
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setProjectId(objects[0])
                    .build();
            FirebaseApp.initializeApp(options);

            db = FirestoreClient.getFirestore();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        return null;
    }
}
