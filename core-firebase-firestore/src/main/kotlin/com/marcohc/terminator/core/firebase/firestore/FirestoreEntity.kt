package com.marcohc.terminator.core.firebase.firestore

interface FirestoreEntity {
    var id: String

    fun toMap(): Map<String, Any>

}
