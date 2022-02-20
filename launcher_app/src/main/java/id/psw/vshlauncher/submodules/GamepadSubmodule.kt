package id.psw.vshlauncher.submodules

import android.graphics.PointF
import android.util.SparseArray
import android.view.InputDevice
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.MotionEvent
import android.view.MotionEvent.*
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.hasFlag
import id.psw.vshlauncher.select
import kotlin.math.abs

class GamepadSubmodule(ctx: VSH) {
    enum class Key (val base:UByte){
        None (0b0u),
        MaskSystem      (0b00000000u),
        MaskFace        (0b00010000u),
        MaskDirPad      (0b00100000u),
        MaskLShoulder   (0b01000000u),
        MaskLSAxis      (0b01001000u),
        MaskRShoulder   (0b10000000u),
        MaskRSAxis      (0b10001000u),
        PS              (0b00000001u),
        Start           (0b00000010u),
        Select          (0b00000100u),
        Triangle        (0b00010001u),
        Square          (0b00010010u),
        Circle          (0b00010100u),
        Cross           (0b00011000u),
        PadU            (0b00100001u),
        PadD            (0b00100010u),
        PadL            (0b00100100u),
        PadR            (0b00101000u),
        L1              (0b01000001u),
        L2              (0b01000010u),
        L3              (0b01000100u),
        LSX             (0b01001001u),
        LSY             (0b01001010u),
        R1              (0b10000001u),
        R2              (0b10000010u),
        R3              (0b10000100u),
        RSX             (0b10001001u),
        RSY             (0b10001010u);

        companion object {
            /** Let the Asian and Western Console users to have their preference in here. they have their preference and culture
             *
             * And also... Who maps Triangle as Back button? isn't that adds more complexity than just swapping Circle and Cross? */
            var spotMarkedByX = false
            /** Standard Confirm Button */
            val Confirm get() = spotMarkedByX.select(Cross, Circle)
            /** Standard Cancel Button */
            val Cancel get() = spotMarkedByX.select(Circle, Cross)
        }
    }

    enum class KeyState(val base:UByte) {
        Free (0b0000u),
        Down (0b0001u),
        Held (0b0010u),
        Up   (0b0100u),
    }

    enum class Remap {
        Normal,
        DualSense,
        DualShock4,
        DualShock3,
    }

    class InputState {
        var state : KeyState = KeyState.Free
        var axisValue : Float = 0.0f
        var downSince : Float = 0.0f

        override fun toString(): String = "$state : $axisValue"
    }

