package id.psw.vshlauncher.submodules

import id.psw.vshlauncher.Vsh

class SubmoduleManager(ctx: Vsh) {
    val pref = PreferenceSubmodule(ctx)
    val bmp = BitmapManager(ctx)
    val customizer = CustomizerPackageSubmodule()
    val plugin = PluginManager(ctx)
    val icons = XmbAdaptiveIconRenderer(ctx)
    val network = NetworkSubmodule(ctx)
    val gamepad = GamepadSubmodule(ctx)
    val gamepadUi= GamepadUISubmodule()
    val audio = AudioSubmodule(ctx)
    val updater = UpdateCheckSubmodule(ctx)

    fun onCreate(){
        for(mod in arrayOf(
            pref,
            bmp,
            customizer,
            audio,
            icons,
            network,
            gamepad,
            gamepadUi,
            plugin,
            updater
        )){
            if(mod is IVshSubmodule){
                mod.onCreate()
            }
        }
    }

    fun onDestroy(){
        for(mod in arrayOf(
            pref,
            bmp,
            customizer,
            audio,
            icons,
            network,
            gamepad,
            gamepadUi,
            plugin,
            updater
        )){
            if(mod is IVshSubmodule){
                mod.onDestroy()
            }
        }
    }
}