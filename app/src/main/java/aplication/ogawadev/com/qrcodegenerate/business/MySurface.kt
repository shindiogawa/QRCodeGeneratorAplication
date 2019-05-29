package aplication.ogawadev.com.qrcodegenerate.business

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.view.SurfaceHolder
import android.view.SurfaceView

class MySurface(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    init {
        holder.addCallback(this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val image = BitmapFactory.decodeResource(resources, 0)
        canvas!!.drawBitmap(image, 30f, 30f, Paint())
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas(null)
            synchronized(holder) {
                draw(canvas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }
}