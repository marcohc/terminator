@file:Suppress("unused")

package com.marcohc.terminator.core.firebase.firestore

import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.marcohc.terminator.core.firebase.onError
import com.marcohc.terminator.core.firebase.onSuccess
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber

inline fun <reified T : FirestoreEntity> FirebaseFirestore.getEntitiesList(path: String): Single<List<T>> {
    return Single
        .create<List<DocumentSnapshot>> { emitter ->
            Timber.v("Firestore --> getEntitiesList($path)")
            this
                .collection(path)
                .get()
                .onSuccess { emitter.onSuccess(it.documents) }
                .onError { emitter.onError(it) }
        }
        .map { documents ->
            documents.map { documentSnapshot ->
                // For entities with only id
                if (documentSnapshot.data.isNullOrEmpty()) {
                    T::class.java.newInstance().apply { id = documentSnapshot.id }
                } else {
                    requireNotNull(documentSnapshot.toObject(T::class.java)).apply {
                        id = documentSnapshot.id
                    }
                }
            }
        }
        .doOnSuccess { Timber.v("Firestore --> received ${it.size} ${T::class.simpleName}") }
        .doOnError { Timber.e(it, "Firestore --> error") }
}

inline fun <reified T : FirestoreEntity> Query.getEntitiesList(): Single<List<T>> {
    return Single
        .create<List<DocumentSnapshot>> { emitter ->
            Timber.v("Firestore --> getEntitiesList()")
            this
                .get()
                .onSuccess { emitter.onSuccess(it.documents) }
                .onError { emitter.onError(it) }
        }
        .map { documents ->
            documents.map { documentSnapshot ->
                // For entities with only id
                if (documentSnapshot.data.isNullOrEmpty()) {
                    T::class.java.newInstance().apply { id = documentSnapshot.id }
                } else {
                    requireNotNull(documentSnapshot.toObject(T::class.java)).apply {
                        id = documentSnapshot.id
                    }
                }
            }
        }
        .doOnSuccess { Timber.v("Firestore --> received ${it.size} ${T::class.simpleName}") }
        .doOnError { Timber.e(it, "Firestore --> error") }
}

inline fun <reified T : FirestoreEntity> Query.getEntityLimitToOne(): Single<Optional<T>> {
    return Single
        .create<Optional<T>> { emitter ->
            Timber.v("Firestore --> getEntityLimitToOne()")
            this
                .get()
                .onSuccess { querySnapshot ->
                    when {
                        querySnapshot.documents.isEmpty() -> {
                            Timber.v("Firestore --> no ${T::class.simpleName} entity by query found")
                            emitter.onSuccess(None)
                        }
                        querySnapshot.documents.size > 1 -> {
                            Timber.v("Firestore --> more than one ${T::class.simpleName} entity found with this query: ${querySnapshot.documents.size}")
                            emitter.onSuccess(None)
                        }
                        else -> {
                            val document = querySnapshot.documents.first()
                            val entity = Optional.toOptional(
                                document.toObject(T::class.java)!!.apply { id = document.id }
                            )
                            Timber.v("Firestore --> ${entity::class.simpleName} found")
                            emitter.onSuccess(entity)
                        }
                    }
                }
                .onError { emitter.onError(it) }
        }
        .doOnError { Timber.e(it, "Firestore --> error") }
}

inline fun <reified T : FirestoreEntity> FirebaseFirestore.getEntity(
    path: String,
    documentId: String
): Single<Optional<T>> {
    return Single
        .create<DocumentSnapshot> { emitter ->
            Timber.v("Firestore --> getEntity(path: $path, entityId: $documentId)")
            this
                .collection(path)
                .document(documentId)
                .get()
                .onSuccess { emitter.onSuccess(it) }
                .onError { emitter.onError(it) }
        }
        .map { document ->
            if (document.exists()) {
                Optional.toOptional(document.toObject(T::class.java)!!.apply { id = document.id })
            } else {
                None
            }
        }
        .doOnSuccess {
            when (it) {
                is Some -> Timber.v("Firestore --> found ${it.value::class.simpleName}")
                is None -> Timber.v("Firestore --> ${T::class.simpleName} not found")
            }
        }
        .doOnError { Timber.e(it, "Firestore --> error") }
}

fun FirebaseFirestore.exists(path: String, documentId: String): Single<Boolean> {
    return Single
        .create<DocumentSnapshot> { emitter ->
            Timber.v("Firestore --> getEntity(path: $path, documentId: $documentId)")
            this
                .collection(path)
                .document(documentId)
                .get()
                .onSuccess { emitter.onSuccess(it) }
                .onError { emitter.onError(it) }
        }
        .map { document -> document.exists() }
        .doOnSuccess {
            when (it) {
                true -> Timber.v("Firestore --> document ${"$path/$documentId"} exists")
                false -> Timber.v("Firestore --> document ${"$path/$documentId"} does not exist")
            }
        }
        .doOnError { Timber.e(it, "Firestore --> error") }
}

fun FirebaseFirestore.saveList(path: String, entitiesList: List<FirestoreEntity>): Completable {
    return Completable
        .create { emitter ->
            Timber.v("Firestore --> saveList(path: $path, entitiesList: ${entitiesList.size})")
            runBatch { batch ->
                entitiesList.forEach {
                    val reference = collection(path)
                    val id = if (it.id.isBlank()) reference.document().id else it.id
                    batch.set(reference.document(id), it.toMap())
                }
            }
                .onSuccess { emitter.onComplete() }
                .onError { emitter.onError(it) }
        }
        .doOnComplete { Timber.v("Firestore --> saved ${entitiesList.size} $path entities") }
        .doOnError { Timber.e(it, "Firestore --> error") }
}

fun FirebaseFirestore.save(path: String, entity: FirestoreEntity): Completable {
    return Completable
        .create { emitter ->
            Timber.v("Firestore --> save(path: $path, entity: ${entity})")
            val reference = collection(path)
            val id = if (entity.id.isBlank()) reference.document().id else entity.id
            reference
                .document(id)
                .set(entity.toMap())
                .onSuccess { emitter.onComplete() }
                .onError { emitter.onError(it) }
        }
        .doOnComplete { Timber.v("Firestore --> saved ${entity::class.simpleName}") }
        .doOnError { Timber.e(it, "Firestore --> error") }
}

fun FirebaseFirestore.delete(path: String, documentId: String): Completable {
    return Completable
        .create { emitter ->
            Timber.v("Firestore --> delete(path: $path, documentId: ${documentId})")
            collection(path)
                .document(documentId)
                .delete()
                .onSuccess { emitter.onComplete() }
                .onError { emitter.onError(it) }
        }
        .doOnComplete { Timber.v("Firestore --> ${path}/${documentId} deleted") }
        .doOnError { Timber.e(it, "Firestore --> error") }
}
