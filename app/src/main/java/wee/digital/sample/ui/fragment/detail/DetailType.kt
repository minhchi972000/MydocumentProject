package wee.digital.sample.ui.fragment.detail

sealed class DetailType {
    object PostPaidPhone : DetailType()
    object Internet : DetailType()
    object CableTV : DetailType()
    object Water : DetailType()
    object LandlinePhone : DetailType()
    object Electric : DetailType()
}