    companion object {
        private val remapOfDualSense = mapOf(
            KEYCODE_BUTTON_L2 to KEYCODE_BUTTON_SELECT,
            KEYCODE_BUTTON_R2 to KEYCODE_BUTTON_START,
            KEYCODE_BUTTON_A to KEYCODE_BUTTON_X,
            KEYCODE_BUTTON_B to KEYCODE_BUTTON_A,
            KEYCODE_BUTTON_C to KEYCODE_BUTTON_B,
            KEYCODE_BUTTON_X to KEYCODE_BUTTON_Y,
            KEYCODE_BUTTON_START to KEYCODE_BUTTON_THUMBR,
            KEYCODE_BUTTON_SELECT to KEYCODE_BUTTON_THUMBL,
            KEYCODE_BUTTON_Y to KEYCODE_BUTTON_L1,
            KEYCODE_BUTTON_L1 to KEYCODE_BUTTON_L2,
            KEYCODE_BUTTON_Z to KEYCODE_BUTTON_R1,
            KEYCODE_BUTTON_R1 to KEYCODE_BUTTON_R2,
        )
        private val android2psKey = mapOf(
            KEYCODE_BUTTON_A to Key.Cross,
            KEYCODE_BUTTON_B to Key.Circle,
            KEYCODE_BUTTON_X to Key.Square,
            KEYCODE_BUTTON_Y to Key.Triangle,
            KEYCODE_BUTTON_SELECT to Key.Select,
            KEYCODE_BUTTON_START to Key.Start,
            KEYCODE_BUTTON_MODE to Key.PS,
            KEYCODE_BUTTON_L1 to Key.L1,
            KEYCODE_BUTTON_L2 to Key.L2,
            KEYCODE_BUTTON_THUMBL to Key.L3,
            KEYCODE_BUTTON_R1 to Key.R1,
            KEYCODE_BUTTON_R2 to Key.R2,
            KEYCODE_BUTTON_THUMBR to Key.R3,
        )
        private val android2psAxis = mapOf(
            AXIS_Z to Key.RSX,
            AXIS_RZ to Key.RSY,
            AXIS_X to Key.LSX,
            AXIS_Y to Key.LSY,
            AXIS_HAT_X to Key.PadL,
            AXIS_HAT_Y to Key.PadR,
        )
        private val kbd2psKey = mapOf(
            KEYCODE_X to Key.Cross,
            KEYCODE_S to Key.Circle,
            KEYCODE_Z to Key.Square,
            KEYCODE_A to Key.Triangle,
            KEYCODE_SHIFT_RIGHT to Key.Select,
            KEYCODE_ENTER to Key.Start,
            KEYCODE_BREAK to Key.PS,
            KEYCODE_1 to Key.L2,
            KEYCODE_2 to Key.L3,
            KEYCODE_3 to Key.R2,
            KEYCODE_Q to Key.L1,
            KEYCODE_W to Key.R3,
            KEYCODE_E to Key.R1,
            KEYCODE_DPAD_UP to Key.PadU,
            KEYCODE_DPAD_DOWN to Key.PadD,
            KEYCODE_DPAD_LEFT to Key.PadL,
            KEYCODE_DPAD_RIGHT to Key.PadR,
            KEYCODE_T to Key.LSY,
            KEYCODE_F to Key.LSX,
            KEYCODE_G to Key.LSY,
            KEYCODE_H to Key.LSX,
            KEYCODE_I to Key.RSY,
            KEYCODE_J to Key.RSX,
            KEYCODE_K to Key.RSY,
            KEYCODE_L to Key.RSX,
        )
        private val kbd2psKeyAxisMul = mapOf(
            KEYCODE_T to -1.0f,
            KEYCODE_F to -1.0f,
            KEYCODE_G to 1.0f,
            KEYCODE_H to 1.0f,
            KEYCODE_I to -1.0f,
            KEYCODE_J to -1.0f,
            KEYCODE_K to 1.0f,
            KEYCODE_L to 1.0f
        )

        const val SONY_DUALSHOCK_3_HID_ID = 0x054C0268
        const val SONY_DUALSHOCK_4_V1_HID_ID = 0x054C05C4
        const val SONY_DUALSHOCK_4_V2_HID_ID = 0x054C09CC
        const val SONY_DUALSENSE_HID_ID = 0x054C0CE6
    }

    data class GamePadInfo(
        val id : Int,
        val product : Int,
        val vendor : Int,
        val name : String){
        companion object {
            fun hexform(int:Int): String{
                return Integer.toHexString(int).padStart(4,'0').uppercase()
            }
        }
        val displayName = "$name (${hexform(vendor)} ${hexform(product)})"
        val deviceId = (vendor shl 16) or (product)
    }

    val gamePads = mutableMapOf<Int, GamePadInfo>()

    private val inputPairs = mutableMapOf<Key, InputState>()
    private val touchPairs = SparseArray<PointF>()
    var downThreshold : Float = 0.5f

    init{
        Key.values().forEach {
            inputPairs[it] = InputState()
        }
    }

    fun getAxisValue(k:Key) : Float{
        if(!inputPairs.containsKey(k)){
            inputPairs[k] = InputState()
        }
        return inputPairs[k]?.axisValue ?: 0.0f;
    }

    fun getKeyDown(k:Key) : Boolean =
        inputPairs[k]?.state == KeyState.Down
    fun getKey(k:Key) : Boolean =
        inputPairs[k]?.state == KeyState.Held
    fun getKeyUp(k:Key) : Boolean = inputPairs[k]?.state == KeyState.Up
    fun getKeyFree(k:Key) : Boolean = inputPairs[k]?.state == KeyState.Free
    fun getDownTime(k:Key) : Float = inputPairs[k]?.downSince ?: 0.0f

    private val keyReMap = mutableMapOf(SONY_DUALSENSE_HID_ID to remapOfDualSense)
    private val keyReMapActive = mutableMapOf(SONY_DUALSENSE_HID_ID to true)

    private fun getRemappedKeyCode(keyCode:Int, device:Int) : Int {
        return if(keyReMapActive[device] == true){
            keyReMap[device]?.get(keyCode) ?: keyCode
        }else keyCode
    }

