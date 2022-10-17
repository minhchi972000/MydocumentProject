package wee.digital.sample.ui.fragment.motion

import androidx.constraintlayout.motion.widget.MotionLayout
import wee.digital.library.extension.toast
import wee.digital.sample.R
import wee.digital.sample.databinding.MotionBinding
import wee.digital.sample.ui.base.Inflating
import wee.digital.sample.ui.main.MainFragment

class MotionFragment : MainFragment<MotionBinding>() {

    override fun inflating(): Inflating = MotionBinding::inflate
    override fun onViewCreated() {

        vb.motionLayout.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {

            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {

            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
             toast("Success")
                mainNavigate(R.id.billPhoneFragment)
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {

            }
        })
    }
}