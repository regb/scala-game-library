//import scala.scalajs.js
//import scala.scalajs.js.annotation._
//import scala.scalajs.js.|
//
//package sgl.html5 {
//package firebase {
//
//  package app {
//
//    @js.native
//    trait App extends js.Object {
//      def analytics(): firebase.analytics.Analytics = js.native
//    }
//
//  }
//
//  package analytics {
//
//    @js.native
//    trait Analytics extends js.Object {
//      var app: firebase.app.App = js.native
//
//      def logEvent(eventName: String, eventParams: Object*): Unit = js.native
//    }
//
//  }
//
//  @JSName("firebase")
//  @js.native
//  trait Firebase extends js.Object {
//    var SDK_VERSION: String = js.native
//    def app(name: String = ???): firebase.app.App = js.native
//    var apps: js.Array[firebase.app.App | Null] = js.native
//    def analytics(app: firebase.app.App = ???): firebase.analytics.Analytics = js.native
//    def initializeApp(options: FirebaseConfig, name: String = ???): firebase.app.App = js.native
//  }
//
//  @JSExportAll
//  case class FirebaseConfig(
//    apiKey: String,
//    authDomain: String,
//    databaseURL: String,
//    storageBucket: String,
//    messagingSenderId: String
//  )
//  
//}
//}
