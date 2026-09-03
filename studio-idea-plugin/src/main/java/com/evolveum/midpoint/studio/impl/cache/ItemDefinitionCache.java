package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.SmartPointerManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Dominik.
 */
public class ItemDefinitionCache {
    private static final Map<SmartPsiElementPointer<? extends PsiElement>, ItemDefinition<?> > cache =
            new ConcurrentHashMap<>();

    public static void put(PsiElement element, ItemDefinition<?> definition) {
        SmartPointerManager manager = SmartPointerManager.getInstance(element.getProject());
        SmartPsiElementPointer<? extends PsiElement> pointer = manager.createSmartPsiElementPointer(element);
        cache.put(pointer, definition);
    }

    public static ItemDefinition<?> get(PsiElement element) {
        for (Map.Entry<SmartPsiElementPointer<? extends PsiElement>, ItemDefinition<?>> entry : cache.entrySet()) {
            if (entry.getKey().getElement() == element) {
                return entry.getValue();
            }
        }
        return null;
    }
}
