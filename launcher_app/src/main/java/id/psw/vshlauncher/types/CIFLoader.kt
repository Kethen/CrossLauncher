package id.psw.vshlauncher.types

import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshResTypes
import id.psw.vshlauncher.delayedExistenceCheck
import id.psw.vshlauncher.getOrMake
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.submodules.BitmapRef
import id.psw.vshlauncher.types.sequentialimages.XMBAnimAPNG
import id.psw.vshlauncher.types.sequentialimages.XMBAnimGIF
import id.psw.vshlauncher.types.sequentialimages.XMBAnimMMR
import id.psw.vshlauncher.types.sequentialimages.XMBAnimWebP
import id.psw.vshlauncher.types.sequentialimages.XMBFrameAnimation
import id.psw.vshlauncher.uniqueActivityName
import java.io.File

/**
 * Content Information Files structure loader
 *
 * Since XmbAppItem and XmbShortcutItem is generic, this class is to make sure
 * the loader implementation have the same exact behaviour
 */
class CIFLoader(val vsh : VSH, private val resInfo : ResolveInfo, val root : ArrayList<File>) {
    companion object {
        var disableAnimatedIcon = false
        var disableBackSound = false
        var disableBackdrop = false
        var disableBackdropOverlay = false
        const val DEFAULT_BITMAP_REF = "None"
        val default_bitmap = BitmapRef("none", {XMBItem.TRANSPARENT_BITMAP}, BitmapRef.FallbackColor.Transparent)
        private val ios = mutableMapOf<File, Ref<Boolean>>()
        private val ioc = mutableMapOf<File, Ref<Int>>()
    }

    private val _iconSync = Object()
    private val _animIconSync = Object()
    private val _backdropSync = Object()
    private val _portBackdropSync = Object()
    private val _backSoundSync = Object()

    private var _animIcon : XMBFrameAnimation = XMBItem.TRANSPARENT_ANIM_BITMAP
    private var _backdrop = default_bitmap
    private var _backOverlay = default_bitmap
    private var _portBackdrop = default_bitmap
    private var _portBackOverlay = default_bitmap
    private var _backSound : File = XMBItem.SILENT_AUDIO
    private var _icon = default_bitmap

    val icon get() = _icon
    val backdrop get()= _backdrop
    val portBackdrop get() = _portBackdrop
    val backSound get() = _backSound
    val backOverlay get() = _backOverlay
    val portBackOverlay get() = _portBackOverlay
    val animIcon get() = _animIcon

    fun requestCustomizationFiles(fileBaseName : String, extensions: Array<String>) : ArrayList<File>{
        return ArrayList<File>().apply {
            for(s in root){
                for(e in extensions){
                    add(File(s, "$fileBaseName.$e"))
                }
            }
        }
    }

    private fun createCustomizationFileArray(isDisabled: Boolean, fileBaseName: String, extensions : Array<String>)
    : ArrayList<File>
    {
        return ArrayList<File>().apply {
            if(!isDisabled) addAll(requestCustomizationFiles(fileBaseName, extensions))
        }
    }

    private var backdropFiles = createCustomizationFileArray(disableBackdrop,"PIC1",VshResTypes.IMAGES)
    private var backdropOverlayFiles = createCustomizationFileArray(disableBackdropOverlay,"PIC0",VshResTypes.IMAGES)
    private var portraitBackdropFiles = createCustomizationFileArray(disableBackdrop,"PIC1_P",VshResTypes.IMAGES)
    private var portraitBackdropOverlayFiles = createCustomizationFileArray(disableBackdropOverlay,"PIC0_P",VshResTypes.IMAGES)
    private var animatedIconFiles = createCustomizationFileArray(disableAnimatedIcon,"ICON1",VshResTypes.ANIMATED_ICONS)
    private var iconFiles = createCustomizationFileArray(false,"ICON0",VshResTypes.ICONS)
    private var backSoundFiles = createCustomizationFileArray(disableBackSound,"SND0",VshResTypes.SOUNDS)
    private var _hasAnimIconLoaded = false
    val hasIconLoaded get() = _icon.isLoaded
    val hasAnimIconLoaded get() = _hasAnimIconLoaded
    val hasBackSoundLoaded get() = _backSound.exists()
    val hasBackdropLoaded get() = _backdrop.isLoaded
    val hasPortBackdropLoaded get() = _portBackdrop.isLoaded