    private fun androidToPsKey(keyCode: Int, isAxis:Boolean, deviceId : Int) : Key {
        val remapKeyCode = getRemappedKeyCode(keyCode, deviceId)
        return (if(isAxis) android2psAxis else android2psKey)[remapKeyCode] ?: Key.None
    }

    private fun keyboardToPsKey(keyCode:Int) : Key {
        return kbd2psKey[keyCode] ?: Key.None
    }

    private fun getKbdAxis(keyCode:Int) : Float {
        return kbd2psKeyAxisMul[keyCode] ?: 0.0f
    }

    fun motionEventReceiver(evt:MotionEvent) : Boolean {
        return if(
            (evt.source hasFlag InputDevice.SOURCE_JOYSTICK ||
            evt.source hasFlag InputDevice.SOURCE_DPAD) &&
            evt.action == ACTION_MOVE){
                val padId = gamePads[evt.deviceId]?.deviceId ?: -1
                arrayOf(AXIS_X, AXIS_Y, AXIS_Z, AXIS_RZ, AXIS_HAT_X, AXIS_HAT_Y).forEach { axis ->
                    val axVal = evt.getAxisValue(axis)
                    val nVal = if(axVal <= 0.0f) abs(axVal) else 0.0f
                    val pVal = if(axVal >= 0.0f) abs(axVal) else 0.0f
                    when (axis) {
                        AXIS_HAT_X -> {
                            inputPairs[Key.PadL]?.axisValue = nVal
                            inputPairs[Key.PadR]?.axisValue = pVal
                            inputPairs[Key.PadL]?.state = (nVal > downThreshold).select(KeyState.Down, KeyState.Up)
                            inputPairs[Key.PadR]?.state = (pVal > downThreshold).select(KeyState.Down, KeyState.Up)
                        }
                        AXIS_HAT_Y -> {
                            inputPairs[Key.PadU]?.axisValue = nVal
                            inputPairs[Key.PadD]?.axisValue = pVal
                            inputPairs[Key.PadU]?.state = (nVal > downThreshold).select(KeyState.Down, KeyState.Up)
                            inputPairs[Key.PadD]?.state = (pVal > downThreshold).select(KeyState.Down, KeyState.Up)
                        }
                        else -> {
                            val psKey = androidToPsKey(axis, true, padId)
                            inputPairs[psKey]?.axisValue = axVal
                            inputPairs[psKey]?.state = (axVal > downThreshold).select(KeyState.Down, KeyState.Up)
                        }
                    }
                }
                true
        } else false
    }

    fun keyEventReceiver(isDown: Boolean, keyCode:Int, evt:KeyEvent) : Boolean {
        if(evt.source hasFlag InputDevice.SOURCE_GAMEPAD){
            if(evt.repeatCount == 0){
                val padId = gamePads[evt.deviceId]?.deviceId ?: -1
                val key = androidToPsKey(keyCode, false, padId)
                if(key != Key.None){
                    inputPairs[key]?.apply {
                        axisValue = isDown.select(1.0f, 0.0f)
                        state = isDown.select(KeyState.Down, KeyState.Up)
                    }
                    return true
                }
            }
        }else if(evt.source hasFlag InputDevice.SOURCE_KEYBOARD){
            if(evt.repeatCount == 0){
                val key = keyboardToPsKey(keyCode)
                if(key != Key.None){
                    inputPairs[key]?.apply {
                        axisValue += isDown.select(1.0f, -1.0f) * getKbdAxis(keyCode)
                        axisValue = axisValue.coerceIn(-1.0f, 1.0f)
                        state = isDown.select(KeyState.Down, KeyState.Up)
                    }
                    return true
                }
            }
        }
        return false
    }

    fun update(time:Float){
        inputPairs.values.forEach{
            if(it.state == KeyState.Down || it.state == KeyState.Held) it.downSince += time
            if(it.state == KeyState.Up || it.state == KeyState.Free) it.downSince = 0.0f
            if(it.state == KeyState.Down){ it.state = KeyState.Held }
            if(it.state == KeyState.Up){ it.state = KeyState.Free }
            if(it.axisValue >= downThreshold && it.state == KeyState.Free){ it.state = KeyState.Down }
            if(it.axisValue <= downThreshold && it.state == KeyState.Held){ it.state = KeyState.Up }
        }
    }
}