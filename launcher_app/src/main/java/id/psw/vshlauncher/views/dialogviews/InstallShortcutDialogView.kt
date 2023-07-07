package id.psw.vshlauncher.views.dialogviews

import android.content.Intent
import android.graphics.*
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.XMBShortcutInfo
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.drawBitmap
import id.psw.vshlauncher.views.drawText
import kotlin.random.Random

class InstallShortcutDialogView(private val vsh: VSH, private val intent: Intent) : XmbDialogSubview(vsh) {
    companion object {
        private val rng = Random(System.nanoTime())
    }

    override val title: String
        get() = vsh.getString(R.string.install_shortcut_dialog_title)

    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean = true
    override val negativeButton: String = vsh.getString(android.R.string.cancel)
    override val positiveButton: String = vsh.getString(R.string.common_install)

    override val icon: Bitmap = ResourcesCompat.getDrawable(vsh.resources, R.drawable.category_shortcut, null)?.toBitmap(50,50) ?: XMBItem.WHITE_BITMAP

    private val shortcut = XMBShortcutInfo(vsh, intent)

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = FontCollections.masterFont
        textSize = 25.0f
        color = Color.WHITE
    }


    override fun onClose() {
        if(icon != XMBItem.WHITE_BITMAP){
            icon.recycle()
        }
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            val req = shortcut.pinItem
            if(req != null){
                val id = rng.nextBytes(16).toHex()
                shortcut.cxl_id = id

                val files = FileQuery(VshBaseDirs.USER_DIR)
                    .atPath("shortcuts")
                    .withNames(id)
                    .withExtensionArray(VshResTypes.INI)
                    .createParentDirectory(true)
                    .execute(vsh)
                var file = files.find { it.exists() }
                if(file == null){
                    for(ffile in files){
                        try {
                            if(ffile.createNewFile()){
                                file = ffile
                                break
                            }
                        }catch(e:Exception){
                            e.printStackTrace()
                        }
                    }
                }

                if(file != null) {
                    shortcut.write(file)
                    shortcut.saveIcon(file)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    req.accept()
                }
                vsh.reloadShortcutList()
            }
        }
        finish(VshViewPage.MainMenu)
    }

    private val tmpRectF = RectF()
    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime:Float) {
        val bTop = drawBound.top + 100.0f
        tmpRectF.set(
            drawBound.centerX() - 50.0f,
            bTop + 25.0f,
            drawBound.centerX() + 50.0f,
            bTop + 125.0f
        )
        ctx.drawBitmap(shortcut.icon, null, tmpRectF, null, FittingMode.FIT, 0.5f, 0.5f)
        var textTop = bTop + 150.0f
        val textCenter = 0.40f.toLerp(drawBound.left, drawBound.right)

        arrayOf(
            "ID" to shortcut.id,
            "Name" to shortcut.name,
            "Long Name" to shortcut.longName,
            "Package" to shortcut.packageName,
        ).forEach{
           if(it.second.isNotEmpty()){
               textPaint.textAlign = Paint.Align.RIGHT
                ctx.drawText(it.first, textCenter - 10.0f, textTop, textPaint, 0.5f)
               textPaint.textAlign = Paint.Align.LEFT
               ctx.drawText(it.second, textCenter + 10.0f, textTop, textPaint, 0.5f)

               textTop += textPaint.textSize
           }
        }
        textPaint.textAlign = Paint.Align.CENTER
        ctx.drawText("Install this shortcut to Cross Launcher?",
            drawBound.centerX(),
            textTop + textPaint.textSize,
            textPaint,
            0.5f)
    }
}