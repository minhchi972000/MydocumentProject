package wee.digital.library.util.coutdown

open class SecondCountdownTimer : CoroutineCountdownTimer {

    constructor(intervalMillis: Long = 10000) : super(intervalMillis)

    override fun onTicks(remainMillis: Long) {
        val seconds = remainMillis / 1000
        val s = "%02d:%02d".format(seconds / 60, seconds % 60)
    }
}