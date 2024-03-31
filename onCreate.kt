override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val manager = getSystemService(Context.USB_SERVICE) as UsbManager
    // Для версий API ниже 31, используйте 0 или подходящий флаг вместо PendingIntent.FLAG_IMMUTABLE
    val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
    val filter = IntentFilter(ACTION_USB_PERMISSION)
    // Флаги RECEIVER_EXPORTED и RECEIVER_NOT_EXPORTED не используются, просто регистрируйте приемник
    registerReceiver(usbReceiver, filter)
}
