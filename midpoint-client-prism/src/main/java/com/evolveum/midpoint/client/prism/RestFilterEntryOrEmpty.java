package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.client.api.query.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

import javax.xml.namespace.QName;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestFilterEntryOrEmpty<O extends ObjectType> implements FilterEntryOrEmpty<O> {

    @Override
    public AtomicFilterEntry<O> not() {
        return null;
    }

    @Override
    public ConditionEntry<O> item(ItemPathType itemPathType) {
        return null;
    }

    @Override
    public ConditionEntry<O> item(QName... qNames) {
        return null;
    }

    @Override
    public AtomicFilterExit<O> isDirectChildOf(ObjectReferenceType objectReferenceType) {
        return null;
    }

    @Override
    public AtomicFilterExit<O> isChildOf(ObjectReferenceType objectReferenceType) {
        return null;
    }

    @Override
    public AtomicFilterExit<O> isDirectChildOf(String s) {
        return null;
    }

    @Override
    public AtomicFilterExit<O> isChildOf(String s) {
        return null;
    }

    @Override
    public AtomicFilterExit<O> isRoot() {
        return null;
    }

    @Override
    public AtomicFilterExit<O> endBlock() {
        return null;
    }

    @Override
    public FilterExit<O> asc(QName... qNames) {
        return null;
    }

    @Override
    public FilterExit<O> asc(ItemPathType itemPathType) {
        return null;
    }

    @Override
    public FilterExit<O> desc(QName... qNames) {
        return null;
    }

    @Override
    public FilterExit<O> desc(ItemPathType itemPathType) {
        return null;
    }

    @Override
    public FilterExit<O> group(QName... qNames) {
        return null;
    }

    @Override
    public FilterExit<O> group(ItemPathType itemPathType) {
        return null;
    }

    @Override
    public FilterExit<O> offset(Integer integer) {
        return null;
    }

    @Override
    public FilterExit<O> maxSize(Integer integer) {
        return null;
    }

    @Override
    public SearchService<O> build() {
        return null;
    }
}
