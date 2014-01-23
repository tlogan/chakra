import android.Keys._

android.Plugin.androidBuild

name := "chakra"

version := "1.0"
 
scalaVersion := "2.10.2"

 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.3"

libraryDependencies += "org.scaloid" %% "scaloid" % "3.0-8"

useProguard in Android := true 

proguardOptions in Android ++= Seq(
  "-dontobfuscate",
  "-dontoptimize",
  "-dontwarn sun.misc.Unsafe",
  "-keep class akka.actor.ActorSystem",
  "-keep class akka.actor.LightArrayRevolverScheduler",
  "-keep class com.typesafe.config.Config",
  "-dontwarn scala.**",
  """-keepclasseswithmembers public class * {
      public static void main(java.lang.String[]);
  }""",
  "-keep class * implements org.xml.sax.EntityResolver",
  """-keepclassmembers class * {
      ** MODULE$;
  }""",
  """-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinPool {
      long eventCount;
      int  workerCounts;
      int  runControl;
      scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode syncStack;
      scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode spareStack;
  }""",
  """-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinWorkerThread {
      int base;
      int sp;
      int runState;
  }""",
  """-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinTask {
      int status;
  }""",
  """-keepclassmembernames class scala.concurrent.forkjoin.LinkedTransferQueue {
      scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference head;
      scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference tail;
      scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference cleanMe;
  }""",
  """-keep class akka.** {
    public <methods>;
  }"""
)
