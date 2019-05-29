package aplication.ogawadev.com.qrcodegenerate.business


import android.content.ContentValues
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent


class MyGestureDetector : GestureDetector.OnGestureListener {
    override fun onShowPress(e: MotionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDown(e: MotionEvent?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        if (e1!!.getX()  < e2!!.getX()) {
            print("Left to Right swipe performed")
        }

        if (e1.getX() > e2.getX()) {
            Log.d(ContentValues.TAG, "Right to Left swipe performed");
        }

        if (e1.getY() < e2.getY()) {
            Log.d(ContentValues.TAG, "Up to Down swipe performed");
        }

        if (e1.getY() > e2.getY()) {
            Log.d(ContentValues.TAG, "Down to Up swipe performed");
        }

        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLongPress(e: MotionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}