    private fun <K> MutableMap<File, Ref<K>>.getOrMake(k:File, refDefVal:K) = getOrMake<File, Ref<K>>(k){ Ref<K>(refDefVal) }

    private fun ArrayList<File>.checkAnyExists() : Boolean = any { it.delayedExistenceCheck(ioc.getOrMake(it, 0), ios.getOrMake(it, false)) }

    val hasBackdrop : Boolean get() =                   !disableBackdrop && backdropFiles.checkAnyExists()
    val hasPortraitBackdrop : Boolean get() =           !disableBackdrop && backdropOverlayFiles.checkAnyExists()
    val hasBackOverlay : Boolean get() =                !disableBackdropOverlay && backdropOverlayFiles.checkAnyExists()
    val hasPortraitBackdropOverlay : Boolean get() =    !disableBackdropOverlay && portraitBackdropOverlayFiles.checkAnyExists()
    val hasBackSound : Boolean get() =                  !disableBackSound && backSoundFiles.checkAnyExists()
    val hasAnimatedIcon : Boolean get() =               !disableAnimatedIcon && animatedIconFiles.checkAnyExists()


    fun loadIcon(){
        // No need to sync, BitmapRef loaded directly
        _icon = BitmapRef("${resInfo.uniqueActivityName}_icon", {
            var found = false
            var rv : Bitmap? = null
            val file = iconFiles.firstOrNull {it.exists()}

            if(file != null){
                try{
                    rv = BitmapFactory.decodeFile(file.absolutePath)
                    found = true
                }catch(e:Exception){
                    vsh.postNotification(
                        null,
                        vsh.getString(R.string.error_common_header),
                        "Icon file for package ${resInfo.uniqueActivityName} is corrupted : $file :\n${e.message}"
                    )
                }
            }

            if(!found){
                rv = vsh.iconAdapter.create(resInfo.activityInfo, vsh)
            }
            rv
        })

        if(vsh.playAnimatedIcon){
            synchronized(_animIconSync){
                if((!_hasAnimIconLoaded || _animIcon.hasRecycled)){
                    for(file in animatedIconFiles){
                        if(file.exists() || file.isFile){
                            _animIcon = when (file.extension.uppercase()) {
                                "WEBP" -> XMBAnimWebP(file)
                                "APNG" -> XMBAnimAPNG(file)
                                "MP4" -> XMBAnimMMR(file.absolutePath)
                                "GIF" -> XMBAnimGIF(file)
                                else -> XMBItem.WHITE_ANIM_BITMAP
                            }
                            _hasAnimIconLoaded = true
                            break
                        }
                    }
                }
            }
        }
    }

    fun unloadIcon(){

        if(_icon.id != default_bitmap.id) _icon.release()
        _icon = default_bitmap

        synchronized(_animIconSync){
            if(_hasAnimIconLoaded || !_animIcon.hasRecycled){
                _hasAnimIconLoaded = false
                if(_animIcon != XMBItem.WHITE_ANIM_BITMAP && _animIcon != XMBItem.TRANSPARENT_ANIM_BITMAP) _animIcon.recycle()
                _animIcon = XMBItem.TRANSPARENT_ANIM_BITMAP
            }
        }
    }

    fun loadBackdrop(){
        _backdrop = BitmapRef("${resInfo.uniqueActivityName}_backdrop", {
            val f = backdropFiles.find {
                it.exists()
            }
            if(f != null) BitmapFactory.decodeFile(f.absolutePath)else null
        })
    }

    fun unloadBackdrop(){
        if(_backdrop.id != default_bitmap.id) _backdrop.release()
        _backdrop = default_bitmap
    }

    fun loadSound(){
        backSoundFiles.find { it.exists() }?.let {
            _backSound = it
        }
    }

    fun unloadSound(){
        _backSound = XMBItem.SILENT_AUDIO
    }

}