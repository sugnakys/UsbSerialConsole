package jp.sugnakys.usbserialconsole.usb

sealed class UsbState {
    object Initialized : UsbState()
    object Ready : UsbState()
    object NotSupported : UsbState()
    object NoUsb : UsbState()
    object Disconnected : UsbState()
    object CdcDriverNotWorking : UsbState()
    object UsbDeviceNotWorking : UsbState()
}

sealed class UsbPermission {
    object Granted : UsbPermission()
    object NotGranted : UsbPermission()
}