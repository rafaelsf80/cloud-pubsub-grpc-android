package es.rafaelsf80.apps.grpc.pubsub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class Main extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();

    // Constants shared across classes
    public static final String TOPIC ="projects/YOUR_PROJECT_ID/topics/test1";
    public static final String SUBSCRIPTION ="projects/YOUR_PROJECT_ID/subscriptions/test1";
    public static final String CLOUD_PROJECT = "YOUR_PROJECT_ID";
    // assets directory, replace with the json file of your cloud project
    public static final String CREDENTIALS_FILE = "doneval-cloud-d164a2981f94.json";


    TextView tvTitle, tvTopic, tvSubscription;
    Button btPublish, btSubscribe;

   // What happens if I publish to a topic without subscriptions?
   // The publish operation succeeds, but the messages are dropped because there is no subscription interested in them.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTopic = (TextView) findViewById(R.id.tv_topic);
        tvSubscription = (TextView) findViewById(R.id.tv_subscription);
        btPublish = (Button) findViewById(R.id.bt_send);
        btSubscribe = (Button) findViewById(R.id.bt_receive);

        tvTopic.setText( TOPIC );
        tvSubscription.setText( SUBSCRIPTION );

        btPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(Main.this);

                final EditText edittext = new EditText(getApplicationContext());
                alert.setTitle("Message to publish");
                alert.setMessage("To topic: " + TOPIC);
                edittext.setTextColor( Color.BLACK );
                alert.setView(edittext);

                alert.setPositiveButton("Publish", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        PublishMessageTask publishMessageTask = new PublishMessageTask();

                        publishMessageTask.setContext( Main.this );
                        publishMessageTask.execute( edittext.getText().toString() );
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // DO nothing
                    }
                });
                alert.show();
            }
        });

        btSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PullMessageTask pullMessageTask = new PullMessageTask();

                pullMessageTask.setContext( Main.this );
                pullMessageTask.execute( );
            }
        });

//        // Background task for network access
//        new AsyncTask<Void, Void, String>() {
//            protected String doInBackground(Void... params) {
//
//                try {
//                    Log.d(TAG, "Reading credentials file: " + CREDENTIALS_FILE);
//                    AssetManager am = Main.this.getAssets();
//                    InputStream isCredentialsFile = am.open(CREDENTIALS_FILE);
//
//                    ManagedChannelImpl channelImpl = OkHttpChannelBuilder
//                            .forAddress("pubsub.googleapis.com", 443)
//                            .negotiationType(io.grpc.okhttp.NegotiationType.TLS)
//                            .build();
//
//                    Log.d(TAG, "Channel terminated ?: " + channelImpl.isTerminated());
//
//                    GoogleCredentials credential = GoogleCredentials.fromStream(isCredentialsFile);
//                    credential = credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/pubsub"));
//
//                    // Intercept the channel to bind the credential
//                    ExecutorService executor = Executors.newSingleThreadExecutor();
//                    ClientAuthInterceptor interceptor = new ClientAuthInterceptor(credential, executor);
//                    Log.d(TAG, "Interceptor: " + interceptor.toString());
//
//                    Channel channel = ClientInterceptors.intercept(channelImpl, interceptor);
//                    Log.d(TAG, "Channel authority: " + channel.authority());
//
//                    // Create a stub using the channel that has the bound credential
//                    PublisherGrpc.PublisherBlockingStub publisherStub = PublisherGrpc.newBlockingStub(channel);
//
//                    ListTopicsRequest request = ListTopicsRequest.newBuilder()
//                            .setPageSize(10)
//                            .setProject("projects/" + project)
//                            .build();
//                    Log.d(TAG, "Request initialized ?: " + request.isInitialized());
//
//                    ListTopicsResponse resp = publisherStub.listTopics(request);
//                    Log.d(TAG, "Found " + resp.getTopicsCount() + " topics.");
//                    String s = null;
//                    for (Topic topic : resp.getTopicsList()) {
//                        s = topic.getName();
//                        Log.d(TAG, topic.getName());
//                        sbTopics.append( topic.getName() );
//                        sbTopics.append( ", " );
//
//                    }
//
//                    // You need to base64-encode your message
//                    String message = "Hello Cloud Pub/Sub!";
//                    PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setMessageId(message).build();
//
//                    PublishRequest publishRequest = PublishRequest.newBuilder()
//                            .setTopic("projects/decent-envoy-503/topics/test1")
//                            .setMessages(1, pubsubMessage)
//                            .build();
//                    PublishResponse publishResponse = publisherStub.publish(publishRequest);
//                    Log.d(TAG, "Message: " + publishResponse.getMessageIds(0));
//                    Log.d(TAG, "Number of messages: " + String.valueOf(publishResponse.getMessageIdsCount()));
//
//
//                } catch (IOException e) {
//                    Log.d(TAG, "Exception: " + e.toString());
//                }
//                return sbTopics.toString();
//            }
//
//            protected void onPostExecute(String msg) {
//                // Post Code
//                //Log.d(TAG, msg);
//                tvTitle.setText("Topics" + msg);
//            }
//        }.execute();
    }
}

