package wee.digital.sample.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import wee.digital.library.extension.isEmpty
import wee.digital.library.extension.put

fun DocumentSnapshot.documentToJsObject(): JsonObject? {
    val obj = JsonObject()
    this.data?.forEach { (key: String, value: Any) ->
        when (value) {
            is String -> obj.put(key, value)
            is Long -> obj.put(key, value)
            is Number -> obj.put(key, value)
            is Boolean -> obj.put(key, value)
            is Timestamp -> obj.put(key, value.seconds)
            is ArrayList<*> -> obj.put(key, value.arrayToJsArray())
            is HashMap<*, *> -> obj.put(key, value.mapToJsObject())

        }
    }
    return obj
}

fun Map<*, *>?.mapToJsObject(): JsonObject? {
    val obj = JsonObject()
    this?.entries?.forEach {
        val key = it.key?.toString() ?: return null
        when (it.value) {
            is String -> obj.put(key, it.value?.toString())
            is Long -> obj.put(key, it.value as Long)
            is Number -> obj.put(key, it.value as Double)
            is Boolean -> obj.put(key, it.value as Boolean)
            is Timestamp -> obj.put(key, (it.value as Timestamp).seconds)
            is ArrayList<*> -> obj.put(key, (it.value as ArrayList<*>).arrayToJsArray())
            is HashMap<*, *> -> obj.put(key, (it.value as Map<*, *>).mapToJsObject())
        }
    }
    return obj
}

fun ArrayList<*>?.arrayToJsArray(): JsonArray? {
    val array = JsonArray()
    this?.forEach {
        when (it) {
            is String -> array.add(it)
            is Long -> array.add(it)
            is Number -> array.add(it)
            is Boolean -> array.add(it)
            is Timestamp -> array.add(it.seconds)
            is ArrayList<*> -> array.add(it.arrayToJsArray())
            is HashMap<*, *> -> array.add(it.mapToJsObject())
        }
    }
    if (array.isEmpty()) return null
    return array
}


