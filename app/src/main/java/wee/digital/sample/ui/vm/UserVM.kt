package wee.digital.sample.ui.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wee.digital.sample.data.firebase.auth
import wee.digital.sample.data.firebase.selfUserRef
import wee.digital.sample.ui.base.BaseVM

class UserVM : BaseVM() {

    val firebaseUserLiveData = MutableLiveData<FirebaseUser?>()

    fun onFirebaseAppInit() {
        auth.addAuthStateListener {
            userRegistration?.remove()
            val user = it.currentUser
            firebaseUserLiveData.value = user
            if (null != user) {
                syncUser()
            } else {
                userRegistration?.remove()
            }
        }
        auth.addIdTokenListener(FirebaseAuth.IdTokenListener {
        })
    }

    private var userRegistration: ListenerRegistration? = null

    fun syncUser() {
        userRegistration = selfUserRef
            .addSnapshotListener { snapshot, _ ->
                snapshot?.data?.also {
                    syncUser(it)
                }
            }
    }

    private fun syncUser(map: Map<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }


}