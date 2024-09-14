package com.java.firebase.demo.common;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

public class GenericCRUDService<T> {
    private final Class<T> entityClass;
    private final String collectionName;

    public GenericCRUDService(Class<T> entityClass, String collectionName) {
        this.entityClass = entityClass;
        this.collectionName = collectionName;
    }

    public CollectionReference getCollection() {
        Firestore db = FirestoreClient.getFirestore();
        return db.collection(collectionName);
    }

    public void createDocument(String documentId, T entity) {
        getCollection().document(documentId).set(entity);
    }

    public T readDocument(String documentId) throws Exception {
        return getCollection().document(documentId).get().get().toObject(entityClass);
    }

    public void updateDocument(String documentId, T entity) {
        getCollection().document(documentId).set(entity);
    }

    public void deleteDocument(String documentId) {
        getCollection().document(documentId).delete();
    }
}
