package wee.digital.sample.shared


import wee.digital.sample.pref
import wee.digital.sample.ui.model.User

object Pref {

    var hasShownIntro: Boolean
        get() = pref.bool("hadShownIntro", false)
        set(value) {
            pref.putBool("hadShownIntro", value)
        }

    var user: User?
        get() = pref.obj("currentUser", User::class)
        set(value) {
            pref.putObj("currentUser", value)
        }
}