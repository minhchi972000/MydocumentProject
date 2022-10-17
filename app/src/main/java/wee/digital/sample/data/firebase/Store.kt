package wee.digital.sample.data.firebase

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

val store get() = Firebase.firestore

val storage get() = Firebase.storage.reference

val users get() = store.collection("users")

val selfUserRef: DocumentReference get() = users.document(auth.uid!!)

fun userReference(uid: String): DocumentReference {
    return users.document(uid)
}
