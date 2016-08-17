# Cloud PubSub (gRPC) with Android #

Following the [gRPC announcement for Cloud PubSub](https://cloud.google.com/blog/big-data/2016/03/announcing-grpc-alpha-for-google-cloud-pubsub), 
this Android sample publishes into a topic, and pulls messages fom a subscription.
It takes some guidelines from [here](https://cloud.google.com/pubsub/grpc-java).
The code can be executed on an Android device.


## Setup

1) Android Studio

2) [Gradle 2.12](http://gradle.org/gradle-download/)

3) Android plugin for gradle

4) [Protobuf plugin for gradle 0.7.7](https://github.com/google/protobuf-gradle-plugin/tree/v0.7.7)

Important to have Gradle and the protobuf plugin exactly in the versions above (2.12 and 0.7.7 respectively).
protoc must be in version 3.
7
## PubSub gRPC dependencies

The following dependency needs to be added to build.gradle:

```groovy  
compile 'com.google.api.grpc:grpc-pubsub-v1:0.0.2'
```


## Authentication

Authentications using gloud-java libraries are described [here](https://github.com/GoogleCloudPlatform/gcloud-java). Remember to set prereqs and [enable PubSub API](https://cloud.google.com/pubsub/prereqs) on your Google Cloud project.


Basically you have **two authentication options** from Android:

- **Use credentials of a service account**: This requires to generate a JSON file from the console and add it to your apk.
This has some security concerns, since anyone unpacking your apk would have access to the private key of your service account.
If you still would like to proceed, you should place your the corresponding JSON file
into **assets/** directory and use the following code to get credentials.

   
```    

    AssetManager am = mContext.getAssets();
    InputStream isCredentialsFile = am.open( YOUR_JSON_FILE_INSIDE_ASSETS_DIRECTORY );
    
    GoogleCredentials credential = GoogleCredentials.fromStream(isCredentialsFile);
    credential = credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/pubsub"));
```


- **Use OAuth2**, specifying the scopes as described [here](https://developers.google.com/android/guides/http-auth#specifying_scopes). You get a token without compromising security by saving any key inside the apk. The token should be generated as follows:
   
```    

    String scopesString = "https://www.googleapis.com/auth/pubsub";
    String SCOPE = "oauth2:" + scopesString;
    
    token = GoogleAuthUtil.getToken(
            mContext,     // Context of your Main activity
            mAccount,     // Account name with permissions to PubSub and your cloud proyect
            SCOPE         // String scope
    );
    
    GoogleCredentials credential =  new GoogleCredentials( new AccessToken( token, null) );
    credential = credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/pubsub"));
```
   

## Java protobuf nano implementation with Pubsub

By default, the protobuf plugin for gradle always uses javanano implementation in Android.
Pubsub.proto uses proto3 built-in types (empty.proto and annotations.proto) which are not included
on the javanano implementation. This is properly handled by the plugin, however, it causes some duplicated dependencies which needed to be resolved by excluding the duplications on the **packagingOptions** block on **build.gradle**

## Screenshots

Main activity:

<img src="https://raw.githubusercontent.com/rafaelsf80/cloud-pubsub-grpc-android/master/app/screenshots/main1.png" alt="alt text" width="200" height="300">
<img src="https://raw.githubusercontent.com/rafaelsf80/cloud-pubsub-grpc-android/master/app/screenshots/main2.png" alt="alt text" width="200" height="300">

