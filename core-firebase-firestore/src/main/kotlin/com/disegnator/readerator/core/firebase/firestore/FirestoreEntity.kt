package com.disegnator.readerator.core.firebase.firestore

interface FirestoreEntity {
    var id: String

    fun toMap(): Map<String, Any>

}
