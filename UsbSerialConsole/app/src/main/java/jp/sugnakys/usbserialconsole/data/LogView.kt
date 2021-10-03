package jp.sugnakys.usbserialconsole.data

data class LogView(
    val time: Time,
    val text: String
) {
    data class Time(
        val unixTime: Long,
        val format: String,
        val visibility: Boolean
    )
}