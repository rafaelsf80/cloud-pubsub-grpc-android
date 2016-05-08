package es.rafaelsf80.apps.grpc.pubsub;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.pubsub.v1.ListTopicsRequest;
import com.google.pubsub.v1.ListTopicsResponse;
import com.google.pubsub.v1.PublishRequest;
import com.google.pubsub.v1.PublishResponse;
import com.google.pubsub.v1.PublisherGrpc;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Topic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.auth.ClientAuthInterceptor;
import io.grpc.internal.ManagedChannelImpl;
import io.grpc.okhttp.OkHttpChannelBuilder;


public class Main extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();

    private final String project = "decent-envoy-503";
    // assets directory, replace with the json file of your cloud project
    private final String CREDENTIALS_FILE = "doneval-cloud-d164a2981f94.json";

    TextView tvTopics;
    StringBuilder sbTopics = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tvTopics = (TextView) findViewById(R.id.tv_topics);

        // Background task for network access
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {

                try {
                    Log.d(TAG, "Reading credentials file: " + CREDENTIALS_FILE);
                    AssetManager am = Main.this.getAssets();
                    InputStream isCredentialsFile = am.open(CREDENTIALS_FILE);

                    ManagedChannelImpl channelImpl = OkHttpChannelBuilder
                            .forAddress("pubsub.googleapis.com", 443)
                            .negotiationType(io.grpc.okhttp.NegotiationType.TLS)
                            .build();

                    Log.d(TAG, "Channel terminated ?: " + channelImpl.isTerminated());

                    GoogleCredentials credential = GoogleCredentials.fromStream(isCredentialsFile);
                    credential = credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/pubsub"));

                    // Intercept the channel to bind the credential
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    ClientAuthInterceptor interceptor = new ClientAuthInterceptor(credential, executor);
                    Log.d(TAG, "Interceptor: " + interceptor.toString());

                    Channel channel = ClientInterceptors.intercept(channelImpl, interceptor);
                    Log.d(TAG, "Channel authority: " + channel.authority());

                    // Create a stub using the channel that has the bound credential
                    PublisherGrpc.PublisherBlockingStub publisherStub = PublisherGrpc.newBlockingStub(channel);

                    ListTopicsRequest request = ListTopicsRequest.newBuilder()
                            .setPageSize(10)
                            .setProject("projects/" + project)
                            .build();
                    Log.d(TAG, "Request initialized ?: " + request.isInitialized());

                    ListTopicsResponse resp = publisherStub.listTopics(request);
                    Log.d(TAG, "Found " + resp.getTopicsCount() + " topics.");
                    String s = null;
                    for (Topic topic : resp.getTopicsList()) {
                        s = topic.getName();
                        Log.d(TAG, topic.getName());
                        sbTopics.append( topic.getName() );
                        sbTopics.append( ", " );

                    }

                    // You need to base64-encode your message
                    String message = "Hello Cloud Pub/Sub!";
                    PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setMessageId(message).build();

                    PublishRequest publishRequest = PublishRequest.newBuilder()
                            .setTopic("projects/decent-envoy-503/topics/test1")
                            .setMessages(1, pubsubMessage)
                            .build();
                    PublishResponse publishResponse = publisherStub.publish(publishRequest);
                    Log.d(TAG, "Message: " + publishResponse.getMessageIds(0));
                    Log.d(TAG, "Number of messages: " + String.valueOf(publishResponse.getMessageIdsCount()));


                } catch (IOException e) {
                    Log.d(TAG, "Exception: " + e.toString());
                }
                return sbTopics.toString();
            }

            protected void onPostExecute(String msg) {
                // Post Code
                //Log.d(TAG, msg);
                tvTopics.setText("Topics" + msg);
            }
        }.execute();
    }
}

