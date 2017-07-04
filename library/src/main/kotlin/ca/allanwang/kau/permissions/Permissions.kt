package ca.allanwang.kau.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.Manifest.permission
import android.Manifest.permission.SYSTEM_ALERT_WINDOW
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.RECEIVE_MMS
import android.Manifest.permission.RECEIVE_WAP_PUSH
import android.Manifest.permission.READ_SMS
import android.Manifest.permission.RECEIVE_SMS
import android.Manifest.permission.SEND_SMS
import android.Manifest.permission.BODY_SENSORS
import android.Manifest.permission.PROCESS_OUTGOING_CALLS
import android.Manifest.permission.USE_SIP
import android.Manifest.permission.ADD_VOICEMAIL
import android.Manifest.permission.WRITE_CALL_LOG
import android.Manifest.permission.READ_CALL_LOG
import android.Manifest.permission.CALL_PHONE
import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.WRITE_CONTACTS
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_CALENDAR
import android.Manifest.permission.READ_CALENDAR



/**
 * Created by Allan Wang on 2017-07-02.
 *
 * Bindings for the permission manager
 */

/**
 * Hook that should be added inside all [Activity.onRequestPermissionsResult] so that the Permission manager can handle the responses
 */
fun Activity.kauOnRequestPermissionsResult(permissions: Array<out String>, grantResults: IntArray)
        = PermissionManager.onRequestPermissionsResult(this, permissions, grantResults)

/**
 * Request a permission with a callback
 * In reality, an activity is needed to fulfill the request, but a context is enough if those permissions are already granted
 * To be safe, you may want to check that the context can be casted successfully first
 * The [callback] returns [granted], which is true if all permissions are granted
 * [deniedPerm] is the first denied permission, if granted is false
 */
fun Context.requestPermissions(vararg permissions: String, callback: (granted: Boolean, deniedPerm: String?) -> Unit)
        = PermissionManager(this, permissions, callback)

/**
 * See http://developer.android.com/guide/topics/security/permissions.html#normal-dangerous for a
 * list of 'dangerous' permissions that require a permission request on API 23.
 */
const val PERMISSION_READ_CALENDAR = Manifest.permission.READ_CALENDAR

const val PERMISSION_WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR

const val PERMISSION_CAMERA = Manifest.permission.CAMERA

const val PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS
const val PERMISSION_WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS
const val PERMISSION_GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS

const val PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
const val PERMISSION_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

const val PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO

const val PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
const val PERMISSION_CALL_PHONE = Manifest.permission.CALL_PHONE
const val PERMISSION_READ_CALL_LOG = Manifest.permission.READ_CALL_LOG
const val PERMISSION_WRITE_CALL_LOG = Manifest.permission.WRITE_CALL_LOG
const val PERMISSION_ADD_VOICEMAIL = Manifest.permission.ADD_VOICEMAIL
const val PERMISSION_USE_SIP = Manifest.permission.USE_SIP
const val PERMISSION_PROCESS_OUTGOING_CALLS = Manifest.permission.PROCESS_OUTGOING_CALLS

const val PERMISSION_BODY_SENSORS = Manifest.permission.BODY_SENSORS

const val PERMISSION_SEND_SMS = Manifest.permission.SEND_SMS
const val PERMISSION_RECEIVE_SMS = Manifest.permission.RECEIVE_SMS
const val PERMISSION_READ_SMS = Manifest.permission.READ_SMS
const val PERMISSION_RECEIVE_WAP_PUSH = Manifest.permission.RECEIVE_WAP_PUSH
const val PERMISSION_RECEIVE_MMS = Manifest.permission.RECEIVE_MMS

const val PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
const val PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

const val PERMISSION_SYSTEM_ALERT_WINDOW = Manifest.permission.SYSTEM_ALERT_WINDOW