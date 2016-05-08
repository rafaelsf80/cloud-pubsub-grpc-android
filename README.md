# Cloud PubSub (gRPC) with Android #

Following the [gRPC announcement for Cloud PubSub](https://cloud.google.com/blog/big-data/2016/03/announcing-grpc-alpha-for-google-cloud-pubsub), this sample project executes the [Java sample app](https://cloud.google.com/pubsub/grpc-java) on an Android Nexus 6 device


## Setup

1) Android Studio

2) [Gradle 2.12](http://gradle.org/gradle-download/)

3) Android plugin for gradle

4) [Protobuf plugin for gradle 0.7.6](https://github.com/google/protobuf-gradle-plugin/tree/v0.7.6)

Important to have Gradle and the protobuf plugin exactly in the versions above (2.12 and 0.7.6 respectively).
protoc must be in version 3.

## PubSub gRPC dependencies

The following dependency needs to be added to build.gradle:

```groovy  
compile 'com.google.api.grpc:grpc-pubsub-v1:0.0.2'
```


## Authentication

I use a service account of my cloud project. Replace with yours by downloading the corresponding json file
into assets/ file. You can use alternative authentication mechanisms if desired.

Remember to set prereqs and [enable PubSub API](https://cloud.google.com/pubsub/prereqs) on your Google Cloud project.


## Java protobuf nano implementation with Pubsub

By default, the protobuf plugin for gradle always uses javanano implementation in Android.
Pubsub.proto uses proto3 built-in types (empty.proto and annotations.proto) which are not included
on the javanano implementation. This is properly handled by the plugin, however, it causes some duplicated dependencies which needed to be resolved by excluding the duplications on the **packagingOptions** block on **build.gradle**

## Screenshots

Main activity: (pending)
