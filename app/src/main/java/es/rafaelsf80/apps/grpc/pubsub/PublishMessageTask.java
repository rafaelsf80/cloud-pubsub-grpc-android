package es.rafaelsf80.apps.grpc.pubsub;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.protobuf.ByteString;
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

/**
 * Copyright 2016 Rafael Sanchez Fuentes
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Author: Rafael Sanchez Fuentes rafaelsf80 at gmail dot com
 */


public class PublishMessageTask extends AsyncTask<String, Integer, String> {

    ProgressBar pb = null;
    private final String TAG = getClass().getSimpleName();

    StringBuilder sbTopics = new StringBuilder();
    String message;
    private Context mContext;

    public void setContext(Context ctx) {
        mContext = ctx;
    }

    protected void onPreExecute()
    {
        // Show progressDialog
        pb = (ProgressBar) ((Activity) mContext).findViewById(R.id.progress_bar);
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            Log.d(TAG, "Reading credentials file: " + Main.CREDENTIALS_FILE);
            AssetManager am = mContext.getAssets();
            InputStream isCredentialsFile = am.open(Main.CREDENTIALS_FILE);

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
                    .setProject("projects/" + Main.CLOUD_PROJECT)
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

            message = params[0];

            // You need to base64-encode your message
            PublishRequest.Builder publishRequest = PublishRequest.newBuilder()
                    .setTopic( Main.TOPIC );

            PubsubMessage.Builder pubsubMessage = PubsubMessage.newBuilder().
                    setData(ByteString.copyFrom(message.getBytes()));
            //byte[] elementBytes = pubsubMessage.getData().toByteArray();
            //PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setMessageId(message).build();

            publishRequest.addMessages(pubsubMessage);

            PublishResponse publishResponse = publisherStub.publish(publishRequest.build());
            Log.d(TAG, "Message sent (ID="+publishResponse.getMessageIds(0)+"): " + message);
            Log.d(TAG, "Number of messages sent: " + String.valueOf(publishResponse.getMessageIdsCount()));

        } catch (IOException e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return message;
    }

    @Override
    protected void onProgressUpdate(final Integer... progress) {
        // Do nothing
    }

    @Override
    protected void onPostExecute(String msg)
    {
        pb.setVisibility(View.GONE);
        Toast.makeText(mContext, "Message sent: " + msg , Toast.LENGTH_LONG).show();
    }
}